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

package org.goobi.production.properties;

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

    public static Type getTypeByName(String inName) {
        if (inName.equalsIgnoreCase("LIST")) {
            return LIST;
        }
        if (inName.equalsIgnoreCase("LISTMULTISELECT")) {
            return LISTMULTISELECT;
        }
        if (inName.equalsIgnoreCase("BOOLEAN")) {
            return BOOLEAN;
        }
        if (inName.equalsIgnoreCase("DATE")) {
            return DATE;
        }
        if (inName.equalsIgnoreCase("NUMBER")) {
            return NUMBER;
        }
        if (inName.equalsIgnoreCase("LINK")) {
            return LINK;
        }
        return TEXT;
    }

    public String getName() {
        return this.name;
    }
}
