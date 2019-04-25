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

package org.kitodo.data.database.enums;

/**
 * This enum contains property types, which can be used for display and
 * validation purpose Validation can be done by engaging validation classes,
 * which could be returned by the validation type Enum, contained in here.
 * //TODO: do we need one such an Enum?
 */
public enum PropertyType {

    UNKNOWN(0, "unknown"),
    MESSAGE_IMPORTANT(3, "messageImportant"),
    MESSAGE_ERROR(4, "messageError"),
    STRING(5, "String");

    private int id;
    private String name;

    PropertyType(int id, String inName) {
        this.id = id;
        this.name = inName;
    }

    public String getName() {
        return this.name.toLowerCase();
    }

    @Override
    public java.lang.String toString() {
        return this.name();
    }

    /**
     * Get property by name.
     * 
     * @param name
     *            of property
     * @return PropertyType object
     */
    public static PropertyType getByName(String name) {
        for (PropertyType p : PropertyType.values()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return STRING;
    }

    /**
     * Get id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Get property type by id.
     *
     * @param id
     *            of property type
     * @return property type
     */
    public static PropertyType getById(int id) {
        for (PropertyType p : PropertyType.values()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return STRING;
    }
}
