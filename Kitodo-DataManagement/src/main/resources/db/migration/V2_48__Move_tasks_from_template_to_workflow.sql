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
-- Migration: Move tasks from template to workflow

-- Before run this migration you need to assign workflows to your templates by hand!!!

-- 1. Drop foreign key
--

ALTER TABLE task DROP FOREIGN KEY `FK_task_template_id`;

-- 2. Update column for task table
--

ALTER TABLE task CHANGE template_id workflow_id INT(11);

-- 3. Switch off safe updates
--

SET SQL_SAFE_UPDATES = 0;

-- 4. Update data in task table
--

UPDATE task SET task.workflow_id = (SELECT template.workflow_id FROM template WHERE task.workflow_id = template.id);

-- 5. Switch on safe updates
--

SET SQL_SAFE_UPDATES = 1;

-- 6. Drop foreign key
--

ALTER TABLE task ADD CONSTRAINT `FK_task_workflow_id`
FOREIGN KEY (workflow_id) REFERENCES workflow(id);
