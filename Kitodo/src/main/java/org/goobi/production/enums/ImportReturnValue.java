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

public enum ImportReturnValue {

    ExportFinished(0, "Export finished"),
    InvalidData(1, "Invalid data"),
    NoData(2, "No data found"),
    DataAlreadyExists(3, "Data already exists"),
    WriteError(4, "Data could not be written");

    private int id;
    private String value;

    ImportReturnValue(int id, String title) {
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
     * Get by value.
     *
     * @param title
     *            String
     * @return ImportReturnValue
     */
    public static ImportReturnValue getByValue(String title) {
        for (ImportReturnValue t : ImportReturnValue.values()) {
            if (t.getValue().equals(title)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Get by id.
     *
     * @param id
     *            int
     * @return ImportReturnValue
     */
    public static ImportReturnValue getById(int id) {
        for (ImportReturnValue t : ImportReturnValue.values()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }
}
