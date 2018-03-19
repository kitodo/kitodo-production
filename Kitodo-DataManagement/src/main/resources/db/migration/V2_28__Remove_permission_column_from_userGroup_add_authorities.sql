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

ALTER TABLE userGroup DROP COLUMN permission;

# Ruleset
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllRulesets', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewRuleset', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addRuleset', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editRuleset', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteRuleset', '1', '0', '0');
