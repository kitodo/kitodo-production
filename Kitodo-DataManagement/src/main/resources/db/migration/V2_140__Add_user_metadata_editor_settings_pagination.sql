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

-- Add column "show_logical_page_number_below_thumbnail" to "user" table
ALTER TABLE user ADD show_logical_page_number_below_thumbnail TINYINT(1) DEFAULT 0;

-- Add column "default_pagination_type" to "user" table
ALTER TABLE user ADD default_pagination_type VARCHAR(12) DEFAULT NULL;
