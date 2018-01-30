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

CREATE TABLE client (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NULL,
  PRIMARY KEY (id))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE project
  ADD COLUMN `client_id` INT(11) NULL DEFAULT NULL;

ALTER TABLE project ADD CONSTRAINT `FK_project_client_id`
foreign key (client_id) REFERENCES client(id);
