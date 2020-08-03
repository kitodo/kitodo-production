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
    MODIFY isCorrected TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE process
    MODIFY inChoiceListShown TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE project
    MODIFY active TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE property
    MODIFY obligatory TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE ruleset
    MODIFY orderMetadataByRuleset TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE task
    MODIFY typeMetadata TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeAutomatic TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeImagesRead TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeImagesWrite TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeExportDms TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeAcceptClose TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeCloseVerify TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY batchStep TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY `concurrent` TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY `last` TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY correction TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY separateStructure TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeGenerateImages TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY typeValidateImages TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE template
    MODIFY inChoiceListShown TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE user
    MODIFY active TINYINT(1) NOT NULL DEFAULT 1,
    MODIFY withMassDownload TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY configProductionDateShow TINYINT(1) NOT NULL DEFAULT 0;
