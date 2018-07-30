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

-- Create columns (boolean) copyFolder, (boolean) createFolder,
--     (double) derivative, (int) dpi, (double) imageScale, (int) imageSize,
--     (enum {ALL, EXISTING, NO, PREVIEW_IMAGE}) linkingMode

ALTER TABLE projectFileGroup
  ADD copyFolder   tinyint(1) NOT NULL DEFAULT 1
        COMMENT 'whether the folder is copied during export',
  ADD createFolder tinyint(1) NOT NULL DEFAULT 1
        COMMENT 'whether the folder is created with a new process',
  ADD derivative   double              DEFAULT NULL
        COMMENT 'the percentage of scaling for createDerivative()',
  ADD dpi          int(11)             DEFAULT NULL
        COMMENT 'the new DPI for changeDpi()',
  ADD imageScale   double              DEFAULT NULL
        COMMENT 'the percentage of scaling for getScaledWebImage()',
  ADD imageSize    int(11)             DEFAULT NULL
        COMMENT 'the new width in pixels for getSizedWebImage()',
  ADD linkingMode  varchar(13) NOT NULL COLLATE utf8mb4_unicode_ci DEFAULT 'ALL'
        COMMENT 'how to link the contents in a METS fileGrp',
  ADD CONSTRAINT CK_folder_linkingMode
        CHECK (linkingMode IN ('ALL', 'EXISTING', 'NO', 'PREVIEW_IMAGE'));

-- Set 'linkingMode' column to 'EXISTING' where 'folder' is not empty

UPDATE projectFileGroup SET linkingMode = 'EXISTING'
  WHERE id > 0 AND folder <> '';


-- Set 'linkingMode' to 'PREVIEW_IMAGE' where 'previewImage' = 1

UPDATE projectFileGroup SET linkingMode = 'PREVIEW_IMAGE'
  WHERE id > 0 AND previewImage = 1;


-- Rename columns 'name' => 'fileGroup', 'path' => 'urlStructure',
--     'folder' => 'path' (no column with same name as table)

ALTER TABLE projectFileGroup
  CHANGE name   fileGroup    varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  CHANGE path   urlStructure varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  CHANGE folder path         varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL;


-- Delete columns 'suffix' and 'previewImage'. 'previewImage' is now part of
--     'linkingMode'; 'suffix' depends on 'mimeType' and needs no extra storage.

ALTER TABLE projectFileGroup
  DROP previewImage,
  DROP suffix;


-- Rename table 'projectfilegroup' into 'folder'

ALTER TABLE projectFileGroup RENAME TO folder;


-- Rename foreign key constraints

ALTER TABLE folder
  DROP FOREIGN KEY `FK_projectFileGroup_project_id`,
  ADD CONSTRAINT `FK_folder_project_id` FOREIGN KEY (project_id) REFERENCES project (id);


-- Make sure we have LOCAL, MAX and THUMBS file groups
--
-- LOCAL file group was existing internally and hard-coded without being
-- visible in the database in the past; MAX and THUMBS are typically already
-- existing, but if not, they will be added here. They are pre-configured as
-- non-linking and non-export, so behaviour won’t be changed.

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'LOCAL'      as fileGroup,
         ''           as urlStructure,
         'image/tiff' as mimeType,
         ''           as path,          -- path is set later, see below
         project.id   as project_id,
         0            as copyFolder,
         'NO'         as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'LOCAL')
  WHERE folder.id IS NULL;

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'MAX'        as fileGroup,
         'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/' as urlStructure,
         'image/jpeg' as mimeType,
         ''           as path,          -- path is set later, see below
         project.id   as project_id,
         0            as copyFolder,
         'NO'         as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'MAX')
  WHERE folder.id IS NULL;

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'THUMBS'     as fileGroup,
         'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/' as urlStructure,
         'image/jpeg' as mimeType,
         ''           as path,          -- path is set later, see below
         project.id   as project_id,
         0            as copyFolder,
         'NO'         as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'THUMBS')
  WHERE folder.id IS NULL;


-- Fill in path column
--
-- In this example, we use Linux file separator and the _tif suffix for the
-- source images folder. You may want to adjust these values before migrating
-- your system.

UPDATE folder SET path = 'images/(processtitle)_tif'
  WHERE id > 0 AND fileGroup = 'LOCAL' AND path = '';

UPDATE folder SET path = 'pdf'
  WHERE id > 0 AND fileGroup = 'DOWNLOAD' AND path = '';

UPDATE folder SET path = 'ocr/alto'
  WHERE id > 0 AND fileGroup = 'FULLTEXT' AND path = '';

-- all remaining cases
UPDATE folder SET path = CONCAT('jpgs/', LOWER(fileGroup))
  WHERE id > 0 AND path = '';
