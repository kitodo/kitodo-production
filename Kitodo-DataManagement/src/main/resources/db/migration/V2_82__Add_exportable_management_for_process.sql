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

-- 1. Add column for exportable to process table
--
ALTER TABLE process ADD exportable TINYINT(1) NOT NULL DEFAULT 0;

-- 2. Add column for typeSetExportableToTrue to task table
--
ALTER TABLE task ADD typeSetExportableToTrue TINYINT(1) NOT NULL DEFAULT 0;
