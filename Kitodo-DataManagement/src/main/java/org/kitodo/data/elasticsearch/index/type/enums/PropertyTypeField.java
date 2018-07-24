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

package org.kitodo.data.elasticsearch.index.type.enums;

public enum PropertyTypeField implements TypeInterface {

    ID("id"),
    TITLE("title"),
    VALUE("value"),
    CREATION_DATE("creationDate"),
    PROCESSES("processes"),
    TEMPLATES("templates"),
    WORKPIECES("workpieces"),
    TYPE("type");

    private String name;

    PropertyTypeField(String name) {
        this.name = name;
    }

    /**
     * Get name.
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
