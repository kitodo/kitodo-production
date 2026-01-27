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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlphabeticNumeralTest {

    @Test
    public void testOutputInput() {
        for (int i = 1; i <= 4999; i++) {
            AlphabeticNumeral alphabeticNumeral = new AlphabeticNumeral(null, null);
            String encoded = alphabeticNumeral.format(i);
            int decoded = AlphabeticNumeral.parseInt(encoded);
            assertEquals(i, decoded);
        }
    }

    @Test
    public void testParseInt() {
        assertEquals(13, AlphabeticNumeral.parseInt("m"));
        assertEquals(175, AlphabeticNumeral.parseInt("fs"));
        assertEquals(907, AlphabeticNumeral.parseInt("ahw"));
        assertEquals(1520, AlphabeticNumeral.parseInt("bfl"));
        assertEquals(4937, AlphabeticNumeral.parseInt("ggw"));
    }

    @Test
    public void testFormat() {
        AlphabeticNumeral alphabeticNumeral = new AlphabeticNumeral(null, null);
        assertEquals("c", alphabeticNumeral.format(3));
        assertEquals("z", alphabeticNumeral.format(26));
        assertEquals("bb", alphabeticNumeral.format(54));
        assertEquals("qwf", alphabeticNumeral.format(12096));
    }
}
