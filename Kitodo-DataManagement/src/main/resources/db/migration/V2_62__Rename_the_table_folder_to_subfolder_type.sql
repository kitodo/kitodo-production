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
-- Migration: Rename the table folder to subfolder type
--
-- 62. Rename the table folder to subfolder type
--

USE kitodo;
CREATE TABLE subfolder_type AS SELECT * FROM folder;
DROP TEMPORARY TABLE IF EXISTS folder;
