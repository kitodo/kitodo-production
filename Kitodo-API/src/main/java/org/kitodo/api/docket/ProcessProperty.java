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

package org.kitodo.api.docket;

public class ProcessProperty extends Property {

    /** The process id. */
    private Integer processId;

    /**
     * Gets the processId.
     * 
     * @return The processId.
     */
    public Integer getProcessId() {
        return processId;
    }

    /**
     * Sets the processId.
     * 
     * @param processId
     *            The processId.
     */
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }
}
