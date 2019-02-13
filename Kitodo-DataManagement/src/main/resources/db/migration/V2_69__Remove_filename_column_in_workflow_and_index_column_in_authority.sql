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
-- Migration: Remove column fileName from workflow table, indexAction from authority table and make title column unique.
--

ALTER TABLE workflow DROP COLUMN fileName;

ALTER TABLE authority DROP COLUMN indexAction;

ALTER TABLE workflow ADD UNIQUE (title);
