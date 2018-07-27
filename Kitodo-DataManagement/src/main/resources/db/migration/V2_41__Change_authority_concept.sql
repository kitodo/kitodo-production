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

DROP TABLE userGroup_x_project_x_authority;
DROP TABLE userGroup_x_client_x_authority;

ALTER TABLE authority
DROP COLUMN `projectAssignable`,
DROP COLUMN `clientAssignable`,
DROP COLUMN `globalAssignable`;

# Client
INSERT INTO authority (title) VALUES ('viewClient_Client');
INSERT INTO authority (title) VALUES ('editClient_Client');

# Project
INSERT INTO authority (title) VALUES ('viewProject_Client');
INSERT INTO authority (title) VALUES ('viewAllProjects_Client');
INSERT INTO authority (title) VALUES ('editProject_Client');
INSERT INTO authority (title) VALUES ('deleteProject_Client');
INSERT INTO authority (title) VALUES ('addProject_Client');

INSERT INTO authority (title) VALUES ('viewProject_Project');
INSERT INTO authority (title) VALUES ('editProject_Project');

# Docket
INSERT INTO authority (title) VALUES ('viewDocket_Client');
INSERT INTO authority (title) VALUES ('viewAllDockets_Client');
INSERT INTO authority (title) VALUES ('editDocket_Client');
INSERT INTO authority (title) VALUES ('deleteDocket_Client');
INSERT INTO authority (title) VALUES ('addDocket_Client');

# Ruleset
INSERT INTO authority (title) VALUES ('viewRuleset_Client');
INSERT INTO authority (title) VALUES ('viewAllRulesets_Client');
INSERT INTO authority (title) VALUES ('editRuleset_Client');
INSERT INTO authority (title) VALUES ('deleteRuleset_Client');
INSERT INTO authority (title) VALUES ('addRuleset_Client');

# Process
INSERT INTO authority (title) VALUES ('viewProcess_Client');
INSERT INTO authority (title) VALUES ('viewAllProcesses_Client');
INSERT INTO authority (title) VALUES ('editProcess_Client');
INSERT INTO authority (title) VALUES ('deleteProcess_Client');
INSERT INTO authority (title) VALUES ('addProcess_Client');

INSERT INTO authority (title) VALUES ('viewProcess_Project');
INSERT INTO authority (title) VALUES ('viewAllProcesses_Project');
INSERT INTO authority (title) VALUES ('editProcess_Project');
INSERT INTO authority (title) VALUES ('deleteProcess_Project');
INSERT INTO authority (title) VALUES ('addProcess_Project');

INSERT INTO authority (title) VALUES ('editProcessMetaData_Client');
INSERT INTO authority (title) VALUES ('editProcessStructureData_Client');
INSERT INTO authority (title) VALUES ('editProcessPagination_Client');
INSERT INTO authority (title) VALUES ('editProcessImages_Client');
INSERT INTO authority (title) VALUES ('viewProcessMetaData_Client');
INSERT INTO authority (title) VALUES ('viewProcessStructureData_Client');
INSERT INTO authority (title) VALUES ('viewProcessPagination_Client');
INSERT INTO authority (title) VALUES ('viewProcessImages_Client');

INSERT INTO authority (title) VALUES ('editProcessMetaData_Project');
INSERT INTO authority (title) VALUES ('editProcessStructureData_Project');
INSERT INTO authority (title) VALUES ('editProcessPagination_Project');
INSERT INTO authority (title) VALUES ('editProcessImages_Project');
INSERT INTO authority (title) VALUES ('viewProcessMetaData_Project');
INSERT INTO authority (title) VALUES ('viewProcessStructureData_Project');
INSERT INTO authority (title) VALUES ('viewProcessPagination_Project');
INSERT INTO authority (title) VALUES ('viewProcessImages_Project');

# Task
INSERT INTO authority (title) VALUES ('viewTask_Client');
INSERT INTO authority (title) VALUES ('viewAllTasks_Client');
INSERT INTO authority (title) VALUES ('editTask_Client');
INSERT INTO authority (title) VALUES ('deleteTask_Client');
INSERT INTO authority (title) VALUES ('addTask_Client');

INSERT INTO authority (title) VALUES ('viewTask_Project');
INSERT INTO authority (title) VALUES ('viewAllTasks_Project');
INSERT INTO authority (title) VALUES ('editTask_Project');
INSERT INTO authority (title) VALUES ('deleteTask_Project');
INSERT INTO authority (title) VALUES ('addTask_Project');

# UserGroup
INSERT INTO authority (title) VALUES ('viewUserGroup_Client');
INSERT INTO authority (title) VALUES ('viewAllUserGroups_Client');
INSERT INTO authority (title) VALUES ('editUserGroup_Client');
INSERT INTO authority (title) VALUES ('deleteUserGroup_Client');
INSERT INTO authority (title) VALUES ('addUserGroup_Client');

# User
INSERT INTO authority (title) VALUES ('viewUser_Client');
INSERT INTO authority (title) VALUES ('viewAllUsers_Client');
INSERT INTO authority (title) VALUES ('editUser_Client');
INSERT INTO authority (title) VALUES ('deleteUser_Client');
INSERT INTO authority (title) VALUES ('addUser_Client');
