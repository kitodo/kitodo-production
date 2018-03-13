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

-- Replace projectIsArchived column with active column and flip its value.

-- 1. Rename projectIsArchived column to active.

ALTER TABLE project CHANGE projectIsArchived active TINYINT(1);

-- 2. Flip the value of the entries.

UPDATE project SET active = NOT active;
