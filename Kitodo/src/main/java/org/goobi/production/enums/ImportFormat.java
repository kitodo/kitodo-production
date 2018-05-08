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

public enum ImportFormat {

    PICA("1", "pica"),
    MARC21("2", "marc21"),
    MARCXML("3", "marcxml"),
    DC("4", "dc");

    private String value;
    private String title;

    ImportFormat(String inValue, String inTitle) {
        this.value = inValue;
        this.title = inTitle;
    }

    /**
     * Get type from value.
     *
     * @param editType
     *            String
     * @return ImportFormat
     */
    public static ImportFormat getTypeFromValue(String editType) {
        if (editType != null) {
            for (ImportFormat ss : values()) {
                if (ss.getValue().equals(editType)) {
                    return ss;
                }
            }
        }
        return PICA;
    }

    /**
     * Get type from title.
     *
     * @param editType
     *            String
     * @return ImportFormat
     */
    public static ImportFormat getTypeFromTitle(String editType) {
        if (editType != null) {
            for (ImportFormat ss : values()) {
                if (ss.getTitle().equals(editType)) {
                    return ss;
                }
            }
        }
        return PICA;
    }

    /**
     * Get value.
     *
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

}
