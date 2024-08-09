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

-- Add relationship between workflow and client and add dummy client to workflows.

-- 1. Add column for client id to workflow table
--
ALTER TABLE workflow ADD client_id INT(11);

-- 2. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 3. Assign the client to every workflow which has no client
--
UPDATE workflow workflowTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET workflowTable.client_id = dummyClient.id WHERE workflowTable.client_id IS NULL;

-- 4. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 5. Add foreign key to workflow table
--
ALTER TABLE workflow ADD CONSTRAINT `FK_workflow_client_id`
foreign key (client_id) REFERENCES client(id);
