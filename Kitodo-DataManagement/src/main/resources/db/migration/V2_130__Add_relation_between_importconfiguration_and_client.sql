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
-- Migration: Create relation between ImportConfigurations and Clients
--

-- 1. creating cross table
CREATE TABLE IF NOT EXISTS client_x_importconfiguration (
    client_id INT(11) NOT NULL,
    importconfiguration_id INT(11) NOT NULL,
    PRIMARY KEY ( client_id, importconfiguration_id ),
    KEY FK_client_x_importconfiguration_client_id (client_id),
    KEY FK_client_x_importconfiguration_importconfiguration_id (importconfiguration_id),
    CONSTRAINT FK_client_x_importconfiguration_client_id FOREIGN KEY (client_id) REFERENCES client(id),
    CONSTRAINT FK_client_x_importconfiguration_importconfiguration_id FOREIGN KEY (importconfiguration_id) REFERENCES importconfiguration(id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. initially map all clients to all import configurations to retain status quo
INSERT INTO client_x_importconfiguration SELECT client.id, importconfiguration.id FROM client, importconfiguration;
