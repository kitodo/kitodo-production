--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.
--

ALTER TABLE client_x_listColumn RENAME TO client_x_listcolumn;
ALTER TABLE ldapGroup RENAME TO ldapgroup;
ALTER TABLE ldapServer RENAME TO ldapserver;
ALTER TABLE listColumn RENAME TO listcolumn;
ALTER TABLE workflowCondition RENAME TO workflowcondition;
