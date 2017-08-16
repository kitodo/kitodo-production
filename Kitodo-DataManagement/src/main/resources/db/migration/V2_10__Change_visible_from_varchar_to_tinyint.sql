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

--
-- Migration: Change column visible in user table from varchar to tinyint

-- 1. Update column visible to store correct tinyint values

UPDATE user SET visible = '1' WHERE trim(visible) != 'deleted';
UPDATE user SET visible = '0' WHERE trim(visible) = 'deleted';

-- 2. Change column visible to store correct tinyint values

ALTER TABLE user
  MODIFY visible TINYINT(1) NOT NULL;
