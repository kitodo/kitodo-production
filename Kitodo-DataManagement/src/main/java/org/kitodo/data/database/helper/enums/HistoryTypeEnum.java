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
 * Enum of all history event types for all history events for processes
 * 
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
public enum HistoryTypeEnum {
    /**
     * Default type is unknown for all properties, which still don't have a
     * specific type.
     */
    unknown(0, "unknown", false, false, null),

    /**
     * storageDifference.
     */
    storageDifference(1, "storageDifference", true, false, null),

    /**
     * imagesWorkDiff.
     */
    imagesWorkDiff(2, "imagesWorkDiff", true, false, null),

    /**
     * imagesMasterDiff.
     */
    imagesMasterDiff(3, "imagesMasterDiff", true, false, null),

    /**
     * metadataDiff.
     */
    metadataDiff(4, "metadataDiff", true, false, null),

    /**
     * docstructDiff.
     */
    docstructDiff(5, "docstructDiff", true, false, null),

    /**
     * taskDone, order number and title.
     */
    taskDone(6, "stepDone", true, true, "min"),

    /**
     * taskOpen, order number and title.
     */
    taskOpen(7, "stepOpen", true, true, "min"),

    /**
     * taskInWork, order number and title.
     */
    taskInWork(8, "stepInWork", true, true, null),

    /**
     * taskError, step order number, step title.
     */
    taskError(9, "stepError", true, true, null),

    /**
     * taskError, step order number, step title.
     */
    taskLocked(10, "stepLocked", true, true, "max"),

    /**
     * bitonal Difference - without function yet.
     */
    bitonal(11, "imagesBitonalDiff", true, false, null),

    /**
     * greyscale Difference - without function yet.
     */
    grayScale(12, "imagesGrayScaleDiff", true, false, null),

    /**
     * color Difference - without function yet.
     */
    color(13, "imagesColorDiff", true, false, null);

    private int value;
    private String title;
    private Boolean isNumeric;
    private Boolean isString;
    private String groupingExpression;

    /**
     * Private constructor, initializes integer value, title and sets boolean,
     * if EventType contains string and/or numeric content.
     */
    HistoryTypeEnum(int inValue, String inTitle, Boolean inIsNumeric, Boolean inIsString,
            String groupingExpression) {
        this.value = inValue;
        this.title = inTitle;
        this.isNumeric = inIsNumeric;
        this.isString = inIsString;
        this.groupingExpression = groupingExpression;
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
     * Get title from type.
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
        // return Helper.getTranslation(this.title);
    }

    /**
     * Return if type contains numeric content.
     *
     * @return isNumeric as {@link Boolean}
     */

    public Boolean isNumeric() {
        return this.isNumeric;
    }

    /**
     * Return if type contains string content.
     *
     * @return isNumeric as {@link String}
     */
    public Boolean isString() {
        return this.isString;
    }

    /**
     * Return grouping function if needed.
     *
     * @return groupingExpression as{@link String}
     */
    public String getGroupingFunction() {
        return this.groupingExpression;
    }

    /**
     * Retrieve history event type by integer value, necessary for database
     * handlings, where only integer is saved but not type safe.
     *
     * @param inType
     *            as integer value
     * @return {@link HistoryTypeEnum} for given integer
     */
    public static HistoryTypeEnum getTypeFromValue(Integer inType) {
        if (inType != null) {
            for (HistoryTypeEnum ss : values()) {
                if (ss.getValue() == inType.intValue()) {
                    return ss;
                }
            }
        }
        return unknown;
    }
}
