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

import org.junit.Assert;
import org.junit.Test;

public class PaginatorTest {

    @Test
    public void firstPagination() {
        Paginator paginator = new Paginator("[`0`-(1)]");
        Assert.assertEquals("[0-(1)]", paginator.next());
        Assert.assertEquals("[0-(2)]", paginator.next());
        Assert.assertEquals("[0-(3)]", paginator.next());
        Assert.assertEquals("[0-(4)]", paginator.next());
    }

    @Test
    public void secondPagination() {
        Paginator paginator = new Paginator("[`1`-1²]");
        Assert.assertEquals("[1-1]", paginator.next());
        Assert.assertEquals("[1-3]", paginator.next());
        Assert.assertEquals("[1-5]", paginator.next());
        Assert.assertEquals("[1-7]", paginator.next());
    }

    @Test
    public void thirdPagination() {
        Paginator paginator = new Paginator("[`1`]-5²");
        Assert.assertEquals("[1]-5", paginator.next());
        Assert.assertEquals("[1]-7", paginator.next());
        Assert.assertEquals("[1]-9", paginator.next());
        Assert.assertEquals("[1]-11", paginator.next());
    }

    @Test
    public void fourthPagination() {
        Paginator paginator = new Paginator("([`1`]-11²)");
        Assert.assertEquals("([1]-11)", paginator.next());
        Assert.assertEquals("([1]-13)", paginator.next());
        Assert.assertEquals("([1]-15)", paginator.next());
    }

    @Test
    public void interleavePagination() {
        Paginator paginator = new Paginator("1²");
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("3", paginator.next());
        Assert.assertEquals("5", paginator.next());
        Assert.assertEquals("7", paginator.next());
        Assert.assertEquals("9", paginator.next());
    }

    @Test
    public void rectoVersoOnOnePagePagination() {
        Paginator paginator = new Paginator("1`v` 2°r");
        Assert.assertEquals("1v 2r", paginator.next());
        Assert.assertEquals("2v 3r", paginator.next());
        Assert.assertEquals("3v 4r", paginator.next());
    }

    @Test
    public void interleavePaginationHalf() {
        Paginator paginator = new Paginator("1½");
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("2", paginator.next());
        Assert.assertEquals("2", paginator.next());
        Assert.assertEquals("3", paginator.next());
    }

    @Test
    public void inteleavePaginationThree() {
        Paginator paginator = new Paginator("1³");
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("4", paginator.next());
        Assert.assertEquals("7", paginator.next());
        Assert.assertEquals("10", paginator.next());
        Assert.assertEquals("13", paginator.next());
    }

    @Test
    public void rectoVersoPagination() {
        Paginator paginator = new Paginator("1° ¡r¿v½");
        Assert.assertEquals("1 r", paginator.next());
        Assert.assertEquals("1 v", paginator.next());
        Assert.assertEquals("2 r", paginator.next());
        Assert.assertEquals("2 v", paginator.next());
    }

    @Test
    public void rectoVersoPaginationStartRight() {
        Paginator paginator = new Paginator("½1° ¡r¿v½");
        Assert.assertEquals("1 v", paginator.next());
        Assert.assertEquals("2 r", paginator.next());
        Assert.assertEquals("2 v", paginator.next());
        Assert.assertEquals("3 r", paginator.next());
        Assert.assertEquals("3 v", paginator.next());
    }

    @Test
    public void romanPaginationLowercase() {
        Paginator p = new Paginator("i");
        Assert.assertEquals("i", p.next());
        Assert.assertEquals("ii", p.next());
        Assert.assertEquals("iii", p.next());
        Assert.assertEquals("iv", p.next());
        Assert.assertEquals("v", p.next());
    }

    @Test
    public void romanPaginationUppercase() {
        Paginator paginator = new Paginator("I");
        Assert.assertEquals("I", paginator.next());
        Assert.assertEquals("II", paginator.next());
        Assert.assertEquals("III", paginator.next());
        Assert.assertEquals("IV", paginator.next());
        Assert.assertEquals("V", paginator.next());
    }

    @Test
    public void simplePagination() {
        Paginator paginator = new Paginator("1");
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("2", paginator.next());
        Assert.assertEquals("3", paginator.next());
        Assert.assertEquals("4", paginator.next());
        Assert.assertEquals("5", paginator.next());
        Assert.assertEquals("6", paginator.next());
        Assert.assertEquals("7", paginator.next());
    }

    @Test
    public void twoColumnPagination() {
        Paginator paginator = new Paginator("1 2");
        Assert.assertEquals("1 2", paginator.next());
        Assert.assertEquals("3 4", paginator.next());
        Assert.assertEquals("5 6", paginator.next());
        Assert.assertEquals("7 8", paginator.next());
        Assert.assertEquals("9 10", paginator.next());
    }

    @Test
    public void twoColumnPaginationRightToLeft() {
        Paginator paginator = new Paginator("2 1");
        Assert.assertEquals("2 1", paginator.next());
        Assert.assertEquals("4 3", paginator.next());
        Assert.assertEquals("6 5", paginator.next());
        Assert.assertEquals("8 7", paginator.next());
        Assert.assertEquals("10 9", paginator.next());
    }

    @Test
    public void ignoreRomanNumeralsThatArePartsOfWordsWithLatin() {
        Paginator paginator = new Paginator("Kapitel 1");
        Assert.assertEquals("Kapitel 1", paginator.next());
        Assert.assertEquals("Kapitel 2", paginator.next());
        Assert.assertEquals("Kapitel 3", paginator.next());
        Assert.assertEquals("Kapitel 4", paginator.next());
    }

    @Test
    public void ignoreRomanNumeralsThatArePartsOfWordsWithLowercaseRoman() {
        Paginator paginator = new Paginator("Kapitel i");
        Assert.assertEquals("Kapitel i", paginator.next());
        Assert.assertEquals("Kapitel ii", paginator.next());
        Assert.assertEquals("Kapitel iii", paginator.next());
        Assert.assertEquals("Kapitel iv", paginator.next());
    }

    @Test
    public void handleRectoVersoForWhiteSpaceCharacterAsWell() {
        Paginator paginator = new Paginator("1°¿ ¿(¿R¿ü¿c¿k¿s¿e¿i¿t¿e¿)½");
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("1 (Rückseite)", paginator.next());
        Assert.assertEquals("2", paginator.next());
        Assert.assertEquals("2 (Rückseite)", paginator.next());
    }

    @Test
    public void allowGroupingOfRectoVersoText() {
        Paginator paginator = new Paginator("1°¿` (Rückseite)`½");
        Assert.assertEquals("1", paginator.next());
        Assert.assertEquals("1 (Rückseite)", paginator.next());
        Assert.assertEquals("2", paginator.next());
        Assert.assertEquals("2 (Rückseite)", paginator.next());
    }
}
