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
-- Migration: Add 'metadata_record_id_xpath' and 'metadata_record_title_xpath' columns to importconfiguration

SET SQL_SAFE_UPDATES = 0;

ALTER TABLE importconfiguration ADD metadata_record_id_xpath varchar(255) NOT NULL;
ALTER TABLE importconfiguration ADD metadata_record_title_xpath varchar(255) NOT NULL;

UPDATE importconfiguration SET metadata_record_id_xpath = './/*[local-name()=''recordInfo'']/*[local-name()=''recordIdentifier'']/text()' WHERE metadata_format = 'MODS';
UPDATE importconfiguration SET metadata_record_id_xpath = './/*[local-name()=''datafield''][@tag=''245'']/*[local-name()=''subfield''][@code=''a'']/text()' WHERE metadata_format = 'MARC';
UPDATE importconfiguration SET metadata_record_id_xpath = './/*[local-name()=''datafield''][@tag=''003@'']/*[local-name()=''subfield''][@code=''0'']/text()' WHERE metadata_format = 'PICA';

UPDATE importconfiguration SET metadata_record_title_xpath = './/*[local-name()=''titleInfo'']/*[local-name()=''title'']/text()' WHERE metadata_format = 'MODS';
UPDATE importconfiguration SET metadata_record_title_xpath = './/*[local-name()=''controlfield''][@tag=''001'']/text()' WHERE metadata_format = 'MARC';
UPDATE importconfiguration SET metadata_record_title_xpath = './/*[local-name()=''datafield''][@tag=''021A'']/*[local-name()=''subfield''][@code=''a'']/text()' WHERE metadata_format = 'PICA';

SET SQL_SAFE_UPDATES = 1;
