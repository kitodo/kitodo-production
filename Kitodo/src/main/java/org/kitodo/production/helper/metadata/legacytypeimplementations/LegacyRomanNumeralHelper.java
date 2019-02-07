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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

import org.kitodo.production.helper.metadata.RomanNumeral;

/**
 * Connects the legacy Roman numeral to a Roman numeral implementation. This is
 * a soldering class to keep legacy code operational which is about to be
 * removed. Do not use this class.
 */
public class LegacyRomanNumeralHelper {
    /**
     * The value of the Roman numeral accessed via this soldering class.
     */
    private int value;

    public String getNumber() {
        return RomanNumeral.format(value, true);
    }

    public int intValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = RomanNumeral.parseInt(value);
    }

    @Override
    public String toString() {
        return getNumber();
    }
}
