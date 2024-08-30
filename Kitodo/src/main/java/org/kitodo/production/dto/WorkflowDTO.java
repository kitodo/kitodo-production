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

import org.apache.commons.lang3.StringUtils;
import org.kitodo.data.database.enums.WorkflowStatus;

public class WorkflowDTO extends BaseDTO {

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

    public WorkflowStatus getStatus() {
        return StringUtils.isNotBlank(status) ? WorkflowStatus.valueOf(status) : null;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = Objects.nonNull(status) ? status.toString() : null;
    }
}
