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

-- Convert the char set to uft8mb4 on all lately inserted tables and their columns.
-- This does not change the global setting for database.

ALTER TABLE project_x_template CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
