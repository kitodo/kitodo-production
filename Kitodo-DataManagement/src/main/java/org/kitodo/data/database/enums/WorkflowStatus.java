/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.data.database.enums;

/**
 * Enum for workflow status. Statuses:
 *
 * <dl>
 * <dt>DRAFT</dt>
 * <dd>it is possible to edit workflow but not yet use</dd>
 * <dt>ACTIVE</dt>
 * <dd>workflow is not editable anymore but can be used</dd>
 * <dt>ARCHIVED</dt>
 * <dd>workflow cannot be used anymore</dd>
 * </dl>
 */
public enum WorkflowStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}
