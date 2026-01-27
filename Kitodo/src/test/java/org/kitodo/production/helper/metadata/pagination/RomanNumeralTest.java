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
            RomanNumeral romanNumeral = new RomanNumeral(null, false, null);
            String encoded = romanNumeral.format(new HalfInteger(i, false));
            int decoded = RomanNumeral.parseInt(encoded);
            assertEquals(i, decoded);
        }
    }

    @Test
    public void testOutputInputUppercase() {
        for (int i = 1; i <= 4999; i++) {
            RomanNumeral romanNumeral = new RomanNumeral(null, true, null);
            String encoded = romanNumeral.format(new HalfInteger(i, false));
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
        RomanNumeral romanNumeral = new RomanNumeral(null, true, null);
        assertEquals("XV", romanNumeral.format(new HalfInteger(15, false)));
        assertEquals("CDI", romanNumeral.format(new HalfInteger(401, false)));
        assertEquals("CMII", romanNumeral.format(new HalfInteger(902, false)));
        assertEquals("MMMMCDXLIII", romanNumeral.format(new HalfInteger(4443, false)));
    }

    @Test
    public void testFormatLowercase() {
        RomanNumeral romanNumeral = new RomanNumeral(null, false, null);
        assertEquals("xxv", romanNumeral.format(new HalfInteger(25, false)));
        assertEquals("miii", romanNumeral.format(new HalfInteger(1003, false)));
        assertEquals("cmxii", romanNumeral.format(new HalfInteger(912, false)));
        assertEquals("mmmmdcclxvi", romanNumeral.format(new HalfInteger(4766, false)));
    }
}
