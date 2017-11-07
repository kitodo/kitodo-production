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

public enum ImportType {
    Record("1", "record"),
    ID("2", "id"),
    FILE("3", "file"),
    FOLDER("4", "folder");

    private String id;
    private String title;

    ImportType(String id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * Set id.
     *
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set title.
     *
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get title.
     *
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Get by title.
     *
     * @param title
     *            String
     * @return ImportType object
     */
    public static ImportType getByTitle(String title) {
        for (ImportType t : ImportType.values()) {
            if (t.getTitle().equals(title)) {
                return t;
            }
        }
        return null;
    }

}
