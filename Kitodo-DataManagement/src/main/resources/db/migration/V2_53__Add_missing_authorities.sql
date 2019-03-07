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

-- Add missing authorities for entities

-- 1. Add workflow
INSERT INTO authority (title) VALUES ('addWorkflow_globalAssignable');

INSERT INTO authority (title) VALUES ('addWorkflow_clientAssignable');

-- 2. View all authorities
INSERT INTO authority (title) VALUES ('viewAllAuthorities_globalAssignable');

-- 3. Add authorities for batch
INSERT INTO authority (title) VALUES ('viewBatch_globalAssignable');
INSERT INTO authority (title) VALUES ('viewAllBatches_globalAssignable');
INSERT INTO authority (title) VALUES ('addBatch_globalAssignable');
INSERT INTO authority (title) VALUES ('editBatch_globalAssignable');
INSERT INTO authority (title) VALUES ('deleteBatch_globalAssignable');

INSERT INTO authority (title) VALUES ('viewBatch_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllBatches_clientAssignable');
INSERT INTO authority (title) VALUES ('addBatch_clientAssignable');
INSERT INTO authority (title) VALUES ('editBatch_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteBatch_clientAssignable');

-- 4. Add authorities for template
INSERT INTO authority (title) VALUES ('viewTemplate_globalAssignable');
INSERT INTO authority (title) VALUES ('viewAllTemplates_globalAssignable');
INSERT INTO authority (title) VALUES ('addTemplate_globalAssignable');
INSERT INTO authority (title) VALUES ('editTemplate_globalAssignable');
INSERT INTO authority (title) VALUES ('deleteTemplate_globalAssignable');

INSERT INTO authority (title) VALUES ('viewTemplate_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllTemplates_clientAssignable');
INSERT INTO authority (title) VALUES ('addTemplate_clientAssignable');
INSERT INTO authority (title) VALUES ('editTemplate_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteTemplate_clientAssignable');

-- 5. Add authorities for workflow interactions
INSERT INTO authority (title) VALUES ('performTask_globalAssignable');
INSERT INTO authority (title) VALUES ('assignTasks_globalAssignable');
INSERT INTO authority (title) VALUES ('overrideTasks_globalAssignable');
INSERT INTO authority (title) VALUES ('superviseTasks_globalAssignable');

INSERT INTO authority (title) VALUES ('performTask_clientAssignable');
INSERT INTO authority (title) VALUES ('assignTasks_clientAssignable');
INSERT INTO authority (title) VALUES ('overrideTasks_clientAssignable');
INSERT INTO authority (title) VALUES ('superviseTasks_clientAssignable');
