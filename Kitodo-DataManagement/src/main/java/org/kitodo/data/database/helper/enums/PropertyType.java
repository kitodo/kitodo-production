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
 * @author Wulf
 */
public enum PropertyType {

    unknown(0, "unknown", false, false),

    /**
     * general type.
     */
    general(1, "general", false, false),

    /**
     * normal message.
     */
    messageNormal(2, "messageNormal", false, false),

    /**
     * important message.
     */
    messageImportant(3, "messageImportant", false, false),

    /**
     * error message.
     */
    messageError(4, "messageError", false, false),

    String(5, "String", true, true),

    Boolean(6, "Boolean", true, true),

    List(7, "List", true, true),

    Number(8, "Number", true, true),

    Container(9, "Container", true, true),

    Date(10, "Date", true, true),

    Integer(11, "Integer", true, true),

    SpecialView(12, "SpecialView", false, true),

    Textarea(13, "Textarea", true, true),

    ListMultiSelect(14, "ListMultiSelect", true, true),

    WikiField(15, "WikiField", false, true),

    // special Properties
    Hidden(16, "Hidden", false, false),

    ErrorMessage(17, "ErrorMessage", true, false),

    CommandLink(18, "CommandLink", true, false),

    NoEdit(19, "NoEdit", true, false);

    private int id;
    private String name;

    private Boolean showInDisplay;

    PropertyType(int id, String inName, Boolean showInDisplay, Boolean editable) {
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
        return String;
    }

    public Boolean getShowInDisplay() {
        return showInDisplay;
    }

    /**
     * Set id.
     *
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
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
        return String;
    }
}
