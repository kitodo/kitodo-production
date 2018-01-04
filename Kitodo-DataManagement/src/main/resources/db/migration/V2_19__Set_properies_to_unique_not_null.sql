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

ALTER TABLE user ADD UNIQUE (login);

ALTER TABLE authorization ADD UNIQUE (title);
ALTER TABLE authorization MODIFY COLUMN title VARCHAR(255) NOT NULL;

ALTER TABLE userGroup ADD UNIQUE (title);
ALTER TABLE userGroup MODIFY COLUMN title VARCHAR(255) NOT NULL;

ALTER TABLE project ADD UNIQUE (title);
ALTER TABLE project MODIFY COLUMN title VARCHAR(255) NOT NULL;
