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
-- Migration: Add example workflow - equal in tasks list to template.
--

INSERT INTO workflow (`title`, `fileName`, `active`, `ready`, `indexAction`)
VALUES ('Example_Workflow', 'Example_Workflow', 1, 1, 'INDEX');
