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
-- Migration: Adjust database to Data Management Module
--
-- 1. Rename boolean columns' names
--

ALTER TABLE process
  CHANGE isTemplate template TINYINT(1),
  CHANGE isChoiceListShown inChoiceListShown TINYINT(1);

ALTER TABLE processProperty
  CHANGE isObligatory obligatory TINYINT(1);

ALTER TABLE templateProperty
  CHANGE isObligatory obligatory TINYINT(1);

ALTER TABLE user
  CHANGE isActive active TINYINT(1),
  CHANGE isVisible visible varchar(255);

ALTER TABLE userProperty
  CHANGE isObligatory obligatory TINYINT(1);

ALTER TABLE workpieceProperty
  CHANGE isObligatory obligatory TINYINT(1);

--
-- 2. Rename boolean columns' names
--

SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE workpiece
  CHANGE id id INT(11) NOT NULL AUTO_INCREMENT;
SET FOREIGN_KEY_CHECKS = 1;
