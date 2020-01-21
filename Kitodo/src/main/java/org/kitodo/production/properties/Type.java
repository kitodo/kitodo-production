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

package org.kitodo.production.properties;

public enum Type {
    TEXT("text"),
    LIST("list"),
    LISTMULTISELECT("listmultiselect"),
    BOOLEAN("boolean"),
    DATE("date"),
    NUMBER("number"),
    LINK("link");
    private String name;

    Type(String name) {
        this.name = name;
    }

    /**
     * Get type by name.
     * 
     * @param name
     *            of the type by String
     * @return Type
     */
    public static Type getTypeByName(String name) {
        if (name.equalsIgnoreCase("LIST")) {
            return LIST;
        }
        if (name.equalsIgnoreCase("LISTMULTISELECT")) {
            return LISTMULTISELECT;
        }
        if (name.equalsIgnoreCase("BOOLEAN")) {
            return BOOLEAN;
        }
        if (name.equalsIgnoreCase("DATE")) {
            return DATE;
        }
        if (name.equalsIgnoreCase("NUMBER")) {
            return NUMBER;
        }
        if (name.equalsIgnoreCase("LINK")) {
            return LINK;
        }
        return TEXT;
    }

    public String getName() {
        return this.name;
    }
}
