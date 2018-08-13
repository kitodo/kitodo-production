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
  ADD copyFolder   tinyint(1)  NOT NULL DEFAULT 1
        COMMENT 'whether the folder is copied during export',
  ADD createFolder tinyint(1)  NOT NULL DEFAULT 1
        COMMENT 'whether the folder is created with a new process',
  ADD derivative   double               DEFAULT NULL
        COMMENT 'the percentage of scaling for createDerivative()',
  ADD dpi          int(11)              DEFAULT NULL
        COMMENT 'the new DPI for changeDpi()',
  ADD imageScale   double               DEFAULT NULL
        COMMENT 'the percentage of scaling for getScaledWebImage()',
  ADD imageSize    int(11)              DEFAULT NULL
        COMMENT 'the new width in pixels for getSizedWebImage()',
  ADD linkingMode  varchar(13) NOT NULL DEFAULT 'ALL'
        COMMENT 'how to link the contents in a METS fileGrp',
  ADD CONSTRAINT CK_folder_linkingMode
        CHECK (linkingMode IN ('ALL', 'EXISTING', 'NO', 'PREVIEW_IMAGE'));


-- Make sure there are no NULL values in string fields. This should not be the
--     case, but may be on databases with a long version history.

UPDATE projectFileGroup SET name = '' WHERE id > 0 AND name IS NULL;
UPDATE projectFileGroup SET path = '' WHERE id > 0 AND path IS NULL;
UPDATE projectFileGroup SET folder = '' WHERE id > 0 AND folder IS NULL;


-- Set 'linkingMode' column to 'EXISTING' where 'folder' is not empty

UPDATE projectFileGroup SET linkingMode = 'EXISTING'
  WHERE id > 0 AND folder <> '';


-- Set 'linkingMode' to 'PREVIEW_IMAGE' where 'previewImage' = 1

UPDATE projectFileGroup SET linkingMode = 'PREVIEW_IMAGE'
  WHERE id > 0 AND previewImage = 1;


-- Rename columns 'name' => 'fileGroup', 'path' => 'urlStructure',
--     'folder' => 'path' (no column with same name as table)

ALTER TABLE projectFileGroup
  CHANGE name   fileGroup    varchar(255) NOT NULL DEFAULT ''
        COMMENT 'USE attribute for METS fileGroup',
  CHANGE path   urlStructure varchar(255) NOT NULL DEFAULT ''
        COMMENT 'Path where the folder is published on a web server',
  CHANGE folder path         varchar(255) NOT NULL DEFAULT ''
        COMMENT 'Path to the folder relative to the process directory, may contain variables';


-- Rename table 'projectfilegroup' into 'folder'

ALTER TABLE projectFileGroup RENAME TO folder;


-- Fill in path column
--
-- In this example, we use the Linux (and Java default) file separator and the
--     _tif suffix for the source images folder. You may want to adjust these
--     values before migrating your system.

UPDATE folder SET path = 'images/(processtitle)_tif'
  WHERE id > 0 AND fileGroup = 'LOCAL' AND path = '';

UPDATE folder SET path = 'pdf'
  WHERE id > 0 AND fileGroup = 'DOWNLOAD' AND path = '';

UPDATE folder SET path = 'ocr/alto'
  WHERE id > 0 AND fileGroup = 'FULLTEXT' AND path = '';

-- all remaining cases
UPDATE folder SET path = CONCAT('jpgs/', LOWER(fileGroup))
  WHERE id > 0 AND path = '';


-- Delete suffix in all cases it is equal to the configured file extension.

UPDATE folder
  SET suffix = ''
  WHERE id > 0 AND (
    mimeType = 'image/jpeg' AND suffix = 'jpg' OR
    mimeType = 'application/pdf' AND suffix = 'pdf' OR
    (mimeType = 'text/xml' OR mimeType = 'application/alto+xml') 
      AND suffix = 'xml' OR
    mimeType = 'image/tiff' AND suffix = 'tif' OR
    mimeType = 'image/png' AND suffix = 'png' OR
    mimeType = 'image/jp2' AND suffix = 'jp2' OR
    mimeType = 'image/bmp' AND suffix = 'bmp' OR
    mimeType = 'image/gif' AND suffix = 'gif'
  );


-- In the remaining cases, replace the configured file extension by the
-- replcement character if the suffix ends with it.

UPDATE folder
  SET suffix = concat(substring(suffix, 1, char_length(suffix) - 4), '.*')
  WHERE id > 0 AND (
    mimeType = 'image/jpeg' AND right(suffix, 4) = '.jpg' OR
    mimeType = 'application/pdf' AND suffix = '.pdf' OR
    (mimeType = 'text/xml' OR mimeType = 'application/alto+xml') 
      AND right(suffix, 4) = '.xml' OR
    mimeType = 'image/tiff' AND right(suffix, 4) = '.tif' OR
    mimeType = 'image/png' AND right(suffix, 4) = '.png' OR
    mimeType = 'image/jp2' AND right(suffix, 4) = '.jp2' OR
    mimeType = 'image/bmp' AND right(suffix, 4) = '.bmp' OR
    mimeType = 'image/gif' AND right(suffix, 4) = '.gif'
  );
  
  
-- To attach the special case suffixes to the path column, make sure the path
--     ends with the file separator. In this example, we use the Linux (and
--     Java default) file separator. You may want to adjust these values before
--     migrating your system.

UPDATE folder
  SET path = concat(path, '/')
  WHERE id > 0 AND suffix <> '' AND right(path, 1) <> '/';


-- Attach the special case suffixes to the path column.

UPDATE folder
  SET path = concat(path, concat('*.', suffix))
  WHERE id > 0 AND suffix <> '';


-- Delete columns 'suffix' and 'previewImage'. 'previewImage' is now part of
--     'linkingMode'; 'suffix' depends on 'mimeType' and needs no extra storage.

ALTER TABLE folder
  DROP previewImage,
  DROP suffix;


-- Rename foreign key constraints

ALTER TABLE folder
  DROP FOREIGN KEY `FK_projectFileGroup_project_id`,
  ADD CONSTRAINT `FK_folder_project_id` FOREIGN KEY (project_id) REFERENCES project (id);
