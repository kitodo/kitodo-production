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

package org.kitodo.data.database.enums;

/**
 * Enum for status of steps, each one with integer value for database, with
 * title and images for gui.
 */
public enum TaskStatus {

    /**
     * Locked = step not startable.
     */
    LOCKED(0, "statusLocked", "steplocked"),
    /**
     * Open = someone can begin with this step.
     */
    OPEN(1, "statusOpen", "stepopen"),
    /**
     * Inwork = someone is currently working on that step.
     */
    INWORK(2, "statusInProcessing", "stepinwork"),
    /**
     * Done = step is executed.
     */
    DONE(3, "statusDone", "stepdone");

    private int value;
    private String title;
    private String searchString;

    /**
     * Private constructor, initializes integer value, title and big
     * image.
     */
    TaskStatus(int inValue, String inTitle, String searchString) {
        this.value = inValue;
        this.title = inTitle;
        this.searchString = searchString;
    }

    /**
     * Return integer value for database savings.
     *
     * @return value as integer
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * Get title from status type.
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Retrieve StepStatus by integer value, necessary for database handlings,
     * where only integer is saved but not type safe.
     *
     * @param value
     *            as integer value
     * @return {@link TaskStatus} for given integer
     */
    public static TaskStatus getStatusFromValue(Integer value) {
        if (value != null) {
            for (TaskStatus taskStatus : values()) {
                if (taskStatus.getValue() == value.intValue()) {
                    return taskStatus;
                }
            }
        }
        return LOCKED;
    }

    public String getSearchString() {
        return this.searchString;
    }
}
