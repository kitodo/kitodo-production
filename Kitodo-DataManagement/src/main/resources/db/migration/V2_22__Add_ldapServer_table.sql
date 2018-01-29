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

CREATE TABLE ldapServer (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) DEFAULT NULL,
  `url` VARCHAR(255) DEFAULT NULL,
  `managerLogin` VARCHAR(255) DEFAULT NULL,
  `managerPassword` VARCHAR(255) DEFAULT NULL,
  `nextFreeUnixIdPattern` VARCHAR(255) DEFAULT NULL,
  `useSsl` TINYINT(1) NOT NULL DEFAULT 0,
  `readOnly` TINYINT(1) NOT NULL DEFAULT 0,
  `passwordEncryption` INT NOT NULL DEFAULT 0,
  `rootCertificate` VARCHAR(255) DEFAULT NULL,
  `pdcCertificate` VARCHAR(255) DEFAULT NULL,
  `keystore` VARCHAR(255) DEFAULT NULL,
  `keystorePassword` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE ldapGroup
ADD COLUMN `ldapserver_id` INT(11) NULL DEFAULT NULL;

ALTER TABLE ldapGroup ADD CONSTRAINT `FK_ldapGroup_ldapServer_id`
foreign key (ldapserver_id) REFERENCES ldapServer(id);
