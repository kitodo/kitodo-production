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
-- Migration: Remove relation between task and user group and add
-- relation between user group and client.
--

-- 1. Drop cross table for tasks and users
DROP TABLE task_x_user;

-- 2. Add cross table user group and client
CREATE TABLE userGroup_x_client (
  `userGroup_id` INT(11) NOT NULL,
  `client_id` INT(11) DEFAULT NULL)
  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Add foreign keys
ALTER TABLE userGroup_x_client ADD CONSTRAINT `FK_userGroup_x_client_userGroup_id`
FOREIGN KEY (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_client ADD CONSTRAINT `FK_userGroup_x_client_client_id`
FOREIGN KEY (client_id) REFERENCES client(id);

-- 4. Copy all user groups to new table
INSERT INTO userGroup_x_client (userGroup_id, client_id) SELECT id, null FROM userGroup;

-- 5. Switch off safe updates
SET SQL_SAFE_UPDATES = 0;

-- 6. Assign the dummy client to every user group
UPDATE userGroup_x_client, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET userGroup_x_client.client_id = dummyClient.id WHERE userGroup_x_client.client_id IS NULL;

-- 7. Switch on safe updates
SET SQL_SAFE_UPDATES = 1;
