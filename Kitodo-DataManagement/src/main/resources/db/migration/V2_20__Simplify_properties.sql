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

-- 1. Drop foreign keys

ALTER TABLE template_x_property DROP FOREIGN KEY `FK_template_x_property_template_id`;
ALTER TABLE template_x_property DROP FOREIGN KEY `FK_template_x_property_property_id`;

ALTER TABLE workpiece_x_property DROP FOREIGN KEY `FK_workpiece_x_property_workpiece_id`;
ALTER TABLE workpiece_x_property DROP FOREIGN KEY `FK_workpiece_x_property_property_id`;

-- 2. Update tables with new column names

ALTER TABLE template_x_property
  CHANGE template_id process_id INT(11) NOT NULL;
ALTER TABLE workpiece_x_property
  CHANGE workpiece_id process_id INT(11) NOT NULL;

-- 3. Update data in cross tables

UPDATE template_x_property JOIN template ON template_x_property.process_id = template.id
  SET template_x_property.process_id = template.process_id;
UPDATE workpiece_x_property JOIN workpiece ON workpiece_x_property.process_id = workpiece.id
  SET workpiece_x_property.process_id = workpiece.process_id;

-- 4. Delete obsolete tables

DROP TABLE template;
DROP TABLE workpiece;

-- 5. Restore foreign keys

ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_template_id`
 FOREIGN KEY (process_id) REFERENCES process (id);
ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_workpiece_id`
 FOREIGN KEY (process_id) REFERENCES process (id);
ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);
