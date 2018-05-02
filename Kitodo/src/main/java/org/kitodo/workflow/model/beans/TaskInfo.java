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

package org.kitodo.workflow.model.beans;

public class TaskInfo {

    private Integer ordering;
    private String condition;

    public TaskInfo(Integer ordering) {
        this.ordering = ordering;
    }

    public TaskInfo(Integer ordering, String condition) {
        this.ordering = ordering;
        this.condition = condition;
    }

    /**
     * Get ordering.
     *
     * @return value of ordering
     */
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * Set ordering.
     *
     * @param ordering as java.lang.Integer
     */
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    /**
     * Get condition.
     *
     * @return value of condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Set condition.
     *
     * @param condition as java.lang.String
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
