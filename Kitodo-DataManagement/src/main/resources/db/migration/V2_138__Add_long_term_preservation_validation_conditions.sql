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

-- Add table "ltp_validation_configuration"
CREATE TABLE IF NOT EXISTS ltp_validation_configuration
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT 'title of ltp validation configuration',
    mimeType VARCHAR(255) NOT NULL COMMENT 'mime type of files this configuration can be applied to',
    requireNoErrorToUploadImage TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'whether to prevent image upload if there is an error',
    requireNoErrorToFinishTask TINYINT(1) NOT NULL DEFAULT 0  COMMENT 'whether to prevent finishing task if there is an error',
    PRIMARY KEY(id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Add column "ltp_validation_configuration_id" to "folder" table
ALTER TABLE folder 
  ADD ltp_validation_configuration_id INT(11) DEFAULT NULL
     COMMENT 'id of validation configuration that is used for this folder',
  ADD KEY FK_folder_ltp_validation_configuration_id (ltp_validation_configuration_id),
  ADD CONSTRAINT FK_folder_ltp_validation_configuration_id
        FOREIGN KEY (ltp_validation_configuration_id) 
        REFERENCES ltp_validation_configuration (id);

-- Add table "ltp_validation_condition"
CREATE TABLE IF NOT EXISTS ltp_validation_condition
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    sorting INT(11) DEFAULT 0 
        COMMENT 'sorting value to persist order amongst validation conditions',
    property VARCHAR(255) NOT NULL 
        COMMENT 'property name of validation condition',
    operation VARCHAR(255) NOT NULL 
        COMMENT 'operation type of validation condition',
    severity VARCHAR(255) NOT NULL 
        COMMENT 'severity level of validation condition',
    ltp_validation_configuration_id INT(11) NOT NULL 
        COMMENT 'id of ltp validation configuration this condition belongs to',
    PRIMARY KEY(id),
    KEY FK_ltp_validation_condition_ltp_validation_configuration_id (ltp_validation_configuration_id),
    CONSTRAINT FK_ltp_validation_condition_ltp_validation_configuration_id 
        FOREIGN KEY (ltp_validation_configuration_id) REFERENCES ltp_validation_configuration (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Add table "ltp_validation_condition_value"
CREATE TABLE IF NOT EXISTS ltp_validation_condition_value
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    sorting INT(11) DEFAULT 0
        COMMENT 'sorting value to persist order amongst validation condition values',
    value VARCHAR(255) NOT NULL
        COMMENT 'comparison value of validation condition',
    ltp_validation_condition_id INT(11) NOT NULL
        COMMENT 'id of validation condition this value belongs to',
    PRIMARY KEY(id),
    KEY FK_ltp_validation_condition_value_ltp_validation_condition_id (ltp_validation_condition_id),
    CONSTRAINT FK_ltp_validation_condition_value_ltp_validation_condition_id 
        FOREIGN KEY (ltp_validation_condition_id) REFERENCES ltp_validation_condition (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- add authorities
INSERT IGNORE INTO authority (title) VALUES ('addLtpValidationConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editLtpValidationConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewLtpValidationConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllLtpValidationConfigurations_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteLtpValidationConfiguration_clientAssignable');