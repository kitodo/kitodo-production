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

package de.sub.goobi.helper.enums;

import de.sub.goobi.helper.Helper;

/**
 * Enum for edit type of task steps each one has an integer value, and a title
 *
 * @author Steffen Hankiewicz
 * @version 17.05.2009
 */
public enum StepEditType {
    /**
     * default type is unknown for all steps, which still don't have a specific
     * type
     */
    UNNOWKN(0, "unbekannt"),

    /** manual single workflow for regular workflow handling */
    MANUAL_SINGLE(1, "manuellSingleWorkflow"),

    /**
     * manual multi workflow for lots of data like image processing with pages
     * of steps
     */
    MANUAL_MULTI(2, "manuellMultiWorkflow"),

    /** administrativ = all kinds of steps changed through administrative gui */
    ADMIN(3, "administrativ"),

    /** automatic = all kinds of automatic steps */
    AUTOMATIC(4, "automatic");

    private int value;
    private String title;

    /**
     * private constructor, initializes integer value and title
     */
    private StepEditType(int inValue, String inTitle) {
        this.value = inValue;
        this.title = inTitle;
    }

    /**
     * return integer value for database savings
     *
     * @return value as integer
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * get title from editType
     *
     * @return title as translated string for current locale
     */
    public String getTitle() {
        return Helper.getTranslation(this.title);
    }

    /**
     * retrieve editType by integer value, necessary for database handlings,
     * where only integer is saved but not type safe
     *
     * @param editType
     *            as integer value
     * @return {@link StepEditType} for given integer
     */
    public static StepEditType getTypeFromValue(Integer editType) {
        if (editType != null) {
            for (StepEditType ss : values()) {
                if (ss.getValue() == editType.intValue()) {
                    return ss;
                }
            }
        }
        return UNNOWKN;
    }

}
