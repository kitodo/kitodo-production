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

-- 1. Disable safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Rename list columns from "correctionMessage" to "comment"
UPDATE listcolumn SET title = "process.comments" WHERE title = "process.correctionMessage";
UPDATE listcolumn SET title = "task.comments" WHERE title = "task.correctionMessage";

-- 3. Enable safe updates
--
SET SQL_SAFE_UPDATES = 1;
