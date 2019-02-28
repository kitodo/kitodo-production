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

-- Update roles and authorities.

-- 1. Add initial roles if they don not exists yet
--
INSERT IGNORE INTO role (title, client_id) VALUES ('WorkflowManagement', 1);

-- 2. Add initial authorities to roles
--
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Workflow_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Docket_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Ruleset_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllTemplates_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Template_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllProjects_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllProcesses_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Process_clientAssignable';


-- 3. Add workflow management to user which is assigned to project management
--
INSERT IGNORE  INTO user_x_role (user_id, role_id)
  SELECT (SELECT user_id FROM user_x_role WHERE role_id = (SELECT id FROM role WHERE title = 'Projectmanagement')),
  id FROM role WHERE title = 'WorkflowManagement';

-- 4. Delete not needed authorities from process management

DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Workflow_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Docket_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Ruleset_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Template_clientAssignable');
