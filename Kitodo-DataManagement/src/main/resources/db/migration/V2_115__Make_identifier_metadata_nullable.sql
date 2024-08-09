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
-- Migration: change 'identifier_metadata', 'metadata_record_id_xpath' and 'metadata_record_title_xpath' columns
--            to be nullable
ALTER TABLE importconfiguration MODIFY identifier_metadata varchar(255) NULL DEFAULT 'CatalogIDDigital';
ALTER TABLE importconfiguration MODIFY metadata_record_id_xpath varchar(255) NULL;
ALTER TABLE importconfiguration MODIFY metadata_record_title_xpath varchar(255) NULL;
