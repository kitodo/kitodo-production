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

-- Adjust task table to use scriptPath / scriptName to determine if task has a script.

-- 1. Rename typeAutomaticScriptPath column to scriptPath in task table.

ALTER TABLE task CHANGE typeAutomaticScriptPath scriptPath VARCHAR(255);

-- 2. Remove typeScriptStep column in task table.

ALTER TABLE task DROP COLUMN typeScriptStep;
