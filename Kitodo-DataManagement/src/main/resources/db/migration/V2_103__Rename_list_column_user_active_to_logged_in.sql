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

-- 1. Switch off safe updates
--
SET SQL_SAFE_UPDATES = 0;

-- 2. Rename user list column "active" to "loggedIn"
--
UPDATE listColumn SET title = "user.loggedIn" WHERE title = "user.active";

-- 3. Switch on safe updates
--
SET SQL_SAFE_UPDATES = 1;
