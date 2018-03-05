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

-- Delete admin authority

DELETE FROM authority WHERE id='1';

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

-- Assign all authorities to user group 1, assuming that this is administrator user group
-- starting with id 2 because id 1 was used by admin authority at V2_17

INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,2);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,3);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,4);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,5);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,6);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,7);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,8);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,9);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,10);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,11);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,12);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,13);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,14);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,15);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,16);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,17);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,18);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,19);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,20);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,21);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,22);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,23);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,24);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,25);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,26);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,27);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,28);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,29);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,30);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,31);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,32);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,33);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,34);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,35);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,36);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,37);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,38);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,39);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,40);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,41);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,42);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,43);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,44);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,45);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,46);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,47);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,48);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,49);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,50);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,51);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,52);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,53);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,54);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,55);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,56);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,57);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,58);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,59);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,60);
INSERT INTO usergroup_x_authority (userGroup_id,authority_id) VALUES (1,61);
