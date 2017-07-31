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

package org.goobi.production.chart;

public interface IProjectTask {

    /**
     * Get Task title.
     * 
     * @return task title
     */
    String getTitle();

    /**
     * Get amount of completed tasks.
     * 
     * @return number of completed steps
     */
    Integer getStepsCompleted();

    /**
     * Get maximal amounts of tasks.
     * 
     * @return maximum number of steps
     */
    Integer getStepsMax();

    /**
     * Set amount of completed tasks.
     * 
     * @param stepsCompleted
     *            sets number of completed steps
     */

    void setStepsCompleted(Integer stepsCompleted);

    /**
     * Set maximal amounts of tasks.
     * 
     * @param stepsMax
     *            as Integer
     */
    void setStepsMax(Integer stepsMax);

}
