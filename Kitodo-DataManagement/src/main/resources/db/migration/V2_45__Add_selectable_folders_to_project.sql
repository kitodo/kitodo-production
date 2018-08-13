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

-- Make sure we have LOCAL, MAX and THUMBS file groups
--
-- LOCAL file group was existing internally and hard-coded without being
-- visible in the database in the past; MAX and THUMBS are typically already
-- existing, but if not, they will be added here. They are pre-configured as
-- non-linking and non-export, so behaviour won’t be changed.
--
-- In this example, we use the Linux (and Java default) file separator and the
-- _tif suffix for the source images folder. You may want to adjust these
-- values before migrating your system.

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'LOCAL'                     as fileGroup,
         ''                          as urlStructure,
         'image/tiff'                as mimeType,
         'images/(processtitle)_tif' as path,
         project.id                  as project_id,
         0                           as copyFolder,
         'NO'                        as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'LOCAL')
  WHERE folder.id IS NULL;

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'MAX'        as fileGroup,
         'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/' as urlStructure,
         'image/jpeg' as mimeType,
         'jpgs/max'   as path,
         project.id   as project_id,
         0            as copyFolder,
         'NO'         as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'MAX')
  WHERE folder.id IS NULL;

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'THUMBS'      as fileGroup,
         'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/' as urlStructure,
         'image/jpeg'  as mimeType,
         'jpgs/thumbs' as path,
         project.id    as project_id,
         0             as copyFolder,
         'NO'          as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'THUMBS')
  WHERE folder.id IS NULL;


-- Create columns (Folder) generatorSource, (Folder) mediaView,
--     (Folder) preview

ALTER TABLE project
  ADD generatorSource_folder_id int(11) DEFAULT NULL
        COMMENT 'folder with templates to create derived media from',
  ADD CONSTRAINT CK_project_generatorSource_folder_id
        FOREIGN KEY (generatorSource_folder_id) REFERENCES folder (id),
  ADD mediaView_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for single medium view',
  ADD CONSTRAINT CK_project_mediaView_folder_id
        FOREIGN KEY (mediaView_folder_id) REFERENCES folder (id),
  ADD preview_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for gallery preview',
  ADD CONSTRAINT CK_project_preview_folder_id
        FOREIGN KEY (preview_folder_id) REFERENCES folder (id);


-- Set 'generatorSource' column to corresponding 'LOCAL' file group

SET SQL_SAFE_UPDATES=0;
UPDATE project JOIN folder ON folder.project_id = project.id
  SET project.generatorSource_folder_id = folder.id
  WHERE project.id > 0 AND folder.fileGroup = 'LOCAL';


-- Set 'mediaView' column to corresponding 'MAX' file group

UPDATE project JOIN folder ON folder.project_id = project.id
  SET project.mediaView_folder_id = folder.id
  WHERE project.id > 0 AND folder.fileGroup = 'MAX';


-- Set 'preview' column to corresponding 'THUMBS' file group

UPDATE project JOIN folder ON folder.project_id = project.id
  SET project.preview_folder_id = folder.id
  WHERE project.id > 0 AND folder.fileGroup = 'THUMBS';
SET SQL_SAFE_UPDATES=1;
