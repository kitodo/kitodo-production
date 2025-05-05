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
-- Delete columns for index action

ALTER TABLE batch DROP COLUMN indexAction;
ALTER TABLE docket DROP COLUMN indexAction;
ALTER TABLE filter DROP COLUMN indexAction;
ALTER TABLE process DROP COLUMN indexAction;
ALTER TABLE project DROP COLUMN indexAction;
ALTER TABLE ruleset DROP COLUMN indexAction;
ALTER TABLE task DROP COLUMN indexAction;
ALTER TABLE template DROP COLUMN indexAction;
ALTER TABLE workflow DROP COLUMN indexAction;

--
-- Add index for better task search

CREATE INDEX idx_task_filtering_title ON task (process_id, typeAutomatic, processingStatus, title);
