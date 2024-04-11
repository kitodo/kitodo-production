/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.interfaces;

import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.kitodo.data.database.enums.WorkflowStatus;

public interface WorkflowInterface extends DataInterface {

    /**
     * Returns the label of the workflow.
     *
     * @return the label
     */
    String getTitle();

    /**
     * Sets the label of the workflow.
     *
     * @param title
     *            label to set
     */
    void setTitle(String title);

    /**
     * Returns the stage of the workflow. Statuses:
     *
     * <dl>
     * <dt>DRAFT</dt>
     * <dd>the workflow is being created</dd>
     * <dt>ACTIVE</dt>
     * <dd>the workflow is in use</dd>
     * <dt>ARCHIVED</dt>
     * <dd>the workflow is no longer used</dd>
     * </dl>
     *
     * @return the stage
     * @deprecated Use {@link #getStatus()}.
     */
    @Deprecated
    default String getWorkflowStatus() {
        WorkflowStatus status = getStatus();
        return Objects.nonNull(status) ? status.toString() : "";
    }

    /**
     * Returns the stage of the workflow. Statuses:
     *
     * <dl>
     * <dt>DRAFT</dt>
     * <dd>the workflow is being created</dd>
     * <dt>ACTIVE</dt>
     * <dd>the workflow is in use</dd>
     * <dt>ARCHIVED</dt>
     * <dd>the workflow is no longer used</dd>
     * </dl>
     *
     * @return the stage
     */
    WorkflowStatus getStatus();

    /**
     * Sets the stage of the workflow. One of {@link WorkflowStatus}.
     *
     * @param status
     *            as String
     * @throws IllegalArgumentException
     *             if {@link WorkflowStatus} has no constant with the specified
     *             name
     * @deprecated Use {@link #setStatus(WorkflowStatus)}.
     */
    @Deprecated
    default void setWorkflowStatus(String status) {
        setStatus(Strings.isNotEmpty(status) ? WorkflowStatus.valueOf(status) : null);
    }

    /**
     * Sets the stage of the workflow.
     *
     * @param status
     *            as String
     */
    void setStatus(WorkflowStatus status);
}
