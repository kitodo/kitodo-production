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

-- 1. Removing tables and columns of former authority relation concept
DROP TABLE userGroup_x_project_x_authority;
DROP TABLE userGroup_x_client_x_authority;

ALTER TABLE authority
DROP COLUMN projectAssignable,
DROP COLUMN clientAssignable,
DROP COLUMN globalAssignable;

-- 2. Add table user_x_client
CREATE TABLE client_x_user (
  `client_id` INT(11) DEFAULT NULL,
  `user_id` INT(11) NOT NULL)
  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Add foreign keys
ALTER TABLE client_x_user add constraint `FK_client_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE client_x_user add constraint `FK_client_x_user_client_id`
foreign key (client_id) REFERENCES client(id);

-- 4. Switch off safe updates
SET SQL_SAFE_UPDATES = 0;

-- 5. Assign the dummy client to every user
INSERT INTO client_x_user (client_id, user_id) SELECT null, id FROM user;

UPDATE client_x_user client_x_userTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET client_x_userTable.client_id = dummyClient.id WHERE client_x_userTable.client_id IS NULL;

-- 6. Set client_id column to not null
ALTER TABLE client_x_user MODIFY COLUMN client_id INT(11) NOT NULL;

-- 7. Add '_globalAssignable' to every existing authority entry
UPDATE authority set title=concat(title,'_globalAssignable');

-- 8. Switch on safe updates
SET SQL_SAFE_UPDATES = 1;

-- 9. Add authorities to replace the client and project assignable columns
# Client
INSERT INTO authority (title) VALUES ('viewClient_clientAssignable');
INSERT INTO authority (title) VALUES ('editClient_clientAssignable');

# Project
INSERT INTO authority (title) VALUES ('viewProject_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllProjects_clientAssignable');
INSERT INTO authority (title) VALUES ('editProject_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteProject_clientAssignable');
INSERT INTO authority (title) VALUES ('addProject_clientAssignable');

INSERT INTO authority (title) VALUES ('viewProject_projectAssignable');
INSERT INTO authority (title) VALUES ('editProject_projectAssignable');

# Docket
INSERT INTO authority (title) VALUES ('viewDocket_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllDockets_clientAssignable');
INSERT INTO authority (title) VALUES ('editDocket_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteDocket_clientAssignable');
INSERT INTO authority (title) VALUES ('addDocket_clientAssignable');

# Ruleset
INSERT INTO authority (title) VALUES ('viewRuleset_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllRulesets_clientAssignable');
INSERT INTO authority (title) VALUES ('editRuleset_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteRuleset_clientAssignable');
INSERT INTO authority (title) VALUES ('addRuleset_clientAssignable');

# Process
INSERT INTO authority (title) VALUES ('viewProcess_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllProcesses_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcess_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteProcess_clientAssignable');
INSERT INTO authority (title) VALUES ('addProcess_clientAssignable');

INSERT INTO authority (title) VALUES ('viewProcess_projectAssignable');
INSERT INTO authority (title) VALUES ('viewAllProcesses_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcess_projectAssignable');
INSERT INTO authority (title) VALUES ('deleteProcess_projectAssignable');
INSERT INTO authority (title) VALUES ('addProcess_projectAssignable');

INSERT INTO authority (title) VALUES ('editProcessMetaData_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcessStructureData_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcessPagination_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcessImages_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessMetaData_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessStructureData_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessPagination_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessImages_clientAssignable');

INSERT INTO authority (title) VALUES ('editProcessMetaData_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcessStructureData_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcessPagination_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcessImages_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessMetaData_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessStructureData_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessPagination_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessImages_projectAssignable');

# Task
INSERT INTO authority (title) VALUES ('viewTask_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllTasks_clientAssignable');
INSERT INTO authority (title) VALUES ('editTask_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteTask_clientAssignable');
INSERT INTO authority (title) VALUES ('addTask_clientAssignable');

INSERT INTO authority (title) VALUES ('viewTask_projectAssignable');
INSERT INTO authority (title) VALUES ('viewAllTasks_projectAssignable');
INSERT INTO authority (title) VALUES ('editTask_projectAssignable');
INSERT INTO authority (title) VALUES ('deleteTask_projectAssignable');
INSERT INTO authority (title) VALUES ('addTask_projectAssignable');

# UserGroup
INSERT INTO authority (title) VALUES ('viewUserGroup_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllUserGroups_clientAssignable');
INSERT INTO authority (title) VALUES ('editUserGroup_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteUserGroup_clientAssignable');
INSERT INTO authority (title) VALUES ('addUserGroup_clientAssignable');

# User
INSERT INTO authority (title) VALUES ('viewUser_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllUsers_clientAssignable');
INSERT INTO authority (title) VALUES ('editUser_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteUser_clientAssignable');
INSERT INTO authority (title) VALUES ('addUser_clientAssignable');
