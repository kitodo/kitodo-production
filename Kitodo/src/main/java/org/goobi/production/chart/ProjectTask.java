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

import java.io.Serializable;

public class ProjectTask implements IProjectTask, Serializable {
    private static final long serialVersionUID = 1L;
    private String taskTitle;
    private Integer taskStepsCompleted;
    private Integer taskStepsMax;

    /**
     * Constructor.
     *
     * @param title
     *            String
     * @param stepsCompleted
     *            Integer
     * @param stepsMax
     *            Integer
     */
    public ProjectTask(String title, Integer stepsCompleted, Integer stepsMax) {
        taskTitle = title;
        taskStepsCompleted = stepsCompleted;
        taskStepsMax = stepsMax;
        checkSizes();
    }

    @Override
    public String getTitle() {
        return taskTitle;
    }

    @Override
    public Integer getStepsCompleted() {
        return taskStepsCompleted;
    }

    @Override
    public Integer getStepsMax() {
        return taskStepsMax;
    }

    @Override
    public void setStepsCompleted(Integer stepsCompleted) {
        taskStepsCompleted = stepsCompleted;
    }

    @Override
    public void setStepsMax(Integer stepsMax) {
        taskStepsMax = stepsMax;
    }

    private void checkSizes() {
        if (taskStepsCompleted > taskStepsMax) {
            taskStepsMax = taskStepsCompleted;
        }
    }
}
