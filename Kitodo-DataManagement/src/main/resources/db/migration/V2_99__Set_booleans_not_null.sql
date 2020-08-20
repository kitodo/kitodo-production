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
-- Migration: Set booleans not null.
--

ALTER TABLE comment
    MODIFY COLUMN isCorrected TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE process
    MODIFY COLUMN inChoiceListShown TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE project
    MODIFY COLUMN active TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE property
    MODIFY COLUMN obligatory TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE ruleset
    MODIFY COLUMN orderMetadataByRuleset TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE task
    MODIFY COLUMN typeMetadata TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeAutomatic TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeImagesRead TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeImagesWrite TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeExportDms TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeAcceptClose TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeCloseVerify TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN batchStep TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN `concurrent` TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN `last` TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN correction TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN separateStructure TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeGenerateImages TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeValidateImages TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE template
    MODIFY COLUMN inChoiceListShown TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE user
    MODIFY COLUMN active TINYINT(1) NOT NULL DEFAULT 1,
    MODIFY COLUMN withMassDownload TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN configProductionDateShow TINYINT(1) NOT NULL DEFAULT 0;
