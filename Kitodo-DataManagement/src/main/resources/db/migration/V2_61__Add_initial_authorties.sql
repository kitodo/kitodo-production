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

-- Change user group and role.

-- 1. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Delete admin authority from cross table
--
DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'admin_globalAssignable');

-- 3. Drop admin authority
--
DELETE FROM authority WHERE title = 'admin_globalAssignable';

-- 4. Delete global authorities which shouldn't be global from cross table
--
DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllProjects_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Project_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllTemplates_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Template_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllWorkflows_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Workflow_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllDockets_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Docket_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllRulesets_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Ruleset_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllProcesses_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Process_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllBatches_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Batch_globalAssignable');

-- 5. Drop global authorities which should not be global
--
DELETE FROM authority WHERE title = 'viewAllProjects_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Project_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllTemplates_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Template_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllWorkflows_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Workflow_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllDockets_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Docket_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllRulesets_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Ruleset_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllProcesses_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Process_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllBatches_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Batch_globalAssignable';

-- 6. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 7. Add initial roles if they do not exist yet
--
INSERT IGNORE INTO role (title, client_id) VALUES ('Administration', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ClientManagement', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('RoleManagement', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('UserManagement', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ProcessManagement', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ProjectManagement', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('Metadata', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('Scanning', 1);

-- 8. Add initial authorities to roles
--
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title LIKE '%Ldap%';
  INSERT INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title LIKE '%Index%';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ClientManagement'), (SELECT id FROM authority WHERE title = 'viewAllClients_globalAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ClientManagement'), id FROM authority WHERE title LIKE '%Client_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ClientManagement'), (SELECT id FROM authority WHERE title = 'viewAllUsers_globalAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ClientManagement'), id FROM authority WHERE title LIKE '%User_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ClientManagement'), (SELECT id FROM authority WHERE title = 'viewAllRoles_globalAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ClientManagement'), id FROM authority WHERE title LIKE '%Role_globalAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'RoleManagement'), (SELECT id FROM authority WHERE title = 'viewAllRoles_clientAssignable'));
INSERT INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'RoleManagement'), id FROM authority WHERE title LIKE '%Role_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'UserManagement'), (SELECT id FROM authority WHERE title = 'viewAllUsers_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'UserManagement'), id FROM authority WHERE title LIKE '%User_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'UserManagement'), (SELECT id FROM authority WHERE title = 'viewAllRoles_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'UserManagement'), (SELECT id FROM authority WHERE title = 'viewRole_clientAssignable'));

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllTemplates_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Template_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Workflow_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Docket_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Ruleset_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewAllProjects_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProjectManagement'), id FROM authority WHERE title LIKE '%Project_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewAllTemplates_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewTemplate_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewWorkflow_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewDocket_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProjectManagement'), (SELECT id FROM authority WHERE title = 'viewRuleset_clientAssignable'));

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Metadata'), (SELECT id FROM authority WHERE title = 'viewAllProcesses_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Metadata'), (SELECT id FROM authority WHERE title = 'viewProcess_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Metadata'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Metadata'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Metadata'), id FROM authority WHERE title LIKE '%ProcessPagination_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Metadata'), (SELECT id FROM authority WHERE title = 'viewProcessImages_clientAssignable'));

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Scanning'), (SELECT id FROM authority WHERE title = 'viewAllProcesses_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Scanning'), (SELECT id FROM authority WHERE title = 'viewProcess_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Scanning'), id FROM authority WHERE title LIKE '%ProcessImages_clientAssignable';

-- 8. Add admins to client management
--
INSERT IGNORE  INTO user_x_role (user_id, role_id)
  SELECT (SELECT user_id FROM user_x_role WHERE role_id = (SELECT id FROM role WHERE title = 'Administration')),
  id FROM role WHERE title = 'ClientManagement';
