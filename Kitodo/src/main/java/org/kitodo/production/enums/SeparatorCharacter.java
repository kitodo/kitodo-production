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

package org.kitodo.production.enums;

/**
 * Enum representing various separator characters that can be used to delimit
 * values in a string. Each enum constant is associated with a specific character
 * used as the separator.
 */
public enum SeparatorCharacter {
    COMMA(","),
    SEMICOLON(";"),
    TAB("\t"),
    DOLLAR("$"),
    VERTICAL_BAR("|");

    private final String separator;

    SeparatorCharacter(String separator) {
        this.separator = separator;
    }

    /**
     * Retrieves the separator character associated with this enum constant.
     *
     * @return the separator character as a string
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Returns a string representation of the enum constant, including its name
     * and the associated separator character in parentheses.
     *
     * @return the string representation of this enum constant in the format
     *         "NAME ('separator')"
     */
    @Override
    public String toString() {
        return name() + (" ('" + separator + "')");
    }

    /**
     * Retrieves the corresponding {@link SeparatorCharacter} for the specified
     * character. If the input character does not match any predefined separator
     * character, this method returns null.
     *
     * @param character the separator character as a string
     * @return the associated {@link SeparatorCharacter} enum constant, or null
     *         if no match is found
     */
    public static SeparatorCharacter getByCharacter(String character) {
        switch (character) {
            case ",":
                return COMMA;
            case ";":
                return SEMICOLON;
            case "\t":
                return TAB;
            case "$":
                return DOLLAR;
            case "|":
                return VERTICAL_BAR;
            default:
                return null;
        }
    }
}
