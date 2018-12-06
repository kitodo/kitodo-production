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

package org.kitodo.legacy.joining;

import org.kitodo.api.ugh.RomanNumeralInterface;
import org.kitodo.production.lugh.pagination.RomanNumeral;

public class RomanNumeralJoint implements RomanNumeralInterface {

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
}
