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
-- Migration: Create table for import configurations.
--
-- 1. Add table "mappingfile"
CREATE TABLE IF NOT EXISTS mappingfile (
    id INT(11) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    file varchar(255) NOT NULL,
    input_metadata_format varchar(255) NOT NULL,
    output_metadata_format varchar(255) NOT NULL,
    PRIMARY KEY(id)
    ) DEFAULT CHARACTER SET = utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Add table "importconfiguration"
CREATE TABLE IF NOT EXISTS importconfiguration
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    configuration_type varchar(255) NOT NULL,
    prestructured_import tinyint(1) DEFAULT 0,
    interface_type varchar(255) DEFAULT NULL,
    return_format varchar(255) DEFAULT NULL,
    metadata_format varchar(255) DEFAULT NULL,
    default_import_depth INT(11) DEFAULT 2 NULL,
    parent_element_xpath varchar(255) DEFAULT NULL,
    parent_element_type varchar(255) DEFAULT NULL,
    parent_element_trim_mode varchar(255) DEFAULT NULL,
    default_searchfield_id INT(11),
    identifier_searchfield_id INT(11),
    parent_searchfield_id INT(11),
    default_templateprocess_id INT(11),
    parent_templateprocess_id INT(11),
    parent_mappingfile_id INT(11),
    scheme varchar(255) DEFAULT NULL,
    host varchar(255) DEFAULT NULL,
    port INT(11) DEFAULT NULL,
    path varchar(255) DEFAULT NULL,
    anonymous_access tinyint(1) DEFAULT 0,
    username varchar(255) DEFAULT NULL,
    password varchar(255) DEFAULT NULL,
    query_delimiter varchar(255) DEFAULT NULL,
    item_field_xpath varchar(255) DEFAULT NULL,
    item_field_owner_sub_path varchar(255) DEFAULT NULL,
    item_field_owner_metadata varchar(255) DEFAULT NULL,
    item_field_signature_sub_path varchar(255) DEFAULT NULL,
    item_field_signature_metadata varchar(255) DEFAULT NULL,
    id_prefix varchar(255) DEFAULT NULL,
    sru_version varchar(255) DEFAULT NULL,
    sru_record_schema varchar(255) DEFAULT NULL,
    oai_metadata_prefix varchar(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY FK_importconfiguration_process_id (default_templateprocess_id),
    CONSTRAINT FK_importconfiguration_process_id
        FOREIGN KEY (default_templateprocess_id) REFERENCES process (id),
    KEY FK_parent_mappingfile_id (parent_mappingfile_id),
    CONSTRAINT FK_parent_mappingfile_id
        FOREIGN KEY (parent_mappingfile_id) REFERENCES mappingfile (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;


-- 3. Add table "searchfield"
CREATE TABLE IF NOT EXISTS searchfield
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    importconfiguration_id INT(11) NOT NULL,
    field_label varchar(255) NOT NULL,
    field_value varchar(255) NOT NULL,
    displayed tinyint(1) DEFAULT 1,
    parent_element tinyint(1) DEFAULT 0,
    PRIMARY KEY(id),
    KEY FK_searchfield_importconfiguration_id (importconfiguration_id),
    CONSTRAINT FK_searchfield_importconfiguration_id
        FOREIGN KEY (importconfiguration_id) REFERENCES importconfiguration (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;


-- 4. Add table "importconfiguration_x_mappingfile"
CREATE TABLE IF NOT EXISTS importconfiguration_x_mappingfile (
    importconfiguration_id INT(11) NOT NULL,
    mappingfile_id INT(11) NOT NULL,
    PRIMARY KEY ( importconfiguration_id, mappingfile_id ),
    KEY FK_importconfiguration_x_mappingfile_importconfiguration_id (importconfiguration_id),
    KEY FK_importconfiguration_x_mappingfile_mappingfile_id (mappingfile_id),
    CONSTRAINT FK_importconfiguration_x_mappingfile_importconfiguration_id FOREIGN KEY (importconfiguration_id) REFERENCES importconfiguration(id),
    CONSTRAINT FK_importconfiguration_x_mappingfile_mappingfile_id FOREIGN KEY (mappingfile_id) REFERENCES mappingfile(id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 5. Add column 'default_importconfiguration_id' to table 'project'
ALTER TABLE project ADD default_importconfiguration_id INT(11);

-- 6. Add PICA to Kitodo mapping file
INSERT IGNORE INTO mappingfile (title, file, input_metadata_format, output_metadata_format)
VALUES ('PICA to Kitodo mapping', 'pica2kitodo.xsl', 'PICA', 'KITODO');

-- 7. Add default K10Plus PICA SRU import configuration
INSERT IGNORE INTO importconfiguration (title, description, configuration_type, interface_type, return_format,
                                        default_searchfield_id, identifier_searchfield_id, metadata_format, scheme,
                                        host, path, sru_version, sru_record_schema)
VALUES ('K10Plus-SLUB-PICA', 'K10Plus OPAC PICA', 'OPAC_SEARCH', 'SRU', 'XML', 2, 2, 'PICA', 'https', 'sru.k10plus.de',
        '/gvk', '1.1', 'picaxml');

-- 8. Add search fields for K10Plus import configuration
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Titel', 'pica.tit');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'PPN', 'pica.ppn');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Author', 'pica.per');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'ISSN', 'pica.iss');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'ISBN', 'pica.isb');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Erscheinungsort', 'pica.plc');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Erscheinungsjahr', 'pica.jah');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Volltext', 'pica.txt');

-- 9. Set mapping file for K10Plus import configuration
INSERT IGNORE INTO importconfiguration_x_mappingfile (importconfiguration_id, mappingfile_id) VALUES (1, 1);

-- 10. Add authorities to view and edit import configurations and mapping files
INSERT IGNORE INTO authority (title) VALUES ('addImportConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editImportConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewImportConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllImportConfigurations_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteImportConfiguration_clientAssignable');

INSERT IGNORE INTO authority (title) VALUES ('addMappingFile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editMappingFile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewMappingFile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllMappingFiles_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteMappingFile_clientAssignable');
