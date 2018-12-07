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

-- 1. Drop foreign keys
--
ALTER TABLE userGroup
  DROP FOREIGN KEY FK_userGroup_client_id;

ALTER TABLE userGroup_x_authority
  DROP FOREIGN KEY FK_userGroup_x_authority_userGroup_id,
  DROP FOREIGN KEY FK_userGroup_x_authority_authority_id;

ALTER TABLE task_x_userGroup
  DROP FOREIGN KEY FK_task_x_userGroup_task_id,
  DROP FOREIGN KEY FK_task_x_userGroup_userGroup_id;

ALTER TABLE user_x_userGroup
  DROP FOREIGN KEY FK_user_x_userGroup_user_id,
  DROP FOREIGN KEY FK_user_x_userGroup_userGroup_id;

-- 2. Rename userGroup tables
--
ALTER TABLE userGroup RENAME TO role;

ALTER TABLE userGroup_x_authority
  CHANGE userGroup_id role_id INT(11) NOT NULL,
  RENAME TO role_x_authority;

ALTER TABLE task_x_userGroup
  CHANGE userGroup_id role_id INT(11) NOT NULL,
  RENAME TO task_x_role;

ALTER TABLE user_x_userGroup
  CHANGE userGroup_id role_id INT(11) NOT NULL,
  RENAME TO user_x_role;

-- 3. Add foreign key to role tables
--
ALTER TABLE role ADD CONSTRAINT FK_role_client_id
  FOREIGN KEY (client_id) REFERENCES client (id);

ALTER TABLE role_x_authority
  ADD CONSTRAINT FK_role_x_authority_authority_id
  FOREIGN KEY (authority_id) REFERENCES authority (id),
  ADD CONSTRAINT FK_role_x_authority_role_id
  FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE task_x_role
  ADD CONSTRAINT FK_task_x_role_task_id
  FOREIGN KEY (task_id) REFERENCES task (id),
  ADD CONSTRAINT FK_task_x_role_role_id
  FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE user_x_role
  ADD CONSTRAINT FK_user_x_role_user_id
  FOREIGN KEY (user_id) REFERENCES user (id),
  ADD CONSTRAINT FK_user_x_role_role_id
  FOREIGN KEY (role_id) REFERENCES role (id);

-- 4. Update entries in authority table
--
UPDATE authority SET title = 'viewAllRoles_globalAssignable'
WHERE title = 'viewAllUserGroups_globalAssignable';

UPDATE authority SET title = 'viewRole_globalAssignable'
WHERE title = 'viewUserGroup_globalAssignable';

UPDATE authority SET title = 'addRole_globalAssignable'
WHERE title = 'addUserGroup_globalAssignable';

UPDATE authority SET title = 'editRole_globalAssignable'
WHERE title = 'editUserGroup_globalAssignable';

UPDATE authority SET title = 'deleteRole_globalAssignable'
WHERE title = 'deleteUserGroup_globalAssignable';

UPDATE authority SET title = 'viewAllRoles_clientAssignable'
WHERE title = 'viewAllUserGroups_clientAssignable';

UPDATE authority SET title = 'viewRole_clientAssignable'
WHERE title = 'viewUserGroup_clientAssignable';

UPDATE authority SET title = 'addRole_clientAssignable'
WHERE title = 'addUserGroup_clientAssignable';

UPDATE authority SET title = 'editRole_clientAssignable'
WHERE title = 'editUserGroup_clientAssignable';

UPDATE authority SET title = 'deleteRole_clientAssignable'
WHERE title = 'deleteUserGroup_clientAssignable';
