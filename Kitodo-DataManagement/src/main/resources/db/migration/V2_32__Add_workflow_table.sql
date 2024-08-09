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

--
-- Migration: Add workflow template

-- 1. Create table workflow
--

CREATE TABLE workflow (
  id INT(11) NOT NULL AUTO_INCREMENT,
  title VARCHAR (255) DEFAULT NULL,
  fileName VARCHAR (255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Add column related to workflow to template table
--

ALTER TABLE template ADD workflow_id INT(11) DEFAULT NULL;

-- 3. Add column related to workflow to task table
--

ALTER TABLE task ADD workflowCondition VARCHAR (255) DEFAULT NULL;

-- 4. Add foreign key
--

ALTER TABLE template add constraint `FK_template_workflow_id`
foreign key (workflow_id) REFERENCES workflow(id);
