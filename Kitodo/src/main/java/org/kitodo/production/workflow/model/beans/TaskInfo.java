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

package org.kitodo.production.workflow.model.beans;

public class TaskInfo {

    private int ordering;
    private boolean last;

    /**
     * Public constructor for task information which are determined out of task
     * sequence.
     * 
     * @param ordering
     *            of the task in the sequence, in case tasks are part of the branch
     *            all of them have the same order number
     * @param last
     *            when task is followed by EndEvent it is marked as the last task in
     *            the workflow, useful for cases when branch is followed by one task
     *            in one path and many tasks in another path
     */
    public TaskInfo(int ordering, boolean last) {
        this.ordering = ordering;
        this.last = last;
    }

    /**
     * Get ordering - tasks which are after gateway have exactly the same ordering.
     *
     * @return value of ordering
     */
    public int getOrdering() {
        return ordering;
    }

    /**
     * Set ordering - tasks which are after gateway have exactly the same ordering.
     *
     * @param ordering
     *            as int
     */
    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    /**
     * Get information if task is the last task in the workflow.
     *
     * @return information if task is the last task in the workflow
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Set information if task is the last task in the workflow.
     *
     * @param last
     *            as true or false
     */
    public void setLast(boolean last) {
        this.last = last;
    }
}
