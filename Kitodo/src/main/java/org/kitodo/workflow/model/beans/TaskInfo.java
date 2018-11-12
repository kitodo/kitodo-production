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

    private String condition;
    private String previousTasks;
    private String concurrentTasks;
    private String nextTasks;

    /**
     * Constructor for workflow specific task information.
     * 
     * @param condition
     *            for conditional tasks
     * @param previousTasks
     *            workflow id of previous task(s)
     */
    public TaskInfo(String condition, String previousTasks) {
        this.condition = condition;
        this.previousTasks = previousTasks;
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
     * @param condition
     *            as java.lang.String
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Get previousTasks.
     *
     * @return value of previousTasks
     */
    public String getPreviousTasks() {
        return previousTasks;
    }

    /**
     * Set previousTasks.
     *
     * @param previousTasks
     *            as java.lang.String
     */
    public void setPreviousTasks(String previousTasks) {
        this.previousTasks = previousTasks;
    }

    /**
     * Get concurrentTasks.
     *
     * @return value of concurrentTasks
     */
    public String getConcurrentTasks() {
        return concurrentTasks;
    }

    /**
     * Set concurrentTasks.
     *
     * @param concurrentTasks
     *            as java.lang.String
     */
    public void setConcurrentTasks(String concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
    }

    /**
     * Get nextTasks.
     *
     * @return value of nextTasks
     */
    public String getNextTasks() {
        return nextTasks;
    }

    /**
     * Set nextTasks.
     *
     * @param nextTasks
     *            as java.lang.String
     */
    public void setNextTasks(String nextTasks) {
        this.nextTasks = nextTasks;
    }
}
