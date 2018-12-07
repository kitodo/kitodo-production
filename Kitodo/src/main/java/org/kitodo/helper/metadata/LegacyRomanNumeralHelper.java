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

package org.kitodo.helper.metadata;

import org.kitodo.api.ugh.RomanNumeralInterface;

/**
 * Connects the legacy Roman numeral to a Roman numeral implementation. This is
 * a soldering class to keep legacy code operational which is about to be
 * removed. Do not use this class.
 */
public class LegacyRomanNumeralHelper implements RomanNumeralInterface {
    /**
     * The value of the Roman numeral accessed via this soldering class.
     */
    private int value;

    @Override
    public String getNumber() {
        return RomanNumeral.format(value, true);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        this.value = RomanNumeral.parseInt(value);
    }

    @Override
    public String toString() {
        return getNumber();
    }
}
