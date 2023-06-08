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
 * Constants for the pagination modes selectable by image buttons.
 */
public enum PaginatorMode {
    /**
     * Only even, or only odd numbers, always skipping one (1, 3, 5, 7, … / [1],
     * [3], [5], [7], …).
     */
    COLUMNS(2) {
        @Override
        String format(String value, String next, boolean fictitious, String separator) {
            if (fictitious) {
                return '[' + value + "²]";
            } else {
                return value.concat("²");
            }
        }
    },

    /**
     * Two subsequent numbers on one image (1␣2, 3␣4, 5␣6, 7␣8, … / [1]␣[2],
     * [3]␣[4], [5]␣[6], [7]␣[8], …).
     */
    DOUBLE_PAGES(6) {
        @Override
        String format(String value, String next, boolean fictitious, String separator) {
            if (fictitious) {
                return '[' + value + "]`" + separator + "`[" + next + "]";
            } else {
                return value + "`" + separator + '`' + next;
            }
        }
    },

    /**
     * Each digit appears two times (1, 1, 2, 2, … / [1], [1], [2], [2], …).
     */
    FOLIATION(3) {
        @Override
        String format(String value, String next, boolean fictitious, String separator) {
            if (fictitious) {
                return '[' + value + "½]";
            } else {
                return value.concat("½");
            }
        }
    },

    /**
     * Normal pagination (1, 2, 3, 4, … / [1], [2], [3], [4], …).
     */
    PAGES(1) {
        @Override
        String format(String value, String next, boolean fictitious, String separator) {
            if (fictitious) {
                return '[' + value + "]";
            } else {
                return value;
            }
        }
    },

    /**
     * One back side (“v”) with the subsequent front side (“r”) on one image (1v␣2r,
     * 2v␣3r, 3v␣4r, 4v␣5r, … / [1]v␣[2]r, [2]v␣[3]r, [3]v␣[4]r, [4]v␣[5]r, …).
     */
    RECTOVERSO(4) {
        @Override
        String format(String value, String next, boolean fictitious, String separator) {
            if (fictitious) {
                return '[' + value + "`]v" + separator + "[`" + next + "°]r";
            } else {
                return value + "`v" + separator + '`' + next + "°r";
            }
        }
    },

    /**
     * Alternating a front side (“r”), then a back side (“v”) (1r, 1v, 2r, 2v, … /
     * [1]r, [1]v, [2]r, [2]v, …).
     */
    RECTOVERSO_FOLIATION(5) {
        @Override
        String format(String value, String next, boolean fictitious, String separator) {
            if (fictitious) {
                return '[' + value + "°]¡r¿v½";
            } else {
                return value.concat("°¡r¿v½");
            }
        }
    };

    /**
     * Map for the look-up of codes.
     */
    private static final Map<Integer, PaginatorMode> codeMap = new HashMap<>(
            (int) Math.ceil(values().length / .75));

    static {
        for (PaginatorMode member : PaginatorMode.values()) {
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
    public static PaginatorMode valueOf(int code) {
        PaginatorMode valueOf = codeMap.get(code);
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
    PaginatorMode(int code) {
        this.code = code;
    }

    abstract String format(String value, String next, boolean fictitious, String separator);

}
