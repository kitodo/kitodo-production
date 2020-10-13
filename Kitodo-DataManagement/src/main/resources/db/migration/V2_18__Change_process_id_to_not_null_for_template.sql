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
-- Migration: Changing the columns for process id in template table to not null
--

SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE template MODIFY process_id INT(11) NOT NULL;
SET FOREIGN_KEY_CHECKS = 1;
