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
