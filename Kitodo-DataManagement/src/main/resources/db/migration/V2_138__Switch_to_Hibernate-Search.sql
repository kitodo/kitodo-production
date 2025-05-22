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

CREATE INDEX index_task_processingstatus ON task(processingStatus);
CREATE INDEX index_task_title ON task (title);
CREATE INDEX index_process_title ON process (title);

--
-- Auto-update process.sortHelperStatus on task change

SET GLOBAL log_bin_trust_function_creators = 1;
DELIMITER //
CREATE TRIGGER update_sortHelperStatus_on_task_update
AFTER UPDATE ON task
FOR EACH ROW
BEGIN
   DECLARE tasks_for_process INT;
   DECLARE locked_tasks VARCHAR(3);
   DECLARE open_tasks VARCHAR(3);
   DECLARE inwork_tasks VARCHAR(3);
   DECLARE done_tasks VARCHAR(3);
   DECLARE progress VARCHAR(12);

   SELECT COUNT(*) INTO tasks_for_process FROM task WHERE process_id = NEW.process_id;

   IF tasks_for_process > 0 THEN
      SELECT LPAD(ROUND(100 * COUNT(*) / tasks_for_process), 3, '0') INTO locked_tasks
         FROM task WHERE process_id = NEW.process_id AND processingStatus = 0;

      SELECT LPAD(ROUND(100 * COUNT(*) / tasks_for_process), 3, '0') INTO open_tasks
         FROM task WHERE process_id = NEW.process_id AND processingStatus = 1;

      SELECT LPAD(ROUND(100 * COUNT(*) / tasks_for_process), 3, '0') INTO inwork_tasks
         FROM task WHERE process_id = NEW.process_id AND processingStatus = 2;

      SELECT LPAD(ROUND(100 * COUNT(*) / tasks_for_process), 3, '0') INTO done_tasks
         FROM task WHERE process_id = NEW.process_id AND processingStatus = 3;

      SET progress = CONCAT(done_tasks, inwork_tasks, open_tasks, locked_tasks);
   ELSE
      SET progress = NULL;
   END IF;

   UPDATE process
      SET sortHelperStatus = progress
      WHERE id = NEW.process_id;
END;//
DELIMITER ;
SET GLOBAL log_bin_trust_function_creators = 0;
