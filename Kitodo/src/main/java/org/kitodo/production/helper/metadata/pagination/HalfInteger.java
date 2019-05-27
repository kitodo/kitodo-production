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

public class HalfInteger extends Number {
    private final boolean halfAboveValue;
    private final int value;

    static HalfInteger valueOf(String string) {
        int mainIncrement = 0;
        int halfIncrement = 0;
        boolean doHalfIncrement = false;
        for (int i = 0; i < string.length();) {
            final int codePoint = string.codePointAt(i);
            mainIncrement *= 10;
            switch (codePoint) {
                case '°':
                case '⁰':
                    break;
                case '¹':
                    mainIncrement += 1;
                    break;
                case '²':
                    mainIncrement += 2;
                    break;
                case '³':
                    mainIncrement += 3;
                    break;
                case '½':
                    if (doHalfIncrement) {
                        halfIncrement++;
                    }
                    doHalfIncrement = !doHalfIncrement;
                    break;
                default:
                    throw new IllegalArgumentException("For string: " + string);
            }
            i += Character.charCount(codePoint);
        }
        return new HalfInteger(mainIncrement + halfIncrement, doHalfIncrement);
    }

    /**
     * Creates a new half integer.
     * 
     * @param value
     *            integer value
     * @param halfAboveValue
     *            if true, is one half above the value
     */
    public HalfInteger(int value, boolean halfAboveValue) {
        this.value = value;
        this.halfAboveValue = halfAboveValue;
    }

    HalfInteger add(HalfInteger increment) {
        if (increment == null) {
            return this;
        }
        if (this.halfAboveValue && increment.halfAboveValue) {
            return new HalfInteger(value + increment.value + 1, false);
        } else {
            return new HalfInteger(value + increment.value, halfAboveValue || increment.halfAboveValue);
        }
    }

    @Override
    public double doubleValue() {
        return halfAboveValue ? value + .5 : value;
    }

    @Override
    public float floatValue() {
        return halfAboveValue ? value + .5f : value;
    }

    @Override
    public int intValue() {
        return value;
    }

    boolean isHalf() {
        return halfAboveValue;
    }

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
        return halfAboveValue ? Integer.toString(value).concat("½") : Integer.toString(value);
    }
}
