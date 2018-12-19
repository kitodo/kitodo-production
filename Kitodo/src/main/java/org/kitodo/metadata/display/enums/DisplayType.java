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

package org.kitodo.metadata.display.enums;

public enum DisplayType {

    INPUT("0", "input"),
    SELECT("1", "select"),
    SELECT1("2", "select1"),
    TEXTAREA("3", "textarea"),
    READONLY("4", "readonly");

    private String id;
    private String title;

    DisplayType(String myId, String myTitle) {
        this.id = myId;
        this.title = myTitle;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * Get display type by title.
     *
     * @param inTitle
     *            input title
     * @return DisplayType object
     */
    public static DisplayType getByTitle(String inTitle) {
        if (inTitle != null) {
            for (DisplayType type : DisplayType.values()) {
                if (type.getTitle().equals(inTitle)) {
                    return type;
                }
            }
        }
        return TEXTAREA; // textarea is default
    }
}
