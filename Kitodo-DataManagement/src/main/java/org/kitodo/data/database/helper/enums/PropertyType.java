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

package org.kitodo.data.database.helper.enums;

/**
 * This enum contains property types, which can be used for display and
 * validation purpose Validation can be done by engaging validation classes,
 * which could be returned by the validation type Enum, contained in here.
 *
 * //TODO: do we need one such an Enum?
 */
public enum PropertyType {

    UNKNOWN(0, "unknown", false),
    MESSAGE_IMPORTANT(3, "messageImportant", false),
    MESSAGE_ERROR(4, "messageError", false),
    STRING(5, "String", true);

    private int id;
    private String name;
    private Boolean showInDisplay;

    PropertyType(int id, String inName, Boolean showInDisplay) {
        this.id = id;
        this.name = inName;
        this.showInDisplay = showInDisplay;
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
            if (p.getName().equals(name.toLowerCase())) {
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
            if (p.getId() == (id)) {
                return p;
            }
        }
        return STRING;
    }
}
