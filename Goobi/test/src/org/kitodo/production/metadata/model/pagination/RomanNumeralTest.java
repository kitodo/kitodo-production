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
package org.kitodo.production.metadata.model.pagination;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kitodo.production.lugh.pagination.*;

public class RomanNumeralTest {

    @Test
    public void outputInputTestLowercase() {
        RomanNumeral rn = new RomanNumeral("i", false);

        for (int i = 1; i <= 4999; i++) {
            String string = rn.format(new HalfInteger(i, false));
            RomanNumeral r2 = new RomanNumeral(string, false);
            assertEquals(i, r2.intValue());
        }
    }

    @Test
    public void outputInputTestUppercase() {
        RomanNumeral rn = new RomanNumeral("i", true);

        for (int i = 1; i <= 4999; i++) {
            String string = rn.format(new HalfInteger(i, false));
            RomanNumeral r2 = new RomanNumeral(string, false);
            assertEquals(i, r2.intValue());
        }
    }
}
