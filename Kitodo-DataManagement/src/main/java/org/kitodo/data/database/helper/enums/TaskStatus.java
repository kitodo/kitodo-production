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

package org.kitodo.data.database.helper.enums;

/**
 * Enum for status of steps, each one with integer value for database, with
 * title and images for gui
 * 
 * @author Steffen Hankiewicz
 * @version 17.05.2009
 */
public enum TaskStatus {

    /**
     * Locked = step not startable.
     */
    LOCKED(0, "statusGesperrt", "red_10.gif", "red_15a.gif", "steplocked"),
    /**
     * Open = someone can begin with this step.
     */
    OPEN(1, "statusOffen", "orange_10.gif", "orange_15a.gif", "stepopen"),
    /**
     * Inwork = someone is currently working on that step.
     */
    INWORK(2, "statusInBearbeitung", "yellow_10.gif", "yellow_15a.gif", "stepinwork"),
    /**
     * Done = step is executed.
     */
    DONE(3, "statusAbgeschlossen", "green_10.gif", "green_15a.gif", "stepdone");

    private int value;
    private String title;
    private String imageSmall;
    private String imageBig;
    private String searchString;

    /**
     * Private constructor, initializes integer value, title, small and big
     * image.
     */
    TaskStatus(int inValue, String inTitle, String smallImage, String bigImage, String searchString) {
        this.value = inValue;
        this.title = inTitle;
        this.imageSmall = smallImage;
        this.imageBig = bigImage;
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
     * Get file name for small image.
     *
     * @return file name for small image
     */
    public String getSmallImagePath() {
        return "/pages/images/status/" + this.imageSmall;
    }

    /**
     * Get file name for big image.
     *
     * @return file name for big image
     */
    public String getBigImagePath() {
        return "/pages/images/status/" + this.imageBig;
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
