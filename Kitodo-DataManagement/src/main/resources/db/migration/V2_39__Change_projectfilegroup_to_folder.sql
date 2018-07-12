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

ALTER TABLE projectfilegroup
  ADD copyFolder   tinyint(1) NOT NULL DEFAULT 1
        COMMENT 'whether the folder is copied during export',
      createFolder tinyint(1) NOT NULL DEFAULT 1
        COMMENT 'whether the folder is created with a new process',
      derivative   double              DEFAULT NULL
        COMMENT 'the percentage of scaling for createDerivative()',
      dpi          int(11)             DEFAULT NULL
        COMMENT 'the new DPI for changeDpi()',
      imageScale   double              DEFAULT NULL
        COMMENT 'the percentage of scaling for getScaledWebImage()',
      imageSize    int(11)             DEFAULT NULL
        COMMENT 'the new width in pixels for getSizedWebImage()',
      linkingMode  varchar(13) NOT NULL COLLATE utf8mb4_unicode_ci DEFAULT 'ALL'
        COMMENT 'how to link the contents in a METS fileGrp';
        
ALTER TABLE projectfilegroup ADD CONSTRAINT CK_folder_linkingMode
  CHECK (linkingMode IN ('ALL', 'EXISTING', 'NO', 'PREVIEW_IMAGE'));

-- Set 'linkingMode' column to 'EXISTING' where 'folder' is not empty

UPDATE projectfilegroup SET linkingMode = 'EXISTING' WHERE folder <> '';


-- Set 'linkingMode' to 'PREVIEW_IMAGE' where 'previewImage' = 1

UPDATE projectfilegroup SET linkingMode = 'PREVIEW_IMAGE' WHERE previewImage = 1;


-- Rename columns 'name' => 'fileGroup', 'path' => 'urlStructure',
--     'folder' => 'path' (no column with same name as table)

ALTER TABLE projectfilegroup
  CHANGE name   fileGroup    varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
         path   urlStructure varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
         folder path         varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL;


-- Delete columns 'suffix' and 'previewImage'. 'previewImage' is now part of
--     'linkingMode'; 'suffix' depends on 'mimeType' and needs no extra storage.

ALTER TABLE projectfilegroup DROP previewImage, suffix;


-- Rename table 'projectfilegroup' into 'folder'

ALTER TABLE projectfilegroup RENAME TO folder;


-- Fill in path column
--
-- In this example, we use Linux file separator and the _tif suffix for the
-- source images folder. You may want to adjust these values before migrating
-- your system.

UPDATE folder SET path = 'images/(processtitle)_tif'
  WHERE fileGroup = 'LOCAL' AND path = '';

UPDATE folder SET path = 'pdf'
  WHERE fileGroup = 'DOWNLOAD' AND path = '';

UPDATE folder SET path = 'ocr/alto'
  WHERE fileGroup = 'FULLTEXT' AND path = '';

-- all remaining cases
UPDATE folder SET path = CONCAT('jpgs/', LOWER(fileGroup))
  WHERE path = '';
