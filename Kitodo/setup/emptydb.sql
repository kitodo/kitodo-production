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
-- Empty database
--
-- This script empties the database without deleting it.
--

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS authority;
DROP TABLE IF EXISTS batch;
DROP TABLE IF EXISTS batch_x_process;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS client_x_listcolumn;
DROP TABLE IF EXISTS client_x_user;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS dataeditor_setting;
DROP TABLE IF EXISTS docket;
DROP TABLE IF EXISTS filter;
DROP TABLE IF EXISTS flyway_schema_history;
DROP TABLE IF EXISTS folder;
DROP TABLE IF EXISTS importconfiguration;
DROP TABLE IF EXISTS importconfiguration_x_mappingfile;
DROP TABLE IF EXISTS ldapgroup;
DROP TABLE IF EXISTS ldapserver;
DROP TABLE IF EXISTS listcolumn;
DROP TABLE IF EXISTS mappingfile;
DROP TABLE IF EXISTS process;
DROP TABLE IF EXISTS process_x_property;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS project_x_template;
DROP TABLE IF EXISTS project_x_user;
DROP TABLE IF EXISTS property;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS role_x_authority;
DROP TABLE IF EXISTS ruleset;
DROP TABLE IF EXISTS searchfield;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS task_x_role;
DROP TABLE IF EXISTS template;
DROP TABLE IF EXISTS template_x_property;
DROP TABLE IF EXISTS urlparameter;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS user_x_role;
DROP TABLE IF EXISTS workflow;
DROP TABLE IF EXISTS workflowcondition;
DROP TABLE IF EXISTS workpiece_x_property;
SET FOREIGN_KEY_CHECKS = 1;
