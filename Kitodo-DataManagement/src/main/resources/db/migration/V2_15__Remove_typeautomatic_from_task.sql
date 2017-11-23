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

-- Copy data and next remove unused column.

-- 1. Copy data from typeAutomatic column to the editType column.

UPDATE task SET editType = 4 WHERE typeAutomatic = 1;

-- 2. Remove typeAutomatic column from Task table.

ALTER TABLE task DROP COLUMN typeAutomatic;
