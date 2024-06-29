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

-- Add relationship between template and client and add dummy client to templates.

-- 1. Add column for client id to template table
--
ALTER TABLE template ADD client_id INT(11);

-- 2. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 3. Assign the client to every template which has no client
--
UPDATE template
SET template.client_id = 1 WHERE template.client_id IS NULL;

-- 4. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 5. Add foreign key to template table
--
ALTER TABLE template ADD CONSTRAINT `FK_template_client_id`
foreign key (client_id) REFERENCES client(id);
