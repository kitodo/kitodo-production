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

-- Add authorities to manage OCR-D workflows
INSERT IGNORE INTO authority (title) VALUES ('addOcrdWorkflow_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editOcrdWorkflow_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteOcrdWorkflow_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewOcrdWorkflow_clientAssignable');

-- Add table "ocrdworkflow"
CREATE TABLE IF NOT EXISTS ocrdworkflow (
    id INT(10) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    file varchar(255) NOT NULL,
    client_id INT(10) NOT NULL,
    PRIMARY KEY(id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Add column related to OCR-D workflow to process table
ALTER TABLE process ADD ocrd_workflow_id INT(11) DEFAULT NULL;

-- Add foreign key
ALTER TABLE process add constraint `FK_process_ocrd_workflow_id`
    foreign key (ocrd_workflow_id) REFERENCES ocrdworkflow(id);

-- Add column related to OCR-D workflow to template table
ALTER TABLE template ADD ocrd_workflow_id INT(11) DEFAULT NULL;

-- Add foreign key
ALTER TABLE template add constraint `FK_template_ocrd_workflow_id`
    foreign key (ocrd_workflow_id) REFERENCES ocrdworkflow(id);
