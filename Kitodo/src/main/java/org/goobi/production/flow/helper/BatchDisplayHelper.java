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

package org.goobi.production.flow.helper;

import java.util.List;

import org.kitodo.data.database.helper.enums.TaskStatus;

public class BatchDisplayHelper {

    private List<BatchDisplayItem> stepList = null;
    private boolean panelOpen = false;

    public BatchDisplayHelper() {
    }

    public boolean isPanelOpen() {
        return this.panelOpen;
    }

    public void setPanelOpen(boolean panelOpen) {
        this.panelOpen = panelOpen;
    }

    public List<BatchDisplayItem> getStepList() {
        return this.stepList;
    }

    public void setStepList(List<BatchDisplayItem> stepList) {
        this.stepList = stepList;
    }

    /**
     * Auswertung des Fortschritts.
     */
    public String getFortschritt() {
        int open = 0;
        int inProgress = 0;
        int completed = 0;

        for (BatchDisplayItem bdi : this.stepList) {
            if (bdi.getStepStatus() == TaskStatus.DONE) {
                completed++;
            } else if (bdi.getStepStatus() == TaskStatus.LOCKED) {
                open++;
            } else {
                inProgress++;
            }
        }
        double openTwo = 0;
        double inProgressTwo = 0;
        double completedTwo = 0;

        if ((open + inProgress + completed) == 0) {
            open = 1;
        }

        openTwo = (open * 100) / (double) (open + inProgress + completed);
        inProgressTwo = (inProgress * 100) / (double) (open + inProgress + completed);
        completedTwo = 100 - openTwo - inProgressTwo;
        // (completed * 100) / (open + inProgress + completed);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        return df.format(completedTwo) + df.format(inProgressTwo) + df.format(openTwo);
    }

    /**
     * Get progress 1.
     *
     * @return int
     */
    public int getProcess() {
        int open = 0;
        int inProgress = 0;
        int completed = 0;

        for (BatchDisplayItem bdi : this.stepList) {
            if (bdi.getStepStatus() == TaskStatus.DONE) {
                completed++;
            } else if (bdi.getStepStatus() == TaskStatus.LOCKED) {
                open++;
            } else {
                inProgress++;
            }
        }
        if ((open + inProgress + completed) == 0) {
            open = 1;
        }
        return (open * 100) / (open + inProgress + completed);
    }

    /**
     * Get progress 2.
     *
     * @return int
     */
    public int getProgressTwo() {
        int open = 0;
        int inProgress = 0;
        int completed = 0;

        for (BatchDisplayItem bdi : this.stepList) {
            if (bdi.getStepStatus() == TaskStatus.DONE) {
                completed++;
            } else if (bdi.getStepStatus() == TaskStatus.LOCKED) {
                open++;
            } else {
                inProgress++;
            }
        }
        if ((open + inProgress + completed) == 0) {
            open = 1;
        }
        return (inProgress * 100) / (open + inProgress + completed);
    }

    /**
     * Get progress 3.
     *
     * @return int
     */
    public int getProgressThree() {
        int open = 0;
        int inProgress = 0;
        int completed = 0;

        for (BatchDisplayItem bdi : this.stepList) {
            if (bdi.getStepStatus() == TaskStatus.DONE) {
                completed++;
            } else if (bdi.getStepStatus() == TaskStatus.LOCKED) {
                open++;
            } else {
                inProgress++;
            }
        }
        if ((open + inProgress + completed) == 0) {
            open = 1;
        }
        double openTwo = 0;
        double inProgressTwo = 0;
        double completedTwo = 0;

        openTwo = (open * 100) / (double) (open + inProgress + completed);
        inProgressTwo = (inProgress * 100) / (double) (open + inProgress + completed);
        completedTwo = 100 - openTwo - inProgressTwo;
        return (int) completedTwo;
    }
}
