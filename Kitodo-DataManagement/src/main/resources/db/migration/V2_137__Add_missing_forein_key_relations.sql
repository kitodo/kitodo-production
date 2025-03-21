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

SET SQL_SAFE_UPDATES = 0;

-- importconfiguration table
ALTER TABLE importconfiguration ADD KEY FK_importconfiguration_searchfield_id(default_searchfield_id);
ALTER TABLE importconfiguration ADD CONSTRAINT FK_importconfiguration_searchfield_id FOREIGN KEY (default_searchfield_id) REFERENCES searchfield(id);

ALTER TABLE importconfiguration ADD KEY FK_importconfiguration_searchfield_identifier_id(identifier_searchfield_id);
ALTER TABLE importconfiguration ADD CONSTRAINT FK_importconfiguration_searchfield_identifier_id FOREIGN KEY (identifier_searchfield_id) REFERENCES searchfield(id);

ALTER TABLE importconfiguration ADD KEY FK_importconfiguration_searchfield_parent_searchfield_id(parent_searchfield_id);
ALTER TABLE importconfiguration ADD CONSTRAINT FK_importconfiguration_searchfield_parent_searchfield_id FOREIGN KEY (parent_searchfield_id) REFERENCES searchfield(id);

-- process table
ALTER TABLE process ADD KEY FK_process_import_configuration_id(import_configuration_id);
ALTER TABLE process ADD CONSTRAINT FK_process_import_configuration_id FOREIGN KEY (import_configuration_id) REFERENCES importconfiguration (id);

-- user table
ALTER TABLE user ADD KEY FK_user_default_client_id(default_client_id);
ALTER TABLE user ADD CONSTRAINT FK_user_default_client_id FOREIGN KEY (default_client_id) REFERENCES client(id);

SET SQL_SAFE_UPDATES = 1;
