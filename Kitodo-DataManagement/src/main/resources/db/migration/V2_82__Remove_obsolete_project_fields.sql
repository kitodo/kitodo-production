--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- For the full copyright and license information, please read the
-- GPL3-License.txt file that was distributed with this source code.
--

-- Remove obsolete project fields

ALTER TABLE project DROP COLUMN metsDigiprovReferenceAnchor;
ALTER TABLE project DROP COLUMN metsDigiprovPresentationAnchor;
ALTER TABLE project DROP COLUMN metsPointerPathAnchor;

ALTER TABLE project DROP COLUMN dmsImportImagesPath;
ALTER TABLE project DROP COLUMN dmsImportSuccessPath;
ALTER TABLE project DROP COLUMN dmsImportErrorPath;
