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
 * Enum for edit type of task steps each one has an integer value, and a title.
 *
 * @author Steffen Hankiewicz
 * @version 17.05.2009
 */
public enum TaskEditType {
    /**
     * Default type is unknown for all steps, which still don't have a specific
     * type.
     */
    UNNOWKN(0, "unknown"),

    /**
     * Manual single workflow for regular workflow handling.
     */
    MANUAL_SINGLE(1, "manuellSingleWorkflow"),

    /**
     * Manual multi workflow for lots of data like image processing with pages
     * of steps.
     */
    MANUAL_MULTI(2, "manuellMultiWorkflow"),

    /**
     * Administrativ = all kinds of steps changed through administrative gui.
     */
    ADMIN(3, "administrativ"),

    /**
     * Automatic = all kinds of automatic steps.
     */
    AUTOMATIC(4, "automatic"),

    /**
     * Queue = all kinds of changes by ActiveMQ.
     */
    QUEUE(5, "queue");

    private int value;
    private String title;

    /**
     * Private constructor, initializes integer value and title.
     */
    TaskEditType(int inValue, String inTitle) {
        this.value = inValue;
        this.title = inTitle;
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
     * Get title from editType.
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Retrieve editType by integer value, necessary for database handlings,
     * where only integer is saved but not type safe.
     *
     * @param editType
     *            as integer value
     * @return {@link TaskEditType} for given integer
     */
    public static TaskEditType getTypeFromValue(Integer editType) {
        if (editType != null) {
            for (TaskEditType taskEditType : values()) {
                if (taskEditType.getValue() == editType.intValue()) {
                    return taskEditType;
                }
            }
        }
        return UNNOWKN;
    }
}
