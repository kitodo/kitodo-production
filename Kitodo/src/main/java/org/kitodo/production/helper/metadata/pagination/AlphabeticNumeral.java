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

package org.kitodo.production.helper.metadata.pagination;

import java.util.Objects;

public class AlphabeticNumeral implements Fragment {
    static final int ALPHABET_SIZE = 26;
    static final char FIRST_CHAR = 'a';

    /**
     * Initial value where pagination is started.
     */
    private final int value;

    /**
     * The increment associated with this numeral.
     */
    private HalfInteger increment;

    AlphabeticNumeral(String value) {
        this.value = parseInt(value);
    }

    /**
     * Returns the increment associated with the fragment.
     *
     * @return the increment of fragment
     */
    @Override
    public HalfInteger getIncrement() {
        return increment;
    }

    /**
     * Returns the initial value of the fragment. This is where pagination will be
     * started.
     *
     * @return the pagination start value
     */
    @Override
    public Integer getInitialValue() {
        return value;
    }

    /**
     * Sets the increment of the fragment.
     *
     * @param increment
     *            increment value the fragment will cause
     */
    @Override
    public void setIncrement(HalfInteger increment) {
        this.increment = increment;
    }

    @Override
    public String toString() {
        return format(value) + (Objects.nonNull(increment) ? " (" + increment + ")" : " (default)");
    }

    /**
     * Returns the value formatted as alphabetic characters.
     *
     * @param value value to format
     * @return the formatted value
     */
    @Override
    public String format(HalfInteger value) {
        return format(value.intValue());
    }

    /**
     * Returns the value formatted as alphabetic characters.
     *
     * @param number numeric value to format
     * @return the formatted value
     */
    public static String format(int number) {
        StringBuilder result = new StringBuilder();

        while (number > 0) {
            number--; // Increment number by 1, to make sure the remainder is computed correctly.
            int remainder = number % ALPHABET_SIZE;
            char letter = (char) (FIRST_CHAR + remainder);
            result.insert(0, letter);
            number = number / ALPHABET_SIZE;
        }

        return result.toString();
    }

    /**
     * Converts an alphabetic string representation into its corresponding integer value.
     * The method interprets the string in a case-insensitive manner, treating 'a' as 1, 'b' as 2, and so on.
     *
     * @param characters the alphabetic string to parse, consisting of letters only.
     *                   Strings containing non-alphabetic characters will result in a NumberFormatException.
     * @return the integer value corresponding to the given alphabetic string.
     * @throws NumberFormatException if the input string contains invalid characters or is empty.
     */
    public static int parseInt(String characters) {
        if (!characters.matches("^[A-Za-z]+$")) {
            throw new NumberFormatException("Invalid characters: " + characters);
        }
        int result = 0;
        String normalizedCharacters = characters.toLowerCase();
        for (int i = 0; i < normalizedCharacters.length(); i++) {
            char c = normalizedCharacters.charAt(i);
            // Use 'a' as the first character (a=1, b=2, …) to compute the value for the current character.
            int value = c - FIRST_CHAR + 1;
            result = result * ALPHABET_SIZE + value;
        }
        return result;
    }
}
