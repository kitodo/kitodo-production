--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- For the full copyright and license information, please read the
-- GPL3-License.txt file that was distributed with this source code.
--

--
-- Migration: Replace columns active and ready on status in workflow table.
--

-- 1. Add status column
ALTER TABLE workflow ADD COLUMN status VARCHAR(15) AFTER title;

-- 2. Update values in status column according to active and ready
SET SQL_SAFE_UPDATES = 0;

UPDATE workflow SET status = 'DRAFT'
  WHERE active = 1 AND ready = 0;

UPDATE workflow SET status = 'ACTIVE'
  WHERE active = 1 AND ready = 1;

UPDATE workflow SET status = 'ARCHIVED'
WHERE active = 0 AND ready = 1;

-- 3. Drop active and ready columns
ALTER TABLE workflow DROP COLUMN active;
ALTER TABLE workflow DROP COLUMN ready;

-- 4. Update column in listColumn table
UPDATE listColumn SET title = 'workflow.status'
  WHERE title = 'workflow.active';

-- 5. Drop foreign keys
ALTER TABLE client_x_listColumn
  DROP FOREIGN KEY FK_client_x_listColumn_client_id;
ALTER TABLE client_x_listColumn
  DROP FOREIGN KEY FK_client_x_listColumn_column_id;

-- 6. Delete not needed column
DELETE FROM client_x_listColumn WHERE column_id = (SELECT id FROM listColumn WHERE title = 'workflow.ready');

DELETE FROM listColumn WHERE title = 'workflow.ready';

SET SQL_SAFE_UPDATES = 1;

-- 7. Restore foreign keys
ALTER TABLE client_x_listColumn
  ADD CONSTRAINT FK_client_x_listColumn_client_id
    FOREIGN KEY (client_id) REFERENCES client(id),
  ADD CONSTRAINT FK_client_x_listColumn_column_id
    FOREIGN KEY (column_id) REFERENCES listColumn(id);
