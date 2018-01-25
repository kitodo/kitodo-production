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

CREATE TABLE ldapserver (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) DEFAULT NULL,
  `url` VARCHAR(255) DEFAULT NULL,
  `managerLogin` VARCHAR(255) DEFAULT NULL,
  `managerPassword` VARCHAR(255) DEFAULT NULL,
  `nextFreeUnixIdPattern` VARCHAR(255) DEFAULT NULL,
  `useSsl` TINYINT(1) DEFAULT NULL,
  `readonly` TINYINT(1) DEFAULT NULL,
  `passwordEncryption` VARCHAR(6) DEFAULT NULL,
  PRIMARY KEY (`id`)),
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE ldapgroup
ADD COLUMN `ldapserver_id` INT(11) NULL DEFAULT NULL;

ALTER TABLE ldapgroup ADD CONSTRAINT `FK_ldapGroup_ldapServer_id`
foreign key (ldapserver_id) REFERENCES ldapserver(id);