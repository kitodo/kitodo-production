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

-- Add authority to assign OCR-D workflow in template or process details
INSERT IGNORE INTO authority (title) VALUES ('assignOcrdWorkflow_clientAssignable');

-- Add columns of OCR-D workflow identifier
ALTER TABLE process ADD ocrd_workflow_id varchar(255) DEFAULT NULL;
ALTER TABLE template ADD ocrd_workflow_id varchar(255) DEFAULT NULL;
