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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PaginatorTypeTest {

    private static final String UPPERCASE_ROMAN_SEVEN = "VII";
    private static final boolean UNUSED_BOOLEAN = false;
    private static final PaginatorMode UNUSED_PAGINATOR_MODE = null;
    private static final String UNUSED_STRING = null;
    private static final String HELLO_WORLD_STRING = "Hello world!";
    private static final String SEMICOLON_STRING = " ; ";

    @Test
    public void testAdvancedFormat() {
        assertEquals("[(`1`)-1]",
            PaginatorType.ADVANCED.format(UNUSED_PAGINATOR_MODE, "[(`1`)-1]", UNUSED_BOOLEAN, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatColumnsFictiousFromArabic() {
        assertEquals("[7²]", PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, "7", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatColumnsFictiousFromLowercaseRoman() {
        assertEquals("[7²]", PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, "vii", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatColumnsFictiousFromUppercaseRoman() {
        assertEquals("[7²]",
            PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, UPPERCASE_ROMAN_SEVEN, true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatColumnsFromArabic() {
        assertEquals("4²", PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, "4", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatColumnsFromLowercaseRoman() {
        assertEquals("4²", PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, "iv", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatColumnsFromUppercaseRoman() {
        assertEquals("4²", PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, "IV", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatDoublePagesFictiousFromArabic() {
        assertEquals("[9]` ; `[10]", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "9", true, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatDoublePagesFictiousFromLowercaseRoman() {
        assertEquals("[9]` ; `[10]", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "ix", true, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatDoublePagesFictiousFromUppercaseRoman() {
        assertEquals("[9]` ; `[10]", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "IX", true, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatDoublePagesFromArabic() {
        assertEquals("67` ; `68", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "67", false, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatDoublePagesFromLowercaseRoman() {
        assertEquals("67` ; `68", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "lxvii", false, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatDoublePagesFromUppercaseRoman() {
        assertEquals("67` ; `68", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "LXVII", false, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatFoliationFictiousFromArabic() {
        assertEquals("[7½]", PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, "7", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatFoliationFictiousFromLowercaseRoman() {
        assertEquals("[7½]", PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, "vii", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatFoliationFictiousFromUppercaseRoman() {
        assertEquals("[7½]",
            PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, UPPERCASE_ROMAN_SEVEN, true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatFoliationFromArabic() {
        assertEquals("68½", PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, "68", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatFoliationFromLowercaseRoman() {
        assertEquals("68½", PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, "lxviii", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatFoliationFromUppercaseRoman() {
        assertEquals("68½", PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, "LXVIII", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatFromJunk() {
        assertThrows(NumberFormatException.class, () -> PaginatorType.ARABIC.format(UNUSED_PAGINATOR_MODE, "junk", UNUSED_BOOLEAN, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatPagesFictiousFromArabic() {
        assertEquals("[55]", PaginatorType.ARABIC.format(PaginatorMode.PAGES, "55", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatPagesFictiousFromLowercaseRoman() {
        assertEquals("[55]", PaginatorType.ARABIC.format(PaginatorMode.PAGES, "lv", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatPagesFictiousFromUppercaseRoman() {
        assertEquals("[55]", PaginatorType.ARABIC.format(PaginatorMode.PAGES, "LV", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatPagesFromArabic() {
        assertEquals("1", PaginatorType.ARABIC.format(PaginatorMode.PAGES, "1", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatPagesFromLowercaseRoman() {
        assertEquals("1", PaginatorType.ARABIC.format(PaginatorMode.PAGES, "i", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatPagesFromUppercaseRoman() {
        assertEquals("1", PaginatorType.ARABIC.format(PaginatorMode.PAGES, "I", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFictiousFromArabic() {
        assertEquals("[4°]¡r¿`v`½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "4", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFictiousFromLowercaseRoman() {
        assertEquals("[4°]¡r¿`v`½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "iv", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFictiousFromUppercaseRoman() {
        assertEquals("[4°]¡r¿`v`½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "IV", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFictiousFromArabic() {
        assertEquals("[3`]v ; [`4°]r",
            PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "3", true, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFictiousFromLowercaseRoman() {
        assertEquals("[3`]v ; [`4°]r",
            PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "iii", true, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFictiousFromUppercaseRoman() {
        assertEquals("[3`]v ; [`4°]r",
            PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "III", true, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFromArabic() {
        assertEquals("1`v ; `2°r", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "1", false, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFromLowercaseRoman() {
        assertEquals("1`v ; `2°r", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "i", false, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFromUppercaseRoman() {
        assertEquals("1`v ; `2°r", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "I", false, SEMICOLON_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFromArabic() {
        assertEquals("1°¡r¿`v`½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "1", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFromLowercaseRoman() {
        assertEquals("1°¡r¿`v`½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "i", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFromUppercaseRoman() {
        assertEquals("1°¡r¿`v`½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "I", false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatColumns() {
        assertEquals("`Hello world!`²",
            PaginatorType.FREETEXT.format(PaginatorMode.COLUMNS, HELLO_WORLD_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatColumnsFictious() {
        assertEquals("[`Hello world!`²]",
            PaginatorType.FREETEXT.format(PaginatorMode.COLUMNS, HELLO_WORLD_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatDoublePages() {
        assertEquals("`Hello world!`` ; ``Hello world!`",
            PaginatorType.FREETEXT.format(PaginatorMode.DOUBLE_PAGES, HELLO_WORLD_STRING, false, SEMICOLON_STRING));
    }

    @Test
    public void testFreetextFormatDoublePagesFictious() {
        assertEquals("[`Hello world!`]` ; `[`Hello world!`]",
            PaginatorType.FREETEXT.format(PaginatorMode.DOUBLE_PAGES, HELLO_WORLD_STRING, true, SEMICOLON_STRING));
    }

    @Test
    public void testFreetextFormatFoliation() {
        assertEquals("`Hello world!`½",
            PaginatorType.FREETEXT.format(PaginatorMode.FOLIATION, HELLO_WORLD_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatFoliationFictious() {
        assertEquals("[`Hello world!`½]",
            PaginatorType.FREETEXT.format(PaginatorMode.FOLIATION, HELLO_WORLD_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatPages() {
        assertEquals("`Hello world!`",
            PaginatorType.FREETEXT.format(PaginatorMode.PAGES, HELLO_WORLD_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatPagesFictious() {
        assertEquals("[`Hello world!`]",
            PaginatorType.FREETEXT.format(PaginatorMode.PAGES, HELLO_WORLD_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatRectoversoFoliation() {
        assertEquals("`Hello world!`°¡r¿`v`½",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO_FOLIATION, HELLO_WORLD_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatRectoversoFoliationFictious() {
        assertEquals("[`Hello world!`°]¡r¿`v`½",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO_FOLIATION, HELLO_WORLD_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatRectoverso() {
        assertEquals("`Hello world!``v ; ``Hello world!`°r",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO, HELLO_WORLD_STRING, false, SEMICOLON_STRING));
    }

    @Test
    public void testFreetextFormatRectoversoFictious() {
        assertEquals("[`Hello world!``]v ; [``Hello world!`°]r",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO, HELLO_WORLD_STRING, true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatColumnsFictiousFromArabic() {
        assertEquals("[VI²]", PaginatorType.ROMAN.format(PaginatorMode.COLUMNS, "6", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatColumnsFictiousFromLowercaseRoman() {
        assertEquals("[VI²]", PaginatorType.ROMAN.format(PaginatorMode.COLUMNS, "vi", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatColumnsFictiousFromUppercaseRoman() {
        assertEquals("[VI²]", PaginatorType.ROMAN.format(PaginatorMode.COLUMNS, "VI", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatColumnsFromArabic() {
        assertEquals("I²", PaginatorType.ROMAN.format(PaginatorMode.COLUMNS, "1", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatColumnsFromLowercaseRoman() {
        assertEquals("I²", PaginatorType.ROMAN.format(PaginatorMode.COLUMNS, "i", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatColumnsFromUppercaseRoman() {
        assertEquals("I²", PaginatorType.ROMAN.format(PaginatorMode.COLUMNS, "I", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatDoublePagesFictiousFromArabic() {
        assertEquals("[VI]` ; `[VII]", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "6", true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatDoublePagesFictiousFromLowercaseRoman() {
        assertEquals("[VI]` ; `[VII]", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "vi", true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatDoublePagesFictiousFromUppercaseRoman() {
        assertEquals("[VI]` ; `[VII]", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "VI", true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatDoublePagesFromArabic() {
        assertEquals("VI` ; `VII", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "6", false, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatDoublePagesFromLowercaseRoman() {
        assertEquals("VI` ; `VII", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "vi", false, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatDoublePagesFromUppercaseRoman() {
        assertEquals("VI` ; `VII", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "VI", false, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatFoliationFictiousFromArabic() {
        assertEquals("[I½]", PaginatorType.ROMAN.format(PaginatorMode.FOLIATION, "1", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatFoliationFictiousFromLowercaseRoman() {
        assertEquals("[I½]", PaginatorType.ROMAN.format(PaginatorMode.FOLIATION, "i", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatFoliationFictiousFromUppercaseRoman() {
        assertEquals("[I½]", PaginatorType.ROMAN.format(PaginatorMode.FOLIATION, "I", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatFoliationFromArabic() {
        assertEquals("I½", PaginatorType.ROMAN.format(PaginatorMode.FOLIATION, "1", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatFoliationFromLowercaseRoman() {
        assertEquals("I½", PaginatorType.ROMAN.format(PaginatorMode.FOLIATION, "i", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatFoliationFromUppercaseRoman() {
        assertEquals("I½", PaginatorType.ROMAN.format(PaginatorMode.FOLIATION, "I", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatFromJunk() {
        assertThrows(NumberFormatException.class, () ->  PaginatorType.ROMAN.format(UNUSED_PAGINATOR_MODE, "junk", UNUSED_BOOLEAN, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFictiousFromArabic() {
        assertEquals("[XLVIII]", PaginatorType.ROMAN.format(PaginatorMode.PAGES, "48", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFictiousFromLowercaseRoman() {
        assertEquals("[XLVIII]", PaginatorType.ROMAN.format(PaginatorMode.PAGES, "xlviii", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFictiousFromUppercaseRoman() {
        assertEquals("[XLVIII]", PaginatorType.ROMAN.format(PaginatorMode.PAGES, "XLVIII", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFromArabic() {
        assertEquals(UPPERCASE_ROMAN_SEVEN, PaginatorType.ROMAN.format(PaginatorMode.PAGES, "7", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFromLowercaseRoman() {
        assertEquals(UPPERCASE_ROMAN_SEVEN,
            PaginatorType.ROMAN.format(PaginatorMode.PAGES, "vii", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFromUppercaseRoman() {
        assertEquals(UPPERCASE_ROMAN_SEVEN,
            PaginatorType.ROMAN.format(PaginatorMode.PAGES, UPPERCASE_ROMAN_SEVEN, false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFictiousFromArabic() {
        assertEquals("[VIII°]¡r¿`v`½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "8", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFictiousFromLowercaseRoman() {
        assertEquals("[VIII°]¡r¿`v`½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "viii", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFictiousFromUppercaseRoman() {
        assertEquals("[VIII°]¡r¿`v`½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "VIII", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFictiousFromArabic() {
        assertEquals("[I`]v ; [`II°]r",
            PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "1", true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFictiousFromLowercaseRoman() {
        assertEquals("[I`]v ; [`II°]r",
            PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "i", true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFictiousFromUppercaseRoman() {
        assertEquals("[I`]v ; [`II°]r",
            PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "I", true, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFromArabic() {
        assertEquals("I`v ; `II°r", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "1", false, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFromLowercaseRoman() {
        assertEquals("I`v ; `II°r", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "i", false, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFromUppercaseRoman() {
        assertEquals("I`v ; `II°r", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "I", false, SEMICOLON_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFromArabic() {
        assertEquals("VI°¡r¿`v`½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "6", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFromLowercaseRoman() {
        assertEquals("VI°¡r¿`v`½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "vi", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFromUppercaseRoman() {
        assertEquals("VI°¡r¿`v`½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "VI", false, UNUSED_STRING));
    }

    @Test
    public void testAlphabeticFormatColumnsFictious() {
        assertEquals("[´e´²]", PaginatorType.ALPHABETIC.format(PaginatorMode.COLUMNS, "e", true, UNUSED_STRING));
    }

    @Test
    public void testAlphabeticFormatColumns() {
        assertEquals("´a´²", PaginatorType.ALPHABETIC.format(PaginatorMode.COLUMNS, "a", false, UNUSED_STRING));
    }

    @Test
    public void testAlphabeticFormatDoublePagesFictiousFromArabic() {
        assertEquals("[´aa´]` ; `[´ab´]", PaginatorType.ALPHABETIC.format(PaginatorMode.DOUBLE_PAGES, "aa", true, SEMICOLON_STRING));
    }

    @Test
    public void testAlphabeticFormatRectoverso() {
        assertEquals("´g´`v ; `´h´°r", PaginatorType.ALPHABETIC.format(PaginatorMode.RECTOVERSO, "g", false, SEMICOLON_STRING));
    }

    @Test
    public void testAlphabeticFormatRectoversoFoliation() {
        assertEquals("´m´°¡r¿`v`½", PaginatorType.ALPHABETIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "m", false, UNUSED_STRING));
    }



    @Test
    public void testUncountedFormatColumns() {
        assertEquals("`uncounted`²",
            PaginatorType.UNCOUNTED.format(PaginatorMode.COLUMNS, UNUSED_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatColumnsFictious() {
        assertEquals("[`uncounted`²]",
            PaginatorType.UNCOUNTED.format(PaginatorMode.COLUMNS, UNUSED_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatDoublePages() {
        assertEquals("`uncounted`` ; ``uncounted`",
            PaginatorType.UNCOUNTED.format(PaginatorMode.DOUBLE_PAGES, UNUSED_STRING, false, SEMICOLON_STRING));
    }

    @Test
    public void testUncountedFormatDoublePagesFictious() {
        assertEquals("[`uncounted`]` ; `[`uncounted`]",
            PaginatorType.UNCOUNTED.format(PaginatorMode.DOUBLE_PAGES, UNUSED_STRING, true, SEMICOLON_STRING));
    }

    @Test
    public void testUncountedFormatFoliation() {
        assertEquals("`uncounted`½",
            PaginatorType.UNCOUNTED.format(PaginatorMode.FOLIATION, UNUSED_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatFoliationFictious() {
        assertEquals("[`uncounted`½]",
            PaginatorType.UNCOUNTED.format(PaginatorMode.FOLIATION, UNUSED_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatPages() {
        assertEquals("`uncounted`",
            PaginatorType.UNCOUNTED.format(PaginatorMode.PAGES, UNUSED_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatPagesFictious() {
        assertEquals("[`uncounted`]",
            PaginatorType.UNCOUNTED.format(PaginatorMode.PAGES, UNUSED_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatRectoversoFoliation() {
        assertEquals("`uncounted`°¡r¿`v`½",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO_FOLIATION, UNUSED_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatRectoversoFoliationFictious() {
        assertEquals("[`uncounted`°]¡r¿`v`½",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO_FOLIATION, UNUSED_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatRectoverso() {
        assertEquals("`uncounted``v ; ``uncounted`°r",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO, UNUSED_STRING, false, SEMICOLON_STRING));
    }

    @Test
    public void testUncountedFormatRectoversoFictious() {
        assertEquals("[`uncounted``]v ; [``uncounted`°]r",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO, UNUSED_STRING, true, SEMICOLON_STRING));
    }

    @Test
    public void testValueOfOne() {
        assertEquals(PaginatorType.ARABIC, PaginatorType.valueOf(1));
    }

    @Test
    public void testValueOfTwo() {
        assertEquals(PaginatorType.ROMAN, PaginatorType.valueOf(2));
    }

    @Test
    public void testValueOfThree() {
        assertEquals(PaginatorType.UNCOUNTED, PaginatorType.valueOf(3));
    }

    @Test
    public void testValueOfFour() {
        assertEquals(PaginatorType.ALPHABETIC, PaginatorType.valueOf(4));
    }

    @Test
    public void testValueOfFourtytwo() {
        assertThrows(IllegalArgumentException.class, () -> PaginatorType.valueOf(42));
    }

    @Test
    public void testValueOfSix() {
        assertEquals(PaginatorType.FREETEXT, PaginatorType.valueOf(6));
    }

    @Test
    public void testValueOfNinetynine() {
        assertEquals(PaginatorType.ADVANCED, PaginatorType.valueOf(99));
    }
}
