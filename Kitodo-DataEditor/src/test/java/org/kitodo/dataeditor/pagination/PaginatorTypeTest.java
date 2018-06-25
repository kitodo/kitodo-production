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

package org.kitodo.dataeditor.pagination;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PaginatorTypeTest {

    private static final boolean UNUSED_BOOLEAN = false;
    private static final PaginatorMode UNUSED_PAGINATOR_MODE = null;
    private static final String UNUSED_STRING = null;

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
        assertEquals("[7²]", PaginatorType.ARABIC.format(PaginatorMode.COLUMNS, "VII", true, UNUSED_STRING));
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
        assertEquals("[9]` ; `[10]", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "9", true, " ; "));
    }

    @Test
    public void testArabicFormatDoublePagesFictiousFromLowercaseRoman() {
        assertEquals("[9]` ; `[10]", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "ix", true, " ; "));
    }

    @Test
    public void testArabicFormatDoublePagesFictiousFromUppercaseRoman() {
        assertEquals("[9]` ; `[10]", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "IX", true, " ; "));
    }

    @Test
    public void testArabicFormatDoublePagesFromArabic() {
        assertEquals("67` ; `68", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "67", false, " ; "));
    }

    @Test
    public void testArabicFormatDoublePagesFromLowercaseRoman() {
        assertEquals("67` ; `68", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "lxvii", false, " ; "));
    }

    @Test
    public void testArabicFormatDoublePagesFromUppercaseRoman() {
        assertEquals("67` ; `68", PaginatorType.ARABIC.format(PaginatorMode.DOUBLE_PAGES, "LXVII", false, " ; "));
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
        assertEquals("[7½]", PaginatorType.ARABIC.format(PaginatorMode.FOLIATION, "VII", true, UNUSED_STRING));
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

    @Test(expected = NumberFormatException.class)
    public void testArabicFormatFromJunk() {
        PaginatorType.ARABIC.format(UNUSED_PAGINATOR_MODE, "junk", UNUSED_BOOLEAN, UNUSED_STRING);
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
    public void testArabicFormatRectoversoFictiousFromArabic() {
        assertEquals("[4°]¡r¿v½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "4", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFictiousFromLowercaseRoman() {
        assertEquals("[4°]¡r¿v½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "iv", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFictiousFromUppercaseRoman() {
        assertEquals("[4°]¡r¿v½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "IV", true, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFictiousFromArabic() {
        assertEquals("[3`]v ; [`4°]r",
            PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "3", true, " ; "));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFictiousFromLowercaseRoman() {
        assertEquals("[3`]v ; [`4°]r",
            PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "iii", true, " ; "));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFictiousFromUppercaseRoman() {
        assertEquals("[3`]v ; [`4°]r",
            PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "III", true, " ; "));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFromArabic() {
        assertEquals("1`v ; `2°r", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "1", false, " ; "));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFromLowercaseRoman() {
        assertEquals("1`v ; `2°r", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "i", false, " ; "));
    }

    @Test
    public void testArabicFormatRectoversoFoliationFromUppercaseRoman() {
        assertEquals("1`v ; `2°r", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO_FOLIATION, "I", false, " ; "));
    }

    @Test
    public void testArabicFormatRectoversoFromArabic() {
        assertEquals("1°¡r¿v½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "1", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFromLowercaseRoman() {
        assertEquals("1°¡r¿v½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "i", false, UNUSED_STRING));
    }

    @Test
    public void testArabicFormatRectoversoFromUppercaseRoman() {
        assertEquals("1°¡r¿v½", PaginatorType.ARABIC.format(PaginatorMode.RECTOVERSO, "I", false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatColumns() {
        assertEquals("`Hello world!`²",
            PaginatorType.FREETEXT.format(PaginatorMode.COLUMNS, "Hello world!", false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatColumnsFictious() {
        assertEquals("[`Hello world!`²]",
            PaginatorType.FREETEXT.format(PaginatorMode.COLUMNS, "Hello world!", true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatDoublePages() {
        assertEquals("`Hello world!`` ; ``Hello world!`",
            PaginatorType.FREETEXT.format(PaginatorMode.DOUBLE_PAGES, "Hello world!", false, " ; "));
    }

    @Test
    public void testFreetextFormatDoublePagesFictious() {
        assertEquals("[`Hello world!`]` ; `[`Hello world!`]",
            PaginatorType.FREETEXT.format(PaginatorMode.DOUBLE_PAGES, "Hello world!", true, " ; "));
    }

    @Test
    public void testFreetextFormatFoliation() {
        assertEquals("`Hello world!`½",
            PaginatorType.FREETEXT.format(PaginatorMode.FOLIATION, "Hello world!", false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatFoliationFictious() {
        assertEquals("[`Hello world!`½]",
            PaginatorType.FREETEXT.format(PaginatorMode.FOLIATION, "Hello world!", true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatPages() {
        assertEquals("`Hello world!`",
            PaginatorType.FREETEXT.format(PaginatorMode.PAGES, "Hello world!", false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatPagesFictious() {
        assertEquals("[`Hello world!`]",
            PaginatorType.FREETEXT.format(PaginatorMode.PAGES, "Hello world!", true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatRectoverso() {
        assertEquals("`Hello world!`°¡r¿v½",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO, "Hello world!", false, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatRectoversoFictious() {
        assertEquals("[`Hello world!`°]¡r¿v½",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO, "Hello world!", true, UNUSED_STRING));
    }

    @Test
    public void testFreetextFormatRectoversoFoliation() {
        assertEquals("`Hello world!``v ; ``Hello world!`°r",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO_FOLIATION, "Hello world!", false, " ; "));
    }

    @Test
    public void testFreetextFormatRectoversoFoliationFictious() {
        assertEquals("[`Hello world!``]v ; [``Hello world!`°]r",
            PaginatorType.FREETEXT.format(PaginatorMode.RECTOVERSO_FOLIATION, "Hello world!", true, " ; "));
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
        assertEquals("[VI]` ; `[VII]", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "6", true, " ; "));
    }

    @Test
    public void testRomanFormatDoublePagesFictiousFromLowercaseRoman() {
        assertEquals("[VI]` ; `[VII]", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "vi", true, " ; "));
    }

    @Test
    public void testRomanFormatDoublePagesFictiousFromUppercaseRoman() {
        assertEquals("[VI]` ; `[VII]", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "VI", true, " ; "));
    }

    @Test
    public void testRomanFormatDoublePagesFromArabic() {
        assertEquals("VI` ; `VII", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "6", false, " ; "));
    }

    @Test
    public void testRomanFormatDoublePagesFromLowercaseRoman() {
        assertEquals("VI` ; `VII", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "vi", false, " ; "));
    }

    @Test
    public void testRomanFormatDoublePagesFromUppercaseRoman() {
        assertEquals("VI` ; `VII", PaginatorType.ROMAN.format(PaginatorMode.DOUBLE_PAGES, "VI", false, " ; "));
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

    @Test(expected = NumberFormatException.class)
    public void testRomanFormatFromJunk() {
        PaginatorType.ROMAN.format(UNUSED_PAGINATOR_MODE, "junk", UNUSED_BOOLEAN, UNUSED_STRING);
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
        assertEquals("VII", PaginatorType.ROMAN.format(PaginatorMode.PAGES, "7", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFromLowercaseRoman() {
        assertEquals("VII", PaginatorType.ROMAN.format(PaginatorMode.PAGES, "vii", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatPagesFromUppercaseRoman() {
        assertEquals("VII", PaginatorType.ROMAN.format(PaginatorMode.PAGES, "VII", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFictiousFromArabic() {
        assertEquals("[VIII°]¡r¿v½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "8", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFictiousFromLowercaseRoman() {
        assertEquals("[VIII°]¡r¿v½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "viii", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFictiousFromUppercaseRoman() {
        assertEquals("[VIII°]¡r¿v½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "VIII", true, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFictiousFromArabic() {
        assertEquals("[I`]v ; [`II°]r",
            PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "1", true, " ; "));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFictiousFromLowercaseRoman() {
        assertEquals("[I`]v ; [`II°]r",
            PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "i", true, " ; "));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFictiousFromUppercaseRoman() {
        assertEquals("[I`]v ; [`II°]r",
            PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "I", true, " ; "));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFromArabic() {
        assertEquals("I`v ; `II°r", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "1", false, " ; "));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFromLowercaseRoman() {
        assertEquals("I`v ; `II°r", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "i", false, " ; "));
    }

    @Test
    public void testRomanFormatRectoversoFoliationFromUppercaseRoman() {
        assertEquals("I`v ; `II°r", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO_FOLIATION, "I", false, " ; "));
    }

    @Test
    public void testRomanFormatRectoversoFromArabic() {
        assertEquals("VI°¡r¿v½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "6", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFromLowercaseRoman() {
        assertEquals("VI°¡r¿v½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "vi", false, UNUSED_STRING));
    }

    @Test
    public void testRomanFormatRectoversoFromUppercaseRoman() {
        assertEquals("VI°¡r¿v½", PaginatorType.ROMAN.format(PaginatorMode.RECTOVERSO, "VI", false, UNUSED_STRING));
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
            PaginatorType.UNCOUNTED.format(PaginatorMode.DOUBLE_PAGES, UNUSED_STRING, false, " ; "));
    }

    @Test
    public void testUncountedFormatDoublePagesFictious() {
        assertEquals("[`uncounted`]` ; `[`uncounted`]",
            PaginatorType.UNCOUNTED.format(PaginatorMode.DOUBLE_PAGES, UNUSED_STRING, true, " ; "));
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
    public void testUncountedFormatRectoverso() {
        assertEquals("`uncounted`°¡r¿v½",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO, UNUSED_STRING, false, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatRectoversoFictious() {
        assertEquals("[`uncounted`°]¡r¿v½",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO, UNUSED_STRING, true, UNUSED_STRING));
    }

    @Test
    public void testUncountedFormatRectoversoFoliation() {
        assertEquals("`uncounted``v ; ``uncounted`°r",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO_FOLIATION, UNUSED_STRING, false, " ; "));
    }

    @Test
    public void testUncountedFormatRectoversoFoliationFictious() {
        assertEquals("[`uncounted``]v ; [``uncounted`°]r",
            PaginatorType.UNCOUNTED.format(PaginatorMode.RECTOVERSO_FOLIATION, UNUSED_STRING, true, " ; "));
    }

    @Test
    public void testValueOf1() {
        assertEquals(PaginatorType.ARABIC, PaginatorType.valueOf(1));
    }

    @Test
    public void testValueOf2() {
        assertEquals(PaginatorType.ROMAN, PaginatorType.valueOf(2));
    }

    @Test
    public void testValueOf3() {
        assertEquals(PaginatorType.UNCOUNTED, PaginatorType.valueOf(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf42() {
        PaginatorType.valueOf(42);
    }

    @Test
    public void testValueOf6() {
        assertEquals(PaginatorType.FREETEXT, PaginatorType.valueOf(6));
    }

    @Test
    public void testValueOf99() {
        assertEquals(PaginatorType.ADVANCED, PaginatorType.valueOf(99));
    }
}

