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
-- Update values of columns for parallel tasks.

-- 1. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Update columns
--
UPDATE task SET concurrent = 1 WHERE concurrent IS NULL;
UPDATE task SET last = 0 WHERE last IS NULL;

-- 3. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;
