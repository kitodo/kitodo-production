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

public class PaginatorTest {

    @Test
    public void firstPagination() {
        Paginator paginator = new Paginator("[`0`-(1)]");
        assertEquals("[0-(1)]", paginator.next());
        assertEquals("[0-(2)]", paginator.next());
        assertEquals("[0-(3)]", paginator.next());
        assertEquals("[0-(4)]", paginator.next());
    }

    @Test
    public void secondPagination() {
        Paginator paginator = new Paginator("[`1`-1²]");
        assertEquals("[1-1]", paginator.next());
        assertEquals("[1-3]", paginator.next());
        assertEquals("[1-5]", paginator.next());
        assertEquals("[1-7]", paginator.next());
    }

    @Test
    public void thirdPagination() {
        Paginator paginator = new Paginator("[`1`]-5²");
        assertEquals("[1]-5", paginator.next());
        assertEquals("[1]-7", paginator.next());
        assertEquals("[1]-9", paginator.next());
        assertEquals("[1]-11", paginator.next());
    }

    @Test
    public void fourthPagination() {
        Paginator paginator = new Paginator("([`1`]-11²)");
        assertEquals("([1]-11)", paginator.next());
        assertEquals("([1]-13)", paginator.next());
        assertEquals("([1]-15)", paginator.next());
    }

    @Test
    public void interleavePagination() {
        Paginator paginator = new Paginator("1²");
        assertEquals("1", paginator.next());
        assertEquals("3", paginator.next());
        assertEquals("5", paginator.next());
        assertEquals("7", paginator.next());
        assertEquals("9", paginator.next());
    }

    @Test
    public void rectoVersoOnOnePagePagination() {
        Paginator paginator = new Paginator("1`v` 2°r");
        assertEquals("1v 2r", paginator.next());
        assertEquals("2v 3r", paginator.next());
        assertEquals("3v 4r", paginator.next());
    }

    @Test
    public void interleavePaginationHalf() {
        Paginator paginator = new Paginator("1½");
        assertEquals("1", paginator.next());
        assertEquals("1", paginator.next());
        assertEquals("2", paginator.next());
        assertEquals("2", paginator.next());
        assertEquals("3", paginator.next());
    }

    @Test
    public void inteleavePaginationThree() {
        Paginator paginator = new Paginator("1³");
        assertEquals("1", paginator.next());
        assertEquals("4", paginator.next());
        assertEquals("7", paginator.next());
        assertEquals("10", paginator.next());
        assertEquals("13", paginator.next());
    }

    @Test
    public void rectoVersoPagination() {
        Paginator paginator = new Paginator("1° ¡r¿`v`½");
        assertEquals("1 r", paginator.next());
        assertEquals("1 v", paginator.next());
        assertEquals("2 r", paginator.next());
        assertEquals("2 v", paginator.next());
    }

    @Test
    public void rectoVersoPaginationStartRight() {
        Paginator paginator = new Paginator("½1° ¡r¿`v`½");
        assertEquals("1 v", paginator.next());
        assertEquals("2 r", paginator.next());
        assertEquals("2 v", paginator.next());
        assertEquals("3 r", paginator.next());
        assertEquals("3 v", paginator.next());
    }

    @Test
    public void romanPaginationLowercase() {
        Paginator p = new Paginator("i");
        assertEquals("i", p.next());
        assertEquals("ii", p.next());
        assertEquals("iii", p.next());
        assertEquals("iv", p.next());
        assertEquals("v", p.next());
    }

    @Test
    public void romanPaginationUppercase() {
        Paginator paginator = new Paginator("I");
        assertEquals("I", paginator.next());
        assertEquals("II", paginator.next());
        assertEquals("III", paginator.next());
        assertEquals("IV", paginator.next());
        assertEquals("V", paginator.next());
    }

    @Test
    public void simplePagination() {
        Paginator paginator = new Paginator("1");
        assertEquals("1", paginator.next());
        assertEquals("2", paginator.next());
        assertEquals("3", paginator.next());
        assertEquals("4", paginator.next());
        assertEquals("5", paginator.next());
        assertEquals("6", paginator.next());
        assertEquals("7", paginator.next());
    }

    @Test
    public void twoColumnPagination() {
        Paginator paginator = new Paginator("1 2");
        assertEquals("1 2", paginator.next());
        assertEquals("3 4", paginator.next());
        assertEquals("5 6", paginator.next());
        assertEquals("7 8", paginator.next());
        assertEquals("9 10", paginator.next());
    }

    @Test
    public void twoColumnPaginationRightToLeft() {
        Paginator paginator = new Paginator("2 1");
        assertEquals("2 1", paginator.next());
        assertEquals("4 3", paginator.next());
        assertEquals("6 5", paginator.next());
        assertEquals("8 7", paginator.next());
        assertEquals("10 9", paginator.next());
    }

    @Test
    public void ignoreRomanNumeralsThatArePartsOfWordsWithLatin() {
        Paginator paginator = new Paginator("Kapitel 1");
        assertEquals("Kapitel 1", paginator.next());
        assertEquals("Kapitel 2", paginator.next());
        assertEquals("Kapitel 3", paginator.next());
        assertEquals("Kapitel 4", paginator.next());
    }

    @Test
    public void ignoreRomanNumeralsThatArePartsOfWordsWithLowercaseRoman() {
        Paginator paginator = new Paginator("Kapitel i");
        assertEquals("Kapitel i", paginator.next());
        assertEquals("Kapitel ii", paginator.next());
        assertEquals("Kapitel iii", paginator.next());
        assertEquals("Kapitel iv", paginator.next());
    }

    @Test
    public void handleRectoVersoForWhiteSpaceCharacterAsWell() {
        Paginator paginator = new Paginator("1°¿ ¿(¿R¿ü¿`c`¿k¿s¿e¿`i`¿t¿e¿)½");
        assertEquals("1", paginator.next());
        assertEquals("1 (Rückseite)", paginator.next());
        assertEquals("2", paginator.next());
        assertEquals("2 (Rückseite)", paginator.next());
    }

    @Test
    public void allowGroupingOfRectoVersoText() {
        Paginator paginator = new Paginator("1°¿` (Rückseite)`½");
        assertEquals("1", paginator.next());
        assertEquals("1 (Rückseite)", paginator.next());
        assertEquals("2", paginator.next());
        assertEquals("2 (Rückseite)", paginator.next());
    }

    @Test
    public void alphabeticPagination() {
        Paginator paginator = new Paginator("´a´");
        assertEquals("a", paginator.next());
        assertEquals("b", paginator.next());
        assertEquals("c", paginator.next());
        assertEquals("d", paginator.next());
    }

    @Test
    public void alphabeticPaginationRectoVerso() {
        Paginator paginator = new Paginator("´a´° ¡r¿`v`½");
        assertEquals("a r", paginator.next());
        assertEquals("a v", paginator.next());
        assertEquals("b r", paginator.next());
        assertEquals("b v", paginator.next());
        assertEquals("c r", paginator.next());
        assertEquals("c v", paginator.next());
    }
}
