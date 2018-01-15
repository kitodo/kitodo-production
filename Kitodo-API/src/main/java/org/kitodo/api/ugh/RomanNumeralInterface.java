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

package org.kitodo.api.ugh;

/**
 * A class that represents a number as a Roman numeral.
 *
 * <p>
 * Roman numerals only represents natural numbers, that are integers greater
 * than 0. The maximum integer value of a roman numeral is 4999. The roman
 * numeral can be displayed either in archaic style (4 = IIII) or modern style
 * (4 = IV). The default display is modern.
 */
public interface RomanNumeralInterface {
    /**
     * Returns the string representation of the Roman numeral.
     *
     * @return a string representation of the Roman numeral
     */
    String getNumber();

    /**
     * Converts the Roman numeral to an int.
     *
     * @return the int value of the Roman numeral
     */
    int intValue();

    /**
     * Accepts an integer and sets Roman numeral to that value.
     *
     * @param value
     *            the value
     * @exception NumberFormatException
     *                If the parameter is out of range.
     */
    void setValue(int value);

    /**
     * Sets the value of the Roman numeral.
     *
     * @param value
     *            A string representation of a Roman numeral
     * @exception NumberFormatException
     *                If the string parameter is not a valid RomanNumeral
     */
    void setValue(String value);
}
