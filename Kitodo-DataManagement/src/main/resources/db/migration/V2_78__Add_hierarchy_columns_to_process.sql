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

-- 1. Add columns for parent process id  and ordering to process table
--
ALTER TABLE process ADD parent_id INT(11);
ALTER TABLE process ADD ordering INT(6);

-- 2. Add foreign key to process table
-- --
ALTER TABLE process ADD CONSTRAINT `FK_process_parent_id`
    FOREIGN KEY (parent_id) REFERENCES process(id);
