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

-- 1. Add columns for generate and validate image to task table
--
ALTER TABLE task
    ADD COLUMN typeGenerateImages TINYINT(1),
    ADD COLUMN typeValidateImages TINYINT(1);

-- 2. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 3. Set default value for this columns
--

UPDATE task SET typeGenerateImages = 0, typeValidateImages = 0 WHERE id IS NOT NULL;

-- 4. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;
