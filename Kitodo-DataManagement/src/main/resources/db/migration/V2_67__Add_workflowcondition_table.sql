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
-- Migration: Remove relation between task and user group and add
-- relation between user group and client.
--

-- 1. Add workflowCondition table
CREATE TABLE workflowCondition (
  id INT(11) NOT NULL  AUTO_INCREMENT,
  type VARCHAR(50) DEFAULT NULL,
  value VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Adjust column for store workflow condition
ALTER TABLE task CHANGE workflowCondition workflowCondition_id INT(11);

-- 3. Add foreign keys
ALTER TABLE task ADD CONSTRAINT `FK_task_workflowCondition_workflowCondition_id`
FOREIGN KEY (workflowCondition_id) REFERENCES workflowCondition(id);
