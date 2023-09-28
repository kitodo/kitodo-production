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

SET SQL_SAFE_UPDATES = 0;

-- add authorities/permission for renaming media files
INSERT IGNORE INTO authority (title) VALUES ('renameMedia_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('renameMedia_globalAssignable');

-- add "filenameLength" column to project table
ALTER TABLE project ADD filename_length INT DEFAULT 8;

SET SQL_SAFE_UPDATES = 1;
