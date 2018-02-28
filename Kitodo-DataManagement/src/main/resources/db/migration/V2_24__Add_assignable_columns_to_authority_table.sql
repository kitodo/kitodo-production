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

-- Add assignable columns to authority table

ALTER TABLE authority
  ADD COLUMN globalAssignable tinyint(1) DEFAULT 1,
  ADD COLUMN clientAssignable tinyint(1) DEFAULT 1,
  ADD COLUMN projectAssignable tinyint(1) DEFAULT 1;
