--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
