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

-- Replace projectIsArchived column with active column and flip its value.

-- 1. Rename projectIsArchived column to active.

ALTER TABLE project CHANGE projectIsArchived active TINYINT(1);

-- 2. Flip the value of the entries.

UPDATE project SET active = NOT active;
