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

package org.kitodo.helper;

import java.util.Comparator;

/**
 * A string comparator implementation that compares strings as to their
 * lexicographical order. This respects ordering symbols before numbers, upper
 * case letters before lower case, sorting umlauts and German sharp s as to
 * German sort rules, and sorting sequences of numbers by their numeric value,
 * ignoring preceding zeroes.
 *
 * <p>
 * This class was implemented to fix the shortcoming that you could only sort
 * the content files either numerically or alphanumerically in the past. They
 * can now be sorted correctly in all cases.
 */
public class LexicographicalOrder implements Comparator<String> {

    /**
     * Compares two strings as defined by this comparator. A value &lt;0 means
     * that the first String goes before the second one, a value of &gt;0 means
     * that the second String has to go before the first one. A value of 0 means
     * that both strings are equal. Supports {@code null} Strings, which are
     * sorted before any other.
     */
    @Override
    public int compare(String one, String another) {
        // First, compare case insensitive.
        int caseInsensitiveResult = compare(one, another, true);
        if (caseInsensitiveResult != 0) {
            return caseInsensitiveResult;
        }

        // If equal, compare case sensitive, uppercase before lowercase.
        int caseSensitiveResult = compare(one, another, false);
        if (caseSensitiveResult != 0) {
            return caseSensitiveResult;
        }

        // If equal, check for binary equality. This is important for not
        // loosing hash data structure members, that differ in symbols, but have
        // no further order.
        return one.compareTo(another);
    }

    private int compare(String one, String another, boolean caseInsensitive) {
        LexicographicalOrderTokenizer oneTokenizer = new LexicographicalOrderTokenizer(one, caseInsensitive);
        LexicographicalOrderTokenizer anotherTokenizer = new LexicographicalOrderTokenizer(another, caseInsensitive);
        while (true) {
            int[] oneToken = oneTokenizer.next();
            int[] anotherToken = anotherTokenizer.next();
            int typeComparison = oneToken[0] - anotherToken[0];
            if (typeComparison != 0) {
                return typeComparison;
            }
            if (oneToken[0] == LexicographicalOrderTokenizer.END) {
                return 0;
            }
            int valueComparison = oneToken[1] - anotherToken[1];
            if (valueComparison != 0) {
                return valueComparison;
            }
            if (oneToken[0] == LexicographicalOrderTokenizer.NUMERAL) {
                int lengthComparison = anotherToken[2] - oneToken[2];
                if (lengthComparison != 0) {
                    return lengthComparison;
                }
            }
        }
    }
}
