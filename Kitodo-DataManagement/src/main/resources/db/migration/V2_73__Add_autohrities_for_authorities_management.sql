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

-- Insert authorities for authorities management.

INSERT IGNORE INTO authority (title) VALUES ('addAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllAuthorities_globalAssignable');

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Administration'), (SELECT id FROM authority WHERE title = 'viewAllAuthorities_globalAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title LIKE '%Authority_globalAssignable';
