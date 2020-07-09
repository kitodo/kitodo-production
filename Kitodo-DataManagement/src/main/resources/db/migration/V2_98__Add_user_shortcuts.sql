--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- For the full copyright and license information, please read the
-- GPL3-License.txt file that was distributed with this source code.

ALTER TABLE user ADD COLUMN shortcuts VARCHAR(1024) DEFAULT '{"detailView":"Control Shift BracketRight","help":"Shift Minus","nextItem":"Control ArrowDown","nextItemMulti":"Control Shift ArrowDown","previousItem":"Control ArrowUp","previousItemMulti":"Control Shift ArrowUp","structuredView":"Control Shift Slash"}';
