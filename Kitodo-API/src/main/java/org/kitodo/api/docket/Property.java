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

package org.kitodo.api.docket;

public class Property {

    /** the property id. */
    private Integer id;
    /** The title of the property. */
    private String title;
    /** The value of the property. */
    private String value;

    /** Gets the id. */
    public Integer getId() {
        return id;
    }

    /** Sets the id. */
    public void setId(Integer id) {
        this.id = id;
    }

    /** Gets the title. */
    public String getTitle() {
        return title;
    }

    /** Sets the title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Gets the value. */
    public String getValue() {
        return value;
    }

    /** Sets the value. */
    public void setValue(String value) {
        this.value = value;
    }
}
