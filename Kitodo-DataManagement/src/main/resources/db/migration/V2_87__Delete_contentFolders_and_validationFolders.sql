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
-- Migration: Delete tables contentFolders_task_x_folder and
--            validationFolders_task_x_folder

-- 1. Delete tables contentFolders_task_x_folder and
--    validationFolders_task_x_folder

DROP TABLE contentFolders_task_x_folder,
           validationFolders_task_x_folder;
