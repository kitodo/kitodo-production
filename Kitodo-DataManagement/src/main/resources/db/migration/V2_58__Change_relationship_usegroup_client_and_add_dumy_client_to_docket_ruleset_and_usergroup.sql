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

-- Change relationship between user group and client.
-- Add dummy client to dockets, rulesets and user groups without assigned client

-- 1. Drop foreign keys
--
ALTER TABLE userGroup_x_client
  DROP FOREIGN KEY FK_userGroup_x_client_client_id;
ALTER TABLE userGroup_x_client
  DROP FOREIGN KEY FK_userGroup_x_client_userGroup_id;

-- 2. Remove all data from userGroup_x_client table
--
TRUNCATE TABLE userGroup_x_client;

-- 2. Add column for client id to userGroup table
--
ALTER TABLE userGroup ADD client_id INT(11);

-- 4. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 5. Assign the client to every docket and ruleset which has no client
--
UPDATE docket docketTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET docketTable.client_id = dummyClient.id WHERE docketTable.client_id IS NULL;

UPDATE ruleset rulesetTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET rulesetTable.client_id = dummyClient.id WHERE rulesetTable.client_id IS NULL;

UPDATE userGroup userGroupTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET userGroupTable.client_id = dummyClient.id WHERE userGroupTable.client_id IS NULL;

-- 6. Switch on safe updates
SET SQL_SAFE_UPDATES = 1;

-- 7. Add foreign key to userGroup table
--
ALTER TABLE userGroup ADD CONSTRAINT `FK_userGroup_client_id`
foreign key (client_id) REFERENCES client(id);

-- 8. Drop userGroup_x_client table
--
DROP TABLE userGroup_x_client;
