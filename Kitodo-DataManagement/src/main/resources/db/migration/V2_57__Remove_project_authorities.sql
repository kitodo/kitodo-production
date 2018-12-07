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

-- Remove project authorities

-- 1. Drop foreign keys
--
ALTER TABLE userGroup_x_authority
  DROP FOREIGN KEY FK_userGroup_x_authority_authority_id;
ALTER TABLE userGroup_x_authority
  DROP FOREIGN KEY FK_userGroup_x_authority_userGroup_id;

-- 2. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 3. Delete project authorities from cross table
--
DELETE FROM userGroup_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%_projectAssignable');

-- 4. Delete project assignable authorities
--
DELETE FROM authority WHERE title LIKE '%_projectAssignable';

-- 5. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 6. Add foreign keys
--
ALTER TABLE userGroup_x_authority
  ADD CONSTRAINT FK_userGroup_x_authority_authority_id
  FOREIGN KEY (authority_id)
  REFERENCES authority (id),
  ADD CONSTRAINT FK_userGroup_x_authority_userGroup_id
  FOREIGN KEY (userGroup_id)
  REFERENCES userGroup (id);
