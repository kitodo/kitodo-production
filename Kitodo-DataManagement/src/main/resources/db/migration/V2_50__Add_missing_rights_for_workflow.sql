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

-- Add rights for workflow
INSERT INTO authority (title) VALUES ('viewWorkflow_globalAssignable');
INSERT INTO authority (title) VALUES ('viewAllWorkflows_globalAssignable');
INSERT INTO authority (title) VALUES ('editWorkflow_globalAssignable');
INSERT INTO authority (title) VALUES ('deleteWorkflow_globalAssignable');

INSERT INTO authority (title) VALUES ('viewWorkflow_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllWorkflows_clientAssignable');
INSERT INTO authority (title) VALUES ('editWorkflow_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteWorkflow_clientAssignable');

INSERT INTO authority (title) VALUES ('viewWorkflow_projectAssignable');
INSERT INTO authority (title) VALUES ('viewAllWorkflows_projectAssignable');
INSERT INTO authority (title) VALUES ('editWorkflow_projectAssignable');
INSERT INTO authority (title) VALUES ('deleteWorkflow_projectAssignable');
