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

-- Add list columns for the template docket and workflow
INSERT INTO listcolumn (title) VALUES ('template.docket');
INSERT INTO listcolumn (title) VALUES ('template.workflow');

-- Add the new columns to the client_x_listColumn table
INSERT INTO client_x_listcolumn (client_id, column_id)
SELECT client.id, listcolumn.id
FROM client
         CROSS JOIN listcolumn
WHERE listcolumn.title IN ('template.docket', 'template.workflow');
