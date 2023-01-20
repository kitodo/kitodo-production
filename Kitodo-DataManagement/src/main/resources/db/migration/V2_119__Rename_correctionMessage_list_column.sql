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

-- 1. Disable safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Rename list columns from "correctionMessage" to "comment"
UPDATE listcolumn SET title = "process.comments" WHERE title = "process.correctionMessage";
UPDATE listcolumn SET title = "task.comments" WHERE title = "task.correctionMessage";

-- 3. Enable safe updates
--
SET SQL_SAFE_UPDATES = 1;
