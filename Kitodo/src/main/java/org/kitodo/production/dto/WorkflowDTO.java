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

package org.kitodo.production.dto;

import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.kitodo.data.database.enums.WorkflowStatus;
import org.kitodo.data.interfaces.WorkflowInterface;

public class WorkflowDTO extends BaseDTO implements WorkflowInterface {

    private String title;
    private String status;

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get status.
     *
     * @return value of status
     */
    public String getWorkflowStatus() {
        return status.toLowerCase();
    }

    /**
     * Set status.
     *
     * @param status
     *            as String
     */
    public void setWorkflowStatus(String status) {
        this.status = status;
    }

    @Override
    public WorkflowStatus getStatus() {
        return Strings.isNotEmpty(status) ? WorkflowStatus.valueOf(status) : null;
    }

    @Override
    public void setStatus(WorkflowStatus status) {
        this.status = Objects.nonNull(status) ? status.toString() : null;
    }
}
