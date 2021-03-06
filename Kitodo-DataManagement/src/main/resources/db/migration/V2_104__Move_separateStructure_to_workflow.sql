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

-- 1. Add column for separateStructure to workflow table
--
ALTER TABLE workflow ADD separateStructure TINYINT(1);

-- 2. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 3. Set default value for this column
--

UPDATE workflow SET separateStructure = 0 WHERE id IS NOT NULL;

-- 4. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;

-- 5. Remove separateStructure column from task
ALTER TABLE task DROP COLUMN separateStructure;
