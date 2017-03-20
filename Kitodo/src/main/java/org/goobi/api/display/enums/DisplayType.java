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

package org.goobi.api.display.enums;

public enum DisplayType {

    input("0","input"),select("1","select"),select1("2","select1"),textarea("3","textarea"),
    readonly("4", "readonly");

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
     * @param inTitle input title
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
        return textarea; // textarea is default
    }
}
