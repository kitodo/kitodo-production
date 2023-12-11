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
-- Migration: Add column for state of preview tooltip to project table.
ALTER TABLE project ADD preview_tooltip TINYINT(1) NOT NULL DEFAULT 0;

--
-- Migration: Add column for state if the media view should be displayed preview tooltip to project table.
ALTER TABLE project ADD preview_tooltip_media_view TINYINT(1) NOT NULL DEFAULT 0;
