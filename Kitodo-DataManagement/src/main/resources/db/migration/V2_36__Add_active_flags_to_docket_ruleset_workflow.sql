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

ALTER TABLE docket ADD COLUMN active TINYINT(1) DEFAULT 1;

ALTER TABLE ruleset ADD COLUMN active TINYINT(1) DEFAULT 1;

ALTER TABLE workflow CHANGE archived active TINYINT(1) DEFAULT 1;
