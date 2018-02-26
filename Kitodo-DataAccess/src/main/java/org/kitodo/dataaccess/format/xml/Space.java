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

package org.kitodo.dataaccess.format.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Enum implementing the XML white space handling value constants.
 *
 * @see "http://www.w3.org/TR/xml/#sec-white-space"
 */
enum Space {

    /**
     * Signals that white-space processing is acceptable for this element.
     */
    DEFAULT("default") {
        @Override
        /**
         * Trimming white space the XML way.
         */
        public String trim(String input) {
            State state = State.INITIAL;
            StringBuilder result = new StringBuilder(input.length());
            for (int i = 0; i < input.length(); i++) {
                int codePoint = input.codePointAt(i);
                if (!WHITE_SPACE.contains(codePoint)) {
                    if (state.equals(State.WHITE_SPACE)) {
                        result.append(' ');
                    }
                    result.appendCodePoint(codePoint);
                    state = State.TEXT;
                } else if (state.equals(State.TEXT)) {
                    state = State.WHITE_SPACE;
                }
            }
            return result.toString();
        }
    },

    /**
     * Signals the intention that in that element the white space should be
     * preserved.
     */
    PRESERVE("preserve") {
        @Override
        /**
         * Trimming is doing nothing.
         */
        public String trim(String input) {
            return input;
        }
    };

    /**
     * States for a little state machine used in trimming white space.
     */
    private enum State {
        /**
         * Initial state. Preceeding white space is skipped.
         */
        INITIAL,
        /**
         * Last character was text. If the next character is text, do not
         * prepend a white space.
         */
        TEXT,
        /**
         * Last character was non-initial white space. If the next character is
         * text, a white space will be prepended.
         */
        WHITE_SPACE
    }

    /**
     * Reverse look-up map for the enum constants.
     */
    private static final Map<String, Space> reversed;

    /**
     * Characters considered as white space.
     */
    private static Set<Integer> WHITE_SPACE = new HashSet<>(
            new HashSet<>(Arrays.asList((int) '\n', (int) '\r', (int) '\t', (int) ' ')));

    static {
        reversed = new HashMap<>();
        for (final Space value : Space.values()) {
            reversed.put(value.enumeratedValue, value);
        }
    }

    /**
     * Returns whether white space must be preserved to correctly represent this
     * string in XML.
     *
     * @param testString
     *            string to examine
     * @return if preservation of white space is necessary
     */
    public static boolean requiresPreservation(String testString) {
        boolean result = true;
        for (int i = 0; i < testString.length(); i++) {
            int codePoint = testString.codePointAt(i);
            if (!WHITE_SPACE.contains(codePoint)) {
                result = false;
            } else if ((codePoint == ' ') && !result) {
                result = true;
            } else {
                return true;
            }
        }
        return result;
    }

    /**
     * Resolve a String to an enum constant.
     *
     * @param value
     *            String to resolve
     * @return the enum constant
     */
    static Space resolve(String value) {
        String lowerCase = value.toLowerCase();
        if (!reversed.containsKey(lowerCase)) {
            throw new IllegalArgumentException("Unknown xml:space value: " + value);
        }
        return reversed.get(lowerCase);
    }

    /**
     * String value as by specification.
     */
    private final String enumeratedValue;

    /**
     * Creates the enumeration’s elements. Populates their private fields.
     *
     * @param enumeratedValue
     *            String value as by specification
     */
    Space(String enumeratedValue) {
        this.enumeratedValue = enumeratedValue;
    }

    /**
     * Returns the string value representing this setting.
     *
     * @return string value representing this setting
     */
    public String getValue() {
        return enumeratedValue;
    }

    /**
     * Trims a String the XML way, if Space.DEFAULT is chosen. Does nothing if
     * Space.PRESERVE is chosen.
     *
     * @param stringToTrim
     *            string to process
     * @return trimmed string
     */
    public abstract String trim(String stringToTrim);
}
