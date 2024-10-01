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

-- allow null in column task_id of table dataeditorsetting to store task-independent layout
ALTER TABLE dataeditor_setting MODIFY COLUMN task_id INT(11) NULL;

SET SQL_SAFE_UPDATES = 1;