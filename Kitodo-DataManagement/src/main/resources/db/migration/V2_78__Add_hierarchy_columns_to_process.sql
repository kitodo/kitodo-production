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

-- 1. Add columns for parent process id  and ordering to process table
--
ALTER TABLE process ADD parent_id INT(11);
ALTER TABLE process ADD ordering INT(6);

-- 2. Add foreign key to process table
-- --
ALTER TABLE process ADD CONSTRAINT `FK_process_parent_id`
    FOREIGN KEY (parent_id) REFERENCES process(id);
