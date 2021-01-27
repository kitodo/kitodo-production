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
-- Migration: Create table for metadata editor settings.
--
-- 1. Add table
--

CREATE TABLE IF NOT EXISTS dataeditor_setting(
    id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) NOT NULL,
    task_id INT(11) NOT NULL,
    structure_width FLOAT(3) DEFAULT NULL,
    metadata_width FLOAT(3) DEFAULT NULL,
    gallery_width FLOAT(3) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY FK_dataeditorsetting_user_id (user_id),
    KEY FK_dataeditorsetting_task_id (task_id),
    CONSTRAINT FK_dataeditorsetting_user_id
        FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT  FK_dataeditorsetting_task_id
        FOREIGN KEY (task_id) REFERENCES  task (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;
