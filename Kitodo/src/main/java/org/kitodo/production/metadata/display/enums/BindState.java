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

package org.kitodo.production.metadata.display.enums;

public enum BindState {

    CREATE("0", "create"),
    EDIT("1", "edit");

    private String id;
    private String title;

    BindState(String myId, String myTitle) {
        id = myId;
        title = myTitle;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Get BindState by title.
     *
     * @param inTitle
     *            input title
     * @return BindState object
     */
    public static BindState getByTitle(String inTitle) {
        for (BindState type : BindState.values()) {
            if (type.getTitle().equals(inTitle)) {
                return type;
            }
        }
        return EDIT; // edit is default
    }
}
