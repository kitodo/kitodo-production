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

-- Adjust task table to use scriptPath / scriptName to determine if task has a script.

-- 1. Rename typeAutomaticScriptPath column to scriptPath in task table.

ALTER TABLE task CHANGE typeAutomaticScriptPath scriptPath VARCHAR(255);

-- 2. Remove typeScriptStep column in task table.

ALTER TABLE task DROP COLUMN typeScriptStep;
