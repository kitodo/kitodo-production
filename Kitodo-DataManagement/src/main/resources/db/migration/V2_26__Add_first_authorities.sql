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
VALUES ('delteProject', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addProject', '1', '1', '0');

# Batch
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllBatches', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewBatch', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addBatch', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editBatch', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteBatch', '1', '1', '1');

# Docket
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllDockets', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewDocket', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addDocket', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editDocket', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteDocket', '1', '1', '1');

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

# Template
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllTemplates', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewTemplate', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addTemplate', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editTemplate', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteTemplate', '1', '1', '1');

# Ruleset
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllRulesets', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewRuleset', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addRuleset', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editRuleset', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteRuleset', '1', '1', '1');

# UserGroup
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllUserGroups', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewUserGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addUserGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editUserGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteUserGroup', '1', '0', '0');

# User
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllUsers', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewUser', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addUser', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editUser', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteUser', '1', '0', '0');

# LdapGroup
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllLdapGroups', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteLdapGroup', '1', '0', '0');

# LdapServer
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllLdapServers', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteLdapServer', '1', '0', '0');
