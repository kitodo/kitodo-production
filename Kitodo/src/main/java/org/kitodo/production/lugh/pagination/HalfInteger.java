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
package org.kitodo.production.lugh.pagination;

/**
 * A half integer is an integer that supports counting halfs (½, 1, 1½, 2, …).
 *
 * @author Matthias Ronge
 */
public class HalfInteger extends Number {
    private static final long serialVersionUID = 1L;

    /**
     * A constant value of one.
     */
    static final HalfInteger ONE = new HalfInteger(1, false);

    static HalfInteger valueOf(String string) {
        int x = 0, y = 0;
        boolean z = false;
        final int u = string.length();
        for (int i = 0; i < u;) {
            final int codePoint = string.codePointAt(i);
            x *= 10;
            switch (codePoint) {
                case '°':
                break;
                case '²':
                    x += 2;
                break;
                case '³':
                    x += 3;
                break;
                case '¹':
                    x += 1;
                break;
                case '½':
                    if (z) y++;
                    z = !z;
                case '⁰':
                break;
                case '⁴':
                    x += 4;
                break;
                case '⁵':
                    x += 5;
                break;
                case '⁶':
                    x += 6;
                break;
                case '⁷':
                    x += 7;
                break;
                case '⁸':
                    x += 8;
                break;
                case '⁹':
                    x += 9;
                break;
                default:
                    throw new IllegalArgumentException("For string: " + string);
            }
            i += Character.charCount(codePoint);
        }
        return new HalfInteger(x + y, z);
    }

    private final boolean aHalf;

    private final int value;

    /**
     * Creates a new half integer.
     * @param value integer value
     * @param aHalf if true, is one half above the value
     */
    public HalfInteger(int value, boolean aHalf) {
        this.value = value;
        this.aHalf = aHalf;
    }

    HalfInteger add(HalfInteger increment) {
        if (increment == null) return this;
        if (aHalf && increment.aHalf) {
            return new HalfInteger(value + increment.value + 1, false);
        } else {
            return new HalfInteger(value + increment.value, aHalf || increment.aHalf);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return aHalf ? value + .5 : value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return aHalf ? value + .5f : value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This instance returns the value <b>rounded down</b>, which is critical
     * for the package to work as expected.
     */
    @Override
    public int intValue() {
        return value;
    }

    boolean isHalf() {
        return aHalf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns a concise string representation of this instance.
     *
     * @return a string representing this instance
     */
    @Override
    public String toString() {
        return aHalf ? Integer.toString(value).concat("½") : Integer.toString(value);
    }
}
