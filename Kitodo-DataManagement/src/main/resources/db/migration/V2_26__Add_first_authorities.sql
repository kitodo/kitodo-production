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
-- Update admin authority that it can only assigned globally

UPDATE authority SET clientAssignable='0',projectAssignable='0' WHERE title='admin';

-- Add authorities to secure the access to the entities

# Client
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllClients', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewClient', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editClient', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteClient', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addClient', '1', '0', '0');

# Project
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProject', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllProjects', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProject', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteProject', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addProject', '1', '1', '0');

# Docket
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllDockets', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewDocket', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addDocket', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editDocket', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteDocket', '1', '0', '0');

# Process
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllProcesses', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcess', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addProcess', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcess', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteProcess', '1', '1', '1');

# Task
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllTasks', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewTask', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addTask', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editTask', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteTask', '1', '1', '1');

# UserGroup
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllUserGroups', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewUserGroup', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addUserGroup', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editUserGroup', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteUserGroup', '1', '1', '0');

# User
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllUsers', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewUser', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addUser', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editUser', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteUser', '1', '1', '0');

-- Assign admin authority to all user groups which has permission = 1
-- admin authority was inserted at V2_17

INSERT IGNORE INTO userGroup_x_authority (userGroup_id,authority_id)
SELECT userGroup.id,1 FROM userGroup WHERE permission = '1';
