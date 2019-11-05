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

-- Add authority and role to view database statistics
INSERT IGNORE INTO authority (title) VALUES ('viewDatabaseStatistic_globalAssignable');
INSERT IGNORE INTO role (title, client_id) VALUES ('DatabaseStatistic', 1);
INSERT IGNORE INTO role_x_authority (role_id, authority_id) VALUES ((SELECT id from role WHERE title = 'DatabaseStatistic'), (SELECT id FROM authority WHERE title = 'viewDatabaseStatistic_globalAssignable'));
