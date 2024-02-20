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

-- Add column "paginate_from_first_page_by_default" to "user" table
ALTER TABLE user ADD paginate_from_first_page_by_default TINYINT(1) DEFAULT 0;
