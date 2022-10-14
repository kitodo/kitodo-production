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
-- Migration: Add 'urlparameter' table used for ImportConfigurations of type 'CUSTOM'
--

-- Add table "urlparameter"
CREATE TABLE IF NOT EXISTS urlparameter
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    importconfiguration_id INT(11) NOT NULL,
    parameter_key varchar(255) NOT NULL,
    parameter_value varchar(255) NOT NULL,
    PRIMARY KEY(id),
    KEY FK_urlparameter_importconfiguration_id (importconfiguration_id),
    CONSTRAINT FK_urlparameter_importconfiguration_id
        FOREIGN KEY (importconfiguration_id) REFERENCES importconfiguration (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;
