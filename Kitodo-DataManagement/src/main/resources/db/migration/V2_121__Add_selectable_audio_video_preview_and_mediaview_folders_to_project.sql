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

-- Add audio and video preview and media view folder to project

ALTER TABLE project
  ADD preview_video_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for video gallery preview',
  ADD CONSTRAINT FK_preview_video_folder_id
        FOREIGN KEY (preview_video_folder_id) REFERENCES folder (id),
  ADD mediaView_video_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for single video view',
  ADD CONSTRAINT FK_project_mediaView_video_folder_id
        FOREIGN KEY (mediaView_video_folder_id) REFERENCES folder (id),
  ADD preview_audio_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for audio gallery preview',
  ADD CONSTRAINT FK_preview_audio_folder_id
        FOREIGN KEY (preview_audio_folder_id) REFERENCES folder (id),
  ADD mediaView_audio_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for single audio view',
  ADD CONSTRAINT FK_project_mediaView_audio_folder_id
        FOREIGN KEY (mediaView_audio_folder_id) REFERENCES folder (id);
