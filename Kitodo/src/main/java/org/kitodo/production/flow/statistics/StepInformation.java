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

package org.kitodo.production.flow.statistics;

public class StepInformation {

    // step identifier in workflow
    private String title = "";
    private Double averageStepOrder = 0.0;

    // information about all steps of these type
    private int numberOfTotalSteps = 0;
    private int numberOfTotalImages = 0;
    private int totalProcessCount = 0;

    // information about all steps of these type with status done
    private int numberOfStepsDone = 0;
    private int numberOfImagesDone = 0;
    private int processCountDone = 0;

    public StepInformation() {
    }

    public StepInformation(String title) {
        this.title = title;
    }

    public StepInformation(String title, Double avgOrdner) {
        this.title = title;
        this.averageStepOrder = avgOrdner;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the averageStepOrder
     */
    public Double getAverageStepOrder() {
        return averageStepOrder;
    }

    /**
     * @param averageStepOrder
     *            the averageStepOrder to set
     */
    public void setAverageStepOrder(Double averageStepOrder) {
        this.averageStepOrder = averageStepOrder;
    }

    /**
     * @return the numberOfTotalSteps
     */
    public int getNumberOfTotalSteps() {
        return numberOfTotalSteps;
    }

    /**
     * @param numberOfTotalSteps
     *            the numberOfTotalSteps to set
     */
    public void setNumberOfTotalSteps(int numberOfTotalSteps) {
        this.numberOfTotalSteps = numberOfTotalSteps;
    }

    /**
     * @return the numberOfTotalImages
     */
    public int getNumberOfTotalImages() {
        return numberOfTotalImages;
    }

    /**
     * @param numberOfTotalImages
     *            the numberOfTotalImages to set
     */
    public void setNumberOfTotalImages(int numberOfTotalImages) {
        this.numberOfTotalImages = numberOfTotalImages;
    }

    /**
     * @return the totalProcessCount
     */
    public int getTotalProcessCount() {
        return totalProcessCount;
    }

    /**
     * @param totalProcessCount
     *            the totalProcessCount to set
     */
    public void setTotalProcessCount(int totalProcessCount) {
        this.totalProcessCount = totalProcessCount;
    }

    /**
     * @return the numberOfStepsDone
     */
    public int getNumberOfStepsDone() {
        return numberOfStepsDone;
    }

    /**
     * @param numberOfStepsDone
     *            the numberOfStepsDone to set
     */
    public void setNumberOfStepsDone(int numberOfStepsDone) {
        this.numberOfStepsDone = numberOfStepsDone;
    }

    /**
     * @return the numberOfImagesDone
     */
    public int getNumberOfImagesDone() {
        return numberOfImagesDone;
    }

    /**
     * @param numberOfImagesDone
     *            the numberOfImagesDone to set
     */
    public void setNumberOfImagesDone(int numberOfImagesDone) {
        this.numberOfImagesDone = numberOfImagesDone;
    }

    /**
     * @return the processCountDone
     */
    public int getProcessCountDone() {
        return processCountDone;
    }

    /**
     * @param processCountDone
     *            the processCountDone to set
     */
    public void setProcessCountDone(int processCountDone) {
        this.processCountDone = processCountDone;
    }
}
