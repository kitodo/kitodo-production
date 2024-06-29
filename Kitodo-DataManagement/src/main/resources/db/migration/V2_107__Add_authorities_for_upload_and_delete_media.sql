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

-- Insert authorities for upload and delete media in metadata editor.

INSERT IGNORE INTO authority (title) VALUES ('uploadMedia_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('uploadMedia_clientAssignable');

INSERT IGNORE INTO authority (title) VALUES ('deleteMedia_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteMedia_clientAssignable');

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'uploadMedia_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'uploadMedia_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'deleteMedia_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'deleteMedia_clientAssignable';
