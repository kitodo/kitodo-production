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

--
-- Migration: Create table contentFolders_task_x_folder

-- 1. Create table contentFolders_task_x_folder

CREATE TABLE contentFolders_task_x_folder (
  task_id   int(11) NOT NULL
     COMMENT 'Task that triggers the generation of the contents of the folder',
  folder_id int(11) NOT NULL
     COMMENT 'Folder whose contents are to be generated in that task',
  PRIMARY KEY ( task_id, folder_id ),
  KEY FK_task_id   ( task_id ),
  KEY FK_folder_id ( folder_id ),
  CONSTRAINT FK_contentFolders_task_x_folder_task_id
    FOREIGN KEY ( task_id ) REFERENCES task ( id ),
  CONSTRAINT FK_contentFolders_task_x_folder_folder_id
    FOREIGN KEY ( folder_id ) REFERENCES folder ( id )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
