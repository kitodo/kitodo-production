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

-- Convert the char set to uft8mb4 on all lately inserted tables and their columns.
-- This does not change the global setting for database.

ALTER TABLE project_x_template CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
