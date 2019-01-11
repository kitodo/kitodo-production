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
-- Migration: Remove column indexAction from client, user and role tables.
--

ALTER TABLE client DROP COLUMN indexAction;
ALTER TABLE role DROP COLUMN indexAction;
ALTER TABLE user DROP COLUMN indexAction;
