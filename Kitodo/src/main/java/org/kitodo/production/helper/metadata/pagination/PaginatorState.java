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

public enum PaginatorState {

    /**
     * A decimal number.
     */
    DECIMAL,

    /**
     * The buffer is currently empty. (Not a character class.)
     */
    EMPTY,

    /**
     * Final run, clear the buffer. (Fictitious character class and not a buffer
     * state.)
     */
    END,

    /**
     * The next element is only to display if the counter value is full (1, 2, 3).
     * (Not a buffer state.)
     */
    FULL_INTEGER,

    /**
     * The next element is only to display if the counter value is half (1.5, 2.5,
     * 3.5). (Not a buffer state.)
     */
    HALF_INTEGER,

    /**
     * Elements to set the counter increment.
     */
    INCREMENT,

    /**
     * An lower-case Roman numeral. This may still turn into a static text if the
     * next character is a letter.
     */
    LOWERCASE_ROMAN,

    /**
     * Other characters. If the next character is {@code LOWERCASE_ROMAN} or
     * {@code UPPERCASE_ROMAN}, it may be treated as Roman numeral.
     */
    SYMBOL,

    /**
     * Letters. If the next character is of type {@code LOWERCASE_ROMAN} or
     * {@code UPPERCASE_ROMAN}, it will be treated as text.
     */
    TEXT,

    /**
     * A text escape marker. Whatever the buffer contains, treat it as text..
     */
    TEXT_ESCAPE_TRANSITION,

    /**
     * An upper-case Roman numeral. This may still turn into a static text if the
     * next character is a letter.
     */
    UPPERCASE_ROMAN
}
