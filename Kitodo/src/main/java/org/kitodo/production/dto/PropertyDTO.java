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

package org.kitodo.production.dto;

/**
 * Property DTO object.
 */
public class PropertyDTO extends BaseDTO {

    private String title;
    private String value;
    private String creationDate;

    /**
     * Get title.
     *
     * @return title as String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get value.
     *
     * @return value as String
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value
     *            as String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get creation date as String.
     *
     * @return creation date as String.
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Set creation date as String.
     *
     * @param creationDate
     *            as String
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
