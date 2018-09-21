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

/**
 * A string tokenizer with type detection, insertions for umlauts and number
 * group detection. I.e., the String "Grüße166.txt" will be tokenized to ['G',
 * 'r', 'u', 'e', 's', 's', 'e', 166, '.', 't', 'x', 't', -1, -1, …].
 */
class LexicographicalOrderTokenizer {

    /**
     * Array of base characters for the characters U+0000 .. U+017E. Example:
     * 'a' (97) for 'ä'.
     */
    private static final short[] BASES = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                                                      19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
                                                      35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 0, 1, 2, 3, 4,
                                                      5, 6, 7, 8, 9, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
                                                      71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86,
                                                      87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
                                                      103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115,
                                                      116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128,
                                                      129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141,
                                                      142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154,
                                                      155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167,
                                                      168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180,
                                                      181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 65, 65, 65,
                                                      65, 65, 65, 65, 67, 69, 69, 69, 69, 73, 73, 73, 73, 68, 78, 79,
                                                      79, 79, 79, 79, 215, 79, 85, 85, 85, 85, 89, 222, 115, 97, 97, 97,
                                                      97, 97, 97, 97, 99, 101, 101, 101, 101, 105, 105, 105, 105, 240,
                                                      110, 111, 111, 111, 111, 111, 247, 111, 117, 117, 117, 117, 121,
                                                      254, 121, 65, 97, 65, 97, 65, 97, 67, 99, 67, 99, 67, 99, 67, 99,
                                                      68, 100, 68, 100, 69, 101, 69, 101, 69, 101, 69, 101, 69, 101, 71,
                                                      103, 71, 103, 71, 103, 71, 103, 72, 104, 72, 104, 73, 105, 73,
                                                      105, 73, 105, 73, 105, 73, 105, 73, 105, 74, 106, 75, 107, 107,
                                                      76, 108, 76, 108, 76, 108, 76, 108, 108, 108, 78, 110, 78, 110,
                                                      78, 110, 110, 78, 110, 79, 111, 79, 111, 79, 111, 79, 111, 82,
                                                      114, 82, 114, 82, 114, 83, 115, 83, 115, 83, 115, 83, 115, 84,
                                                      116, 84, 116, 84, 116, 85, 117, 85, 117, 85, 117, 85, 117, 85,
                                                      117, 85, 117, 87, 119, 89, 121, 89, 90, 122, 90, 122, 90, 122 };

    /**
     * Type value indicating the end of the string was reached. Sorts before any
     * other.
     */
    static final short END = -1;

    /**
     * Constant value indicating that this letter does not require another
     * letter to be inserted in the comparison String.
     */
    private static final short NONE = -1;

    /**
     * Array of umlaut and ligature resolving insertion (secondary) characters
     * for the characters U+0000 .. U+017E. Example: 'e' (101) for 'ä'.
     */
    private static final short[] INSERTS = new short[] {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, 69, NONE, 69, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, 69, NONE, NONE, NONE, NONE, NONE, 69, NONE,
                                                        NONE, 115, NONE, NONE, NONE, NONE, 101, NONE, 101, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, 101, NONE, NONE, NONE, NONE, NONE, 101, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, 74, 106, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, 69, 101, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE,
                                                        NONE, NONE, NONE, NONE, NONE, NONE, NONE };

    /**
     * Last code point that can be resolved using the mapping tables. Each table
     * has a length of 383 entries for the code points U+0000 (0) to U+017E
     * (382). All characters above this point are treated as symbols.
     */
    private static final int LAST_MAPPED_CODE_POINT = 0x017E;

    /**
     * Type value indicating the character was found to be a lower case letter.
     * Sorts after any other. As second array entry, the base value for the
     * letter, like 97 ('a') e.g. for both 'ä' and 'à', is returned.
     */
    private static final short LOWER = 3;

    /**
     * Type value indicating the character was found to be a number. Sorts after
     * symbols, before upper case letters. As second array entry, the full
     * number, which may be represented by multiple ciphers in the input string,
     * is returned. As third array entry, the number of leading zeroes found
     * while looking for the end of the number, is returned. This string
     * comparator does only support handling of non-negative integers, which
     * MUST NOT contain thousands’ separator characters.
     */
    static final short NUMERAL = 1;

    /**
     * Type value indicating the character was found to be a symbol. Sorts
     * before any other type, but after a String that already has terminated. As
     * second array entry, the code point value of the symbol is returned.
     */
    private static final short SYMBOL = 0;

    /**
     * Type value indicating the character was found to be an uppercase letter.
     * Sorts after numbers, before lowercase letters. As second array entry, the
     * base value for the letter, like 65 ('A') e.g. for both 'Ä' and 'À', is
     * returned.
     */
    private static final short UPPER = 2;

    /**
     * Array of character types for the characters U+0000 .. U+017E.
     */
    private static final short[] TYPES = new short[] {SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL, NUMERAL,
                                                      NUMERAL, NUMERAL, NUMERAL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                                                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                                                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                                                      UPPER, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, LOWER,
                                                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
                                                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
                                                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL, SYMBOL,
                                                      SYMBOL, SYMBOL, SYMBOL, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                                                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER,
                                                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, SYMBOL,
                                                      UPPER, UPPER, UPPER, UPPER, UPPER, UPPER, SYMBOL, LOWER, LOWER,
                                                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, LOWER,
                                                      LOWER, LOWER, LOWER, LOWER, LOWER, LOWER, SYMBOL, LOWER, LOWER,
                                                      LOWER, LOWER, LOWER, LOWER, SYMBOL, LOWER, LOWER, LOWER, LOWER,
                                                      LOWER, LOWER, SYMBOL, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                                                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                                                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                                                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                                                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                                                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER,
                                                      UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, LOWER, UPPER, UPPER,
                                                      LOWER, UPPER, LOWER, UPPER, LOWER };

    private final boolean caseInsensitive;

    /**
     * If a letter must be inserted to resolve umlauts or sharp s, it is cached
     * here.
     */
    private short insert;

    /**
     * Reading position within the string.
     */
    private int position;

    /**
     * String to tokenize.
     */
    private final String string;

    /**
     * The length of the String, for not calculating it over and over again.
     */
    private final int stringLength;

    /**
     * Creates a new Tokenizer.
     *
     * @param s
     *            String to tokenize
     * @param caseInsensitive
     *            Whether to work case-insensitive
     */
    LexicographicalOrderTokenizer(String s, boolean caseInsensitive) {
        string = s;
        position = SYMBOL;
        stringLength = s != null ? s.length() : -1;
        insert = NONE;
        this.caseInsensitive = caseInsensitive;
    }

    private int currentCodePoint() {
        int i = string.codePointAt(position);
        return caseInsensitive ? Character.toUpperCase(i) : i;
    }

    /**
     * Each call to next() subesequently returns the next token. Result format
     * is an array with the two entries type and value, in case of numbers three
     * entries type, value and number of leading zeroes. After the end of data,
     * an array with one type value of -1 is returned.
     *
     * @return the tokens of the string, each at a time.
     */
    int[] next() {
        if (string == null) {
            return new int[] {Integer.MIN_VALUE };
        }
        if (insert > NONE) {
            short insertValue = insert;
            insert = NONE;
            return new int[] {TYPES[insertValue], insertValue };
        }
        if (position >= stringLength) {
            return new int[] {END };
        }
        int codePoint = currentCodePoint();
        position++;
        if (codePoint > LAST_MAPPED_CODE_POINT) {
            return new int[] {SYMBOL, codePoint };
        }
        short type = TYPES[codePoint];
        if (type != 1) {
            insert = INSERTS[codePoint];
            return new int[] {type, BASES[codePoint] };
        } else {
            int value = BASES[codePoint];
            int leadingZeroes = value == 0 ? 1 : 0;
            while (position < stringLength && TYPES[codePoint = currentCodePoint()] == 1) {
                value = 10 * value + BASES[codePoint];
                if (value == 0) {
                    leadingZeroes++;
                }
                position++;
            }
            return new int[] {1, value, leadingZeroes };
        }
    }
}
