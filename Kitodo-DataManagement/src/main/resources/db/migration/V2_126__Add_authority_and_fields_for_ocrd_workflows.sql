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

-- Add authority to assign OCR-D workflow in template or process details
INSERT IGNORE INTO authority (title) VALUES ('assignOcrdWorkflow_clientAssignable');

-- Add columns of OCR-D workflow identifier
ALTER TABLE process ADD ocrd_workflow_id varchar(255) DEFAULT NULL;
ALTER TABLE template ADD ocrd_workflow_id varchar(255) DEFAULT NULL;
