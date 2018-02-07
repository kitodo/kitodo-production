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


-- Rename authorization to authority
ALTER TABLE authorization RENAME TO authority;

ALTER TABLE userGroup_x_authorization
  DROP FOREIGN KEY FK_userGroup_x_authorization_authorization_id,
  DROP FOREIGN KEY FK_userGroup_x_authorization_userGroup_id;

ALTER TABLE userGroup_x_authorization
  CHANGE COLUMN authorization_id authority_id INT(11) NOT NULL ,
  DROP INDEX FK_userGroup_x_authorization_userGroup_id ,
  ADD INDEX FK_userGroup_x_authority_userGroup_id (userGroup_id ASC),
  RENAME TO  userGroup_x_authority;

ALTER TABLE userGroup_x_authority
  ADD CONSTRAINT FK_userGroup_x_authority_authority_id
  FOREIGN KEY (authority_id)
  REFERENCES authority (id),
  ADD CONSTRAINT FK_userGroup_x_authority_userGroup_id
  FOREIGN KEY (userGroup_id)
  REFERENCES userGroup (id);

-- Add client table

CREATE TABLE client (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NULL,
  indexAction VARCHAR(6) DEFAULT NULL,
  PRIMARY KEY (id))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE project
  ADD COLUMN client_id INT(11) NULL DEFAULT NULL;

ALTER TABLE project ADD CONSTRAINT FK_project_client_id
  foreign key (client_id) REFERENCES client(id);

-- Add authority relation tables

CREATE TABLE userGroup_x_client_x_authority (
  id INT NOT NULL AUTO_INCREMENT,
  userGroup_id INT NOT NULL,
  client_id INT NOT NULL,
  authority_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_index(`userGroup_id`, `client_id`,`authority_id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE userGroup_x_client_x_authority add constraint `FK_userGroup_x_client_x_authority_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_client_x_authority add constraint `FK_userGroup_x_client_x_authority_client_id`
foreign key (client_id) REFERENCES client(id);

ALTER TABLE userGroup_x_client_x_authority add constraint `FK_userGroup_x_client_x_authority_authority_id`
foreign key (authority_id) REFERENCES authority(id);


CREATE TABLE userGroup_x_project_x_authority (
  id INT NOT NULL AUTO_INCREMENT,
  userGroup_id INT NOT NULL,
  project_id INT NOT NULL,
  authority_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_index(`userGroup_id`, `project_id`,`authority_id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE userGroup_x_project_x_authority add constraint `FK_userGroup_x_project_x_authority_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_project_x_authority add constraint `FK_userGroup_x_project_x_authority_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE userGroup_x_project_x_authority add constraint `FK_userGroup_x_project_x_authority_authority_id`
foreign key (authority_id) REFERENCES authority(id);
