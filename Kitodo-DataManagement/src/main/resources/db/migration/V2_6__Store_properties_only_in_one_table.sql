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
-- Migration: Store properties only in one table: Property


-- 1. Remove auto_increment and primary key from property tables
--        sequence ids
--        reactivate auto_increment and primary key
--        hack: insert value and delete it, so LAST_INSERT_ID is up to date.
--

-- for workpieceproperty
ALTER TABLE workpieceProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE workpieceProperty
DROP PRIMARY KEY;

UPDATE workpieceProperty
JOIN (SELECT @rank := LAST_INSERT_ID()) r
SET id=@rank:=@rank+1;
ALTER TABLE workpieceProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);
INSERT INTO workpieceProperty (title)
VALUES('dummyMigrationDeleteValue');
DELETE FROM workpieceProperty WHERE title ='dummyMigrationDeleteValue';

-- for userproperty
ALTER TABLE userProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE userProperty
DROP PRIMARY KEY;

UPDATE userProperty
JOIN (SELECT @rank := LAST_INSERT_ID()) r
SET id=@rank:=@rank+1;
ALTER TABLE userProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);
INSERT INTO userProperty (title)
VALUES('dummyMigrationDeleteValue');
DELETE FROM userProperty WHERE title ='dummyMigrationDeleteValue';

-- for templateproperty
ALTER TABLE templateProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE templateProperty
DROP PRIMARY KEY;

UPDATE templateProperty
JOIN (SELECT @rank := LAST_INSERT_ID()) r
SET id=@rank:=@rank+1;
ALTER TABLE templateProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);
INSERT INTO templateProperty (title)
VALUES('dummyMigrationDeleteValue');
DELETE FROM templateProperty WHERE title ='dummyMigrationDeleteValue';

-- for processproperty
ALTER TABLE processProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE processProperty
DROP PRIMARY KEY;

UPDATE processProperty
JOIN (SELECT @rank := LAST_INSERT_ID()) r
SET id=@rank:=@rank+1;
ALTER TABLE processProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);
INSERT INTO processProperty (title)
VALUES('dummyMigrationDeleteValue');
DELETE FROM processProperty WHERE title ='dummyMigrationDeleteValue';

-- 2. Create cross Tables, insert id and foreign keys from property tables

CREATE TABLE process_x_property (
  process_id  INT(11) NOT NULL,
  property_id INT(11) NOT NULL
);

INSERT INTO process_x_property (property_id,process_id)
SELECT id * 4 - 3 AS id, process_id
FROM processProperty;

CREATE TABLE template_x_property (
  template_id INT(11) NOT NULL,
  property_id INT(11) NOT NULL
);

INSERT INTO template_x_property (property_id,template_id)
SELECT id * 4 - 2 AS id, template_id
FROM templateProperty;

CREATE TABLE user_x_property (
  user_id     INT(11) NOT NULL,
  property_id INT(11) NOT NULL
);

INSERT INTO user_x_property (property_id,user_id)
SELECT id * 4 - 1 AS id, user_id
FROM userProperty;

CREATE TABLE workpiece_x_property (
  workpiece_id INT(11) NOT NULL,
  property_id  INT(11) NOT NULL
);

INSERT INTO workpiece_x_property (property_id,workpiece_id)
SELECT id * 4 AS id, workpiece_id
FROM workpieceProperty;

-- 3. Create property table

CREATE TABLE property (
  id int(11) NOT NULL,
  title varchar(255) DEFAULT NULL,
  value longtext DEFAULT NULL,
  obligatory tinyint(1) DEFAULT NULL,
  dataType int(11) DEFAULT NULL,
  choice varchar(255) DEFAULT NULL,
  creationDate datetime DEFAULT NULL,
  container int(11) DEFAULT NULL,
  PRIMARY KEY (id)
);

-- 4. Insert values into property table from old [...]property tables

INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate, container)
       SELECT id * 4 - 3 AS id, title, value, obligatory, dataType, choice, creationDate, container
       FROM processProperty;
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate, container)
       SELECT id * 4 - 2 AS id, title, value, obligatory, dataType, choice, creationDate, container
       FROM templateProperty;
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate)
       SELECT id * 4 - 1 AS id, title, value, obligatory, dataType, choice, creationDate
       FROM userProperty;
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate, container)
       SELECT id * 4 AS id, title, value, obligatory, dataType, choice, creationDate, container
       FROM workpieceProperty;

-- 5. Introduce auto_increment to property table

ALTER TABLE property
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT;

-- 6. Delete old [...]property tables

DROP TABLE processProperty;
DROP TABLE templateProperty;
DROP TABLE userProperty;
DROP TABLE workpieceProperty;

-- 7. Add foreign keys to cross tables

ALTER TABLE process_x_property ENGINE=InnoDB;
ALTER TABLE process_x_property
   ADD CONSTRAINT `FK_process_x_property_process_id`
 FOREIGN KEY (process_id) REFERENCES process (id);
ALTER TABLE process_x_property
   ADD CONSTRAINT `FK_process_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);


ALTER TABLE template_x_property ENGINE=InnoDB;
ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_template_id`
 FOREIGN KEY (template_id) REFERENCES template (id);
ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

ALTER TABLE user_x_property ENGINE=InnoDB;
 ALTER TABLE user_x_property
   ADD CONSTRAINT `FK_user_x_property_user_id`
 FOREIGN KEY (user_id) REFERENCES user (id);
 ALTER TABLE user_x_property
   ADD CONSTRAINT `FK_user_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

ALTER TABLE workpiece_x_property ENGINE=InnoDB;
ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_workpiece_id`
 FOREIGN KEY (workpiece_id) REFERENCES workpiece (id);
ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);
