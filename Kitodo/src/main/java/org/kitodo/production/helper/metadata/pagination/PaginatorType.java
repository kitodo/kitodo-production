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

import java.util.HashMap;
import java.util.Map;

/**
 * Constants for the pagination types selectable from a drop-down menu.
 */
public enum PaginatorType {
    /**
     * User defined counter.
     */
    ADVANCED(99) {
        @Override
        public String format(PaginatorMode mode, String value, boolean fictitious, String separator) {
            return value;
        }
    },

    /**
     * Arabic numbers (1, 2, 3, …).
     */
    ARABIC(1) {
        @Override
        public String format(PaginatorMode mode, String value, boolean fictitious, String separator) {
            int a;
            try {
                a = Integer.parseInt(value);
            } catch (NumberFormatException b) {
                try {
                    a = RomanNumeral.parseInt(value);
                } catch (NumberFormatException unused) {
                    throw b;
                }
            }
            return mode.format(Integer.toString(a), Integer.toString(a + 1), fictitious, separator);
        }
    },

    /**
     * Any string.
     */
    FREETEXT(6) {
        @Override
        public String format(PaginatorMode mode, String value, boolean fictitious, String separator) {
            return mode.format('`' + value + '`', '`' + value + '`', fictitious, separator);
        }
    },

    /**
     * Uppercase roman numbers (I, II, III, …).
     */
    ROMAN(2) {
        @Override
        public String format(PaginatorMode mode, String valueString, boolean fictitious, String separator) {
            int value;
            try {
                value = RomanNumeral.parseInt(valueString);
            } catch (NumberFormatException b) {
                try {
                    value = Integer.parseInt(valueString);
                } catch (NumberFormatException unused) {
                    throw b;
                }
            }
            return mode.format(RomanNumeral.format(value, true), RomanNumeral.format(value + 1, true), fictitious,
                separator);
        }
    },

    /**
     * String "uncounted".
     */
    UNCOUNTED(3) {
        @Override
        public String format(PaginatorMode mode, String value, boolean fictitious, String separator) {
            return mode.format("`uncounted`", "`uncounted`", fictitious, separator);
        }
    };

    /**
     * Map for the look-up of codes.
     */
    private static final Map<Integer, PaginatorType> codeMap = new HashMap<>(
            (int) Math.ceil(values().length / .75));

    static {
        for (PaginatorType member : PaginatorType.values()) {
            codeMap.put(member.code, member);
        }
    }

    /**
     * Returns the enum constant of this type with the specified code.
     *
     * @param code
     *            code of the enum constant to be returned
     * @return the enum constant with the specified code
     * @throws IllegalArgumentException
     *             if this enum type has no constant with the specified code
     */
    public static PaginatorType valueOf(int code) {
        PaginatorType valueOf = codeMap.get(code);
        if (valueOf == null) {
            throw new IllegalArgumentException("For int: " + code);
        }
        return valueOf;
    }

    /**
     * Code of the enum constant.
     */
    private final int code;

    /**
     * Enum constant constructor that takes a code.
     *
     * @param code
     *            code of the enum constant.
     */
    PaginatorType(int code) {
        this.code = code;
    }

    /**
     * Returns the paginator syntax for the given parameters.
     *
     * @param mode
     *            paginator mode
     * @param value
     *            first value
     * @param fictitious
     *            if true, create fictitious pagination
     * @param separator
     *            separator character
     * @return paginator syntax
     * @throws NumberFormatException
     *             if the number contains invalid digits
     */
    public abstract String format(PaginatorMode mode, String value, boolean fictitious, String separator);
}
