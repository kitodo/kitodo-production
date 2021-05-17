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

ALTER TABLE client_x_listColumn RENAME TO client_x_listcolumn;
ALTER TABLE ldapGroup RENAME TO ldapgroup;
ALTER TABLE ldapServer RENAME TO ldapserver;
ALTER TABLE listColumn RENAME TO listcolumn;
ALTER TABLE workflowCondition RENAME TO workflowcondition;
