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

-- 1. Add column for correction to task table
--
ALTER TABLE task ADD correction TINYINT(1);

-- 2. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 3. Update correction column with data from priority column
--
UPDATE task SET correction = 1 WHERE priority = 10;
UPDATE task SET correction = 0 WHERE priority <> 10;

-- 4. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 5. Drop priority column from task table
--
ALTER TABLE task DROP priority;
