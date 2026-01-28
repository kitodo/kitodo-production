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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RomanNumeralTest {

    @Test
    public void testOutputInputLowercase() {
        for (int i = 1; i <= 4999; i++) {
            String encoded = RomanNumeral.format(new HalfInteger(i, false), false, null);
            int decoded = RomanNumeral.parseInt(encoded);
            assertEquals(i, decoded);
        }
    }

    @Test
    public void testOutputInputUppercase() {
        for (int i = 1; i <= 4999; i++) {
            String encoded = RomanNumeral.format(new HalfInteger(i, false), true, null);
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
        assertEquals("XV", RomanNumeral.format(new HalfInteger(15, false), true, null));
        assertEquals("CDI", RomanNumeral.format(new HalfInteger(401, false), true, null));
        assertEquals("CMII", RomanNumeral.format(new HalfInteger(902, false), true, null));
        assertEquals("MMMMCDXLIII", RomanNumeral.format(new HalfInteger(4443, false), true, null));
    }

    @Test
    public void testFormatLowercase() {
        assertEquals("xxv", RomanNumeral.format(new HalfInteger(25, false), false, null));
        assertEquals("miii", RomanNumeral.format(new HalfInteger(1003, false), false, null));
        assertEquals("cmxii", RomanNumeral.format(new HalfInteger(912, false), false, null));
        assertEquals("mmmmdcclxvi", RomanNumeral.format(new HalfInteger(4766, false), false, null));
    }
}
