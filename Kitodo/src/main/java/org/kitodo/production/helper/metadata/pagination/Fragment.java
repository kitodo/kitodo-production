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

/**
 * A fragment of a pagination sequence. Paginations can be freely combined out
 * of fragments that can be a {@link DecimalNumeral}, a {@link RomanNumeral} or
 * {@link StaticText}.
 */
interface Fragment {
    /**
     * Returns the value formatted according to the type and configuration of the
     * fragment. For example, a value of 3.5 may be formatted as "3" (decimal),
     * "III" (roman), or "v" (text, for ‘verso’, the back of a sheet).
     * {@code DecimalNumeral} and {@code RomanNumeral} will ignore the half part of
     * the integer, where {@code StaticText} may return text or an empty string for
     * full and half values.
     *
     * @param value
     *            value to format
     * @return the formatted value
     */
    String format(HalfInteger value);

    /**
     * Returns the increment associated with the fragment. For example, to get a
     * pagination with an interval of two (like 1, 3, 5, 7, 9, …), the
     * {@code DecimalNumeral} has to be configured with an interval value of 2.0.
     *
     * @return the increment of fragment
     */
    HalfInteger getIncrement();

    /**
     * Returns the initial value of the fragment. This is where pagination will be
     * started.
     *
     * @return the pagination start value
     */
    Integer getInitialValue();

    /**
     * Sets the increment of the fragment.
     *
     * @param increment
     *            increment value the fragment will cause
     */
    void setIncrement(HalfInteger increment);
}
