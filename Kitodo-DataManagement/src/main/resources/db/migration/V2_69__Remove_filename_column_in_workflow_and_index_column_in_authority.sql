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
-- Migration: Remove column fileName from workflow table, indexAction from authority table and make title column unique.
--

ALTER TABLE workflow DROP COLUMN fileName;

ALTER TABLE authority DROP COLUMN indexAction;

ALTER TABLE workflow ADD UNIQUE (title);
