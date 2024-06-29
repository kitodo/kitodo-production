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

-- 1. Add a dummy client
INSERT INTO client (`name`, `indexAction`) VALUES ('Client_ChangeMe', 'INDEX');

-- 2. Switch off safe updates
SET SQL_SAFE_UPDATES = 0;

-- 3. Assign the client to every project which has no client
UPDATE project projectTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET projectTable.client_id = dummyClient.id WHERE projectTable.client_id IS NULL;

-- 4. Switch on safe updates
SET SQL_SAFE_UPDATES = 1;

-- 5. Set the client_id column of project table to NOT NULL
ALTER TABLE project CHANGE COLUMN `client_id` `client_id` INT(11) NOT NULL;
