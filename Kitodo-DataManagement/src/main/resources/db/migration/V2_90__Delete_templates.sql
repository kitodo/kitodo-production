--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.
--
-- 1. set templateIds to null
-- 2. delete template Tasks from task_x_role
-- 3. delete template Tasks
-- 4. truncate template

SET SQL_SAFE_UPDATES = 0;
UPDATE process SET template_id = NULL;
DELETE FROM task_x_role WHERE task_id IN (SELECT id FROM task WHERE template_id is not NULL);
DELETE FROM task WHERE template_id is not NULL;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE template;
TRUNCATE project_x_template;
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
