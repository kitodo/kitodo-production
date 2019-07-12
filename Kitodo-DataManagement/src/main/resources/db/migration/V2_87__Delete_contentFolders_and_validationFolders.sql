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
-- Migration: Delete tables contentFolders_task_x_folder and
--            validationFolders_task_x_folder

-- 1. Delete tables contentFolders_task_x_folder and
--    validationFolders_task_x_folder

DROP TABLE contentFolders_task_x_folder,
           validationFolders_task_x_folder;
