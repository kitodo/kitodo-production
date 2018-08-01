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

-- 1. Create cross table

CREATE TABLE project_x_template (
  project_id  INT(11) NOT NULL,
  template_id INT(11) NOT NULL
) ENGINE=InnoDB;

-- 2. Insert id and foreign keys from project tables

INSERT INTO project_x_template (project_id, template_id)
SELECT project_id, id
FROM template;

-- 3. Drop foreign keys from project table

ALTER TABLE template DROP FOREIGN KEY `FK_template_project_id`;

-- 4. Remove process column

ALTER TABLE template DROP COLUMN project_id;

-- 5. Add foreign keys to cross table

ALTER TABLE project_x_template
   ADD CONSTRAINT `FK_project_x_template_project_id`
 FOREIGN KEY (project_id) REFERENCES project (id);
ALTER TABLE project_x_template
   ADD CONSTRAINT `FK_project_x_template_template_id`
 FOREIGN KEY (template_id) REFERENCES template (id);
