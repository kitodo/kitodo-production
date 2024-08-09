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

-- Remove obsolete project fields

ALTER TABLE project DROP COLUMN metsDigiprovReferenceAnchor;
ALTER TABLE project DROP COLUMN metsDigiprovPresentationAnchor;
ALTER TABLE project DROP COLUMN metsPointerPathAnchor;

ALTER TABLE project DROP COLUMN dmsImportImagesPath;
ALTER TABLE project DROP COLUMN dmsImportSuccessPath;
ALTER TABLE project DROP COLUMN dmsImportErrorPath;
