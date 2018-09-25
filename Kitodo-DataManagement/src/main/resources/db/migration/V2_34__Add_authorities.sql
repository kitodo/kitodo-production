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

-- Change ruleset authorities added in V2_28
UPDATE authority SET clientAssignable='1' WHERE title='viewAllRulesets';
UPDATE authority SET clientAssignable='1' WHERE title='viewRuleset';
UPDATE authority SET clientAssignable='1' WHERE title='addRuleset';
UPDATE authority SET clientAssignable='1' WHERE title='editRuleset';
UPDATE authority SET clientAssignable='1' WHERE title='deleteRuleset';

-- Change docket authorities added in V2_26
UPDATE authority SET clientAssignable='1' WHERE title='viewAllDockets';
UPDATE authority SET clientAssignable='1' WHERE title='viewDocket';
UPDATE authority SET clientAssignable='1' WHERE title='addDocket';
UPDATE authority SET clientAssignable='1' WHERE title='editDocket';
UPDATE authority SET clientAssignable='1' WHERE title='deleteDocket';

-- LdapGroup
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllLdapGroups', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteLdapGroup', '1', '0', '0');

-- LdapServer
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllLdapServers', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteLdapServer', '1', '0', '0');

-- Interaction with Index
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewIndex', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editIndex', '1', '0', '0');


-- Interaction with Process
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessMetaData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessStructureData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessPagination', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessImages', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessMetaData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessStructureData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessPagination', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessImages', '1', '1', '1');
