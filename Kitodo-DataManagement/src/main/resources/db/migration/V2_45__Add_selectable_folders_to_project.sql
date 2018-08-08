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
