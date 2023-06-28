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

-- Add authorities to manage OCR profile
INSERT IGNORE INTO authority (title) VALUES ('addOcrProfile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editOcrProfile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteOcrProfile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewOcrProfile_clientAssignable');

-- Add table "ocrprofile"
CREATE TABLE IF NOT EXISTS ocrprofile (
    id INT(10) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    file varchar(255) NOT NULL,
    client_id INT(10) NOT NULL,
    PRIMARY KEY(id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Add column related to OCR profile to process table
ALTER TABLE process ADD ocr_profile_id INT(11) DEFAULT NULL;

-- Add foreign key
ALTER TABLE process add constraint `FK_process_ocr_profile_id`
    foreign key (ocr_profile_id) REFERENCES ocrprofile(id);

-- Add column related to OCR profile to template table
ALTER TABLE template ADD ocr_profile_id INT(11) DEFAULT NULL;

-- Add foreign key
ALTER TABLE template add constraint `FK_template_ocr_profile_id`
    foreign key (ocr_profile_id) REFERENCES ocrprofile(id);
