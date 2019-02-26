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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.kitodo.production.helper.metadata.pagination.RomanNumeral;

public class RomanNumeralTest {

    @Test
    public void testOutputInputLowercase() {
        for (int i = 1; i <= 4999; i++) {
            String encoded = RomanNumeral.format(i, false);
            int decoded = RomanNumeral.parseInt(encoded);
            assertEquals(i, decoded);
        }
    }

    @Test
    public void testOutputInputUppercase() {
        for (int i = 1; i <= 4999; i++) {
            String encoded = RomanNumeral.format(i, true);
            int decoded = RomanNumeral.parseInt(encoded);
            assertEquals(i, decoded);
        }
    }

    @Test
    public void testParseIntUppercase() {
        assertEquals(175, RomanNumeral.parseInt("CLXXV"));
        assertEquals(907, RomanNumeral.parseInt("CMVII"));
        assertEquals(1520, RomanNumeral.parseInt("MDXX"));
        assertEquals(4937, RomanNumeral.parseInt("MMMMCMXXXVII"));
    }

    @Test
    public void testParseIntLowercase() {
        assertEquals(16, RomanNumeral.parseInt("xvi"));
        assertEquals(554, RomanNumeral.parseInt("dliv"));
        assertEquals(393, RomanNumeral.parseInt("cccxciii"));
        assertEquals(4478, RomanNumeral.parseInt("mmmmcdlxxviii"));
    }

    @Test
    public void testFormatUppercase() {
        assertEquals("XV", RomanNumeral.format(15, true));
        assertEquals("CDI", RomanNumeral.format(401, true));
        assertEquals("CMII", RomanNumeral.format(902, true));
        assertEquals("MMMMCDXLIII", RomanNumeral.format(4443, true));
    }

    @Test
    public void testFormatLowercase() {
        assertEquals("xxv", RomanNumeral.format(25, false));
        assertEquals("miii", RomanNumeral.format(1003, false));
        assertEquals("cmxii", RomanNumeral.format(912, false));
        assertEquals("mmmmdcclxvi", RomanNumeral.format(4766, false));
    }
}
