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

--
-- Migration: Add column client_id to docket and ruleset tables

-- 1. Add columns
--

ALTER TABLE docket ADD client_id INT(11);
ALTER TABLE ruleset ADD client_id INT(11);

-- 2. Add foreign keys
--

ALTER TABLE docket add constraint `FK_docket_client_id`
foreign key (client_id) REFERENCES client(id);

ALTER TABLE ruleset add constraint `FK_ruleset_client_id`
foreign key (client_id) REFERENCES client(id);
