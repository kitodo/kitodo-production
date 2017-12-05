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

-- Add authorizations table
CREATE TABLE authorization (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) DEFAULT NULL,
  `indexAction` VARCHAR(6) DEFAULT NULL,
  PRIMARY KEY (`id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Insert admin authorization
INSERT INTO authorization (`title`, `indexAction`) VALUES ('admin', 'INDEX');

-- Add table userGroup_x_authorization
CREATE TABLE userGroup_x_authorization (
  `userGroup_id` INT(11) NOT NULL,
  `authorization_id` INT(11) DEFAULT NULL)
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Insert ids of existing userGroups which have permission = 1 (admin permissions)
INSERT INTO userGroup_x_authorization (userGroup_id)
  SELECT id FROM userGroup WHERE permission=1;

-- Set authorization ids to 1 (inserted admin authorization)
UPDATE userGroup_x_authorization SET authorization_id = 1;

-- Set authorization_id column to not null
ALTER TABLE userGroup_x_authorization MODIFY COLUMN authorization_id INT(11) NOT NULL;

-- Add primary keys
ALTER TABLE userGroup_x_authorization ADD PRIMARY KEY (`authorization_id`,`userGroup_id`);

-- Add foreign keys
ALTER TABLE userGroup_x_authorization ADD CONSTRAINT `FK_userGroup_x_authorization_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_authorization ADD CONSTRAINT `FK_userGroup_x_authorization_authorization_id`
foreign key (authorization_id) REFERENCES authorization(id);
