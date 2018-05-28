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

package org.goobi.production.enums;

public enum StepReturnValue {

    FINISHED(0, "Step finished"),
    INVALID_DATA(1, "Invalid data"),
    NO_DATA(2, "No data found"),
    DATA_ALREADY_EXISTS(3, "Data already exists"),
    WRITE_ERROR(4, "Data could not be written");

    private int id;
    private String value;

    StepReturnValue(int id, String title) {
        this.id = id;
        this.value = title;
    }

    public int getId() {
        return this.id;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Get StepReturnValue by given title.
     * 
     * @param title
     *            as String
     * @return StepReturnValue object
     */
    public static StepReturnValue getByValue(String title) {
        for (StepReturnValue t : StepReturnValue.values()) {
            if (t.getValue().equals(title)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Get StepReturnValue by given id.
     * 
     * @param id
     *            as int
     * @return StepReturnValue object
     */
    public static StepReturnValue getById(int id) {
        for (StepReturnValue t : StepReturnValue.values()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }
}
