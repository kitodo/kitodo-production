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

-- 2. Replace old sortHelperStatus from "100000000" to "100000000000"
UPDATE process SET sortHelperStatus = "100000000000" WHERE sortHelperStatus = "100000000";

-- 3. Enable safe updates
--
SET SQL_SAFE_UPDATES = 1;
