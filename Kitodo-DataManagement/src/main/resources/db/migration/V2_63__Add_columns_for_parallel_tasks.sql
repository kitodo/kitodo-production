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
-- Add columns for parallel tasks to task table.
ALTER TABLE task ADD concurrent TINYINT(1);
ALTER TABLE task ADD last TINYINT(1);
