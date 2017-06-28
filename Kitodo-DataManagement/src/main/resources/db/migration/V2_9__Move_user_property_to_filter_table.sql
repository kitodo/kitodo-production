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
-- Migration: Move user property to filter table

-- 1. Create filter table

CREATE TABLE filter (
  id INT(11) NOT NULL,
  value longtext DEFAULT NULL,
  creationDate datetime DEFAULT NULL,
  indexAction VARCHAR(6),
  user_id INT(11),
  PRIMARY KEY (id)
);

-- 2. Move filters from property table to filter table

INSERT INTO filter (id, value, creationDate)
       SELECT id, value, creationDate
       FROM property
       WHERE title = '_filter';

-- 3. Update table with id

UPDATE filter AS f, user_x_property AS p SET f.user_id = p.user_id WHERE p.property_id = f.id;

-- 4. Drop foreign keys from user_x_property table

ALTER TABLE user_x_property DROP FOREIGN KEY `FK_user_x_property_user_id`;
ALTER TABLE user_x_property DROP FOREIGN KEY `FK_user_x_property_property_id`;

-- 5. Drop user_x_property table

DROP TABLE user_x_property;

-- 6. Introduce auto_increment to filter table

ALTER TABLE filter
  CHANGE id id INT(11) NOT NULL AUTO_INCREMENT;

-- 7. Add foreign keys to user table

ALTER TABLE filter ENGINE=InnoDB;
ALTER TABLE filter
  ADD CONSTRAINT `FK_filter_x_user_id`
FOREIGN KEY (user_id) REFERENCES user (id);

-- 8. Delete all filters from property table

DELETE FROM property WHERE title = '_filter';
