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

package org.kitodo.dataeditor.pagination;

/**
 * A roman numeral as part of a pagination sequence.
 */
public class RomanNumeral implements Fragment {
    /**
     * These are the string constants that represent the hundreds of the roman
     * numeral.
     */
    private static final String[] HUNDREDS = {"", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm" };

    /**
     * These are the numerical values of the letters used for roman numerals.
     */
    private static final int I = 1;
    private static final int V = 5;
    private static final int X = 10;
    private static final int L = 50;
    private static final int C = 100;
    private static final int D = 500;
    private static final int M = 1000;

    /**
     * These are the string constants that represent the ones of the roman numeral.
     */
    private static final String[] ONES = {"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix" };

    /**
     * These are the string constants that represent the tens of the roman numeral.
     */
    private static final String[] TENS = {"", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc" };

    /**
     * Returns the Roman numeral for the value as string.
     *
     * @param value
     *            value to format
     * @param uppercase
     *            if true, the Roman numeral is upper case, otherwise lower case
     * @return Roman numeral for the value
     */
    public static String format(int value, boolean uppercase) {
        StringBuilder result = new StringBuilder();
        while (value >= 1000) {
            result.append(uppercase ? 'M' : 'm');
            value -= M;
        }
        result.append(HUNDREDS[value / 100]);
        value %= 100;
        result.append(TENS[value / 10]);
        result.append(ONES[value % 10]);
        return uppercase ? result.toString().toUpperCase() : result.toString();
    }

    @Override
    public String format(HalfInteger value) {
        return format(value.intValue(), uppercase);
    }

    /**
     * Returns an int value for a roman number.
     *
     * @param value
     *            the string to be parsed
     * @return an Integer object holding the value represented by the string
     *         argument
     * @throws NumberFormatException
     *             if the string cannot be parsed as an integer
     */
    public static int parseInt(String value) {
        int result = 0;
        for (int i = value.length() - 1; i >= 0; i--) {
            switch (value.charAt(i) | 32) {
                case 'c':
                    if (result >= D) {
                        result -= C;
                    } else {
                        result += C;
                    }
                    break;
                case 'd':
                    if (result >= M) {
                        result -= D;
                    } else {
                        result += D;
                    }
                    break;
                case 'i':
                    if (result >= V) {
                        result -= I;
                    } else {
                        result += I;
                    }
                    break;
                case 'l':
                    if (result >= C) {
                        result -= L;
                    } else {
                        result += L;
                    }
                    break;
                case 'm':
                    result += M;
                    break;
                case 'v':
                    if (result >= X) {
                        result -= V;
                    } else {
                        result += V;
                    }
                    break;
                case 'x':
                    if (result >= L) {
                        result -= X;
                    } else {
                        result += X;
                    }
                    break;
                default:
                    throw new NumberFormatException("For string: " + value);
            }
        }
        return result;
    }

    /**
     * The increment associated with this numeral.
     */
    private HalfInteger increment;

    /**
     * If true, produces upper case roman numerals, else lower case.
     */
    private final boolean uppercase;

    /**
     * Initial value where pagination is started.
     */
    private final int value;

    RomanNumeral(String value, boolean uppercase) {
        this.value = parseInt(value);
        this.uppercase = uppercase;
    }

    @Override
    public HalfInteger getIncrement() {
        return increment;
    }

    @Override
    public Integer getInitialValue() {
        return value;
    }

    @Override
    public void setIncrement(HalfInteger increment) {
        this.increment = increment;
    }

    /**
     * Returns a concise string representation of this instance.
     *
     * @return a string representing this instance
     */
    @Override
    public String toString() {
        return format(value, uppercase) + (increment != null ? (" (" + increment + ")") : " (default)");
    }
}
