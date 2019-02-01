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

-- Add column configuration authorities
INSERT INTO authority (title) VALUES ('configureColumns_globalAssignable');
INSERT INTO authority (title) VALUES ('configureColumns_clientAssignable');

-- Add role for new authority to default Client ('Client_ChangeMe')
INSERT INTO role (title, client_id) VALUES ('ConfigureColumns', 1);

-- Add column configuration authorities to corresponding role
INSERT INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id from role WHERE title = 'ConfigureColumns'), (SELECT id FROM  authority WHERE title = 'configureColumns_globalAssignable'));
INSERT INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id from role WHERE title = 'ConfigureColumns'), (SELECT id FROM  authority WHERE title = 'configureColumns_clientAssignable'));