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
-- Migration: Split processes and templates

-- 1. Add template table
--

CREATE TABLE template (
  id INT(11) NOT NULL AUTO_INCREMENT,
  title VARCHAR (255) DEFAULT NULL,
  outputName VARCHAR (255) DEFAULT NULL,
  creationDate datetime DEFAULT NULL,
  inChoiceListShown tinyint(1) DEFAULT NULL,
  sortHelperStatus VARCHAR(255) DEFAULT NULL,
  wikiField longtext,
  project_id INT(11) DEFAULT NULL,
  ruleset_id INT(11) DEFAULT NULL,
  docket_id INT(11) DEFAULT NULL,
  indexAction VARCHAR(6),
  old_id INT(11) NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Copy templates to template table
--

INSERT INTO template (title, outputName, creationDate, inChoiceListShown, sortHelperStatus, wikiField, project_id, ruleset_id, docket_id, old_id)
SELECT title, outputName, creationDate, inChoiceListShown, sortHelperStatus, wikiField, project_id, ruleset_id, docket_id, id
FROM process WHERE template = 1;

-- 3. Update process and task table - add template_id column
--

ALTER TABLE process ADD template_id INT(11) DEFAULT NULL;
ALTER TABLE task ADD template_id INT(11) DEFAULT NULL;

-- 4. Switch off safe updates
--

SET SQL_SAFE_UPDATES = 0;

-- 5. Update process table - add template_id column
--

UPDATE process AS p
  INNER JOIN process_x_property AS pxp ON p.id = pxp.process_id
  INNER JOIN property AS pp ON pxp.property_id = pp.id
  INNER JOIN template AS t ON t.old_id = pp.value
SET p.template_id = t.id WHERE pp.title = 'TemplateID';

-- 6. Remove foreign key process - property
--

ALTER TABLE process_x_property DROP FOREIGN KEY `FK_process_x_property_process_id`;
ALTER TABLE process_x_property DROP FOREIGN KEY `FK_process_x_property_property_id`;

-- 7. Remove entries from process_x_property table
--

DELETE pxp FROM process_x_property AS pxp
  INNER JOIN template AS t ON pxp.process_id = t.old_id
WHERE pxp.process_id = t.old_id;

-- 8. Remove foreign key task - process
--

ALTER TABLE task DROP FOREIGN KEY FK_task_process_id;

-- 9. Replace templates ids in task table
--

UPDATE task AS t
  INNER JOIN template AS temp ON t.process_id = temp.old_id
SET t.template_id = temp.id,
  t.process_id = NULL
WHERE t.process_id = temp.old_id;

-- 10. Delete processes from batches that are templates
--
-- If someone ever linked that together, which was possible in the front-end,
-- this is a mistake in the data. Batches are not intended to ever contain
-- templates, and those batches cannot ever have been useful for anything.
--

DELETE batch_x_process FROM batch_x_process
  LEFT JOIN process ON batch_x_process.process_id = process.id
  WHERE process.template = 1;

-- 11. Remove templates from process table
--

DELETE FROM process
WHERE template = 1;

-- 12. Switch on safe updates
--

SET SQL_SAFE_UPDATES = 1;

-- 13. Drop column with old ids
--

ALTER TABLE template DROP old_id;

-- 14. Add foreign keys
--

ALTER TABLE task ADD CONSTRAINT `FK_task_process_id`
FOREIGN KEY (process_id) REFERENCES process(id);

ALTER TABLE task ADD CONSTRAINT `FK_task_template_id`
FOREIGN KEY (template_id) REFERENCES template(id);

ALTER TABLE template ADD CONSTRAINT `FK_template_project_id`
FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE template ADD CONSTRAINT `FK_template_ruleset_id`
FOREIGN KEY (ruleset_id) REFERENCES ruleset(id);

ALTER TABLE template ADD CONSTRAINT `FK_template_docket_id`
FOREIGN KEY (docket_id) REFERENCES docket(id);

ALTER TABLE process ADD CONSTRAINT `FK_process_template_id`
FOREIGN KEY (template_id) REFERENCES template(id);

ALTER TABLE process_x_property ADD CONSTRAINT `FK_process_x_property_process_id`
FOREIGN KEY (process_id) REFERENCES process (id);

ALTER TABLE process_x_property ADD CONSTRAINT `FK_process_x_property_property_id`
FOREIGN KEY (property_id) REFERENCES property (id);
