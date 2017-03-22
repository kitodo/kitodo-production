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
     *
     * @return task title
     */
    public abstract String getTitle();

    /**
     *
     * @return number of completed steps
     */
    public abstract Integer getStepsCompleted();

    /**
     *
     * @return maximum number of steps
     */
    public abstract Integer getStepsMax();

    /**
     *
     * @param stepsCompleted
     *            sets number of completed steps
     */

    public abstract void setStepsCompleted(Integer stepsCompleted);

    /**
     *
     * @param stepsMax
     */
    public abstract void setStepsMax(Integer stepsMax);

}
