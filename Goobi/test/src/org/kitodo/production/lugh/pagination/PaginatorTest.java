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
package org.kitodo.production.lugh.pagination;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kitodo.production.lugh.pagination.Paginator;

public class PaginatorTest {

    @Test
    public void fancyPagination1() {
        Paginator p = new Paginator("[`0`-(1)]");
        assertEquals("[0-(1)]", p.next());
        assertEquals("[0-(2)]", p.next());
        assertEquals("[0-(3)]", p.next());
        assertEquals("[0-(4)]", p.next());
    }

    @Test
    public void fancyPagination2() {
        Paginator p = new Paginator("[`1`-1²]");
        assertEquals("[1-1]", p.next());
        assertEquals("[1-3]", p.next());
        assertEquals("[1-5]", p.next());
        assertEquals("[1-7]", p.next());
    }

    @Test
    public void fancyPagination3() {
        Paginator p = new Paginator("[`1`]-5²");
        assertEquals("[1]-5", p.next());
        assertEquals("[1]-7", p.next());
        assertEquals("[1]-9", p.next());
        assertEquals("[1]-11", p.next());
    }

    @Test
    public void fancyPagination4() {
        Paginator p = new Paginator("([`1`]-11²)");
        assertEquals("([1]-11)", p.next());
        assertEquals("([1]-13)", p.next());
        assertEquals("([1]-15)", p.next());
    }

    @Test
    public void inteleavePagination() {
        Paginator p = new Paginator("1²");
        assertEquals("1", p.next());
        assertEquals("3", p.next());
        assertEquals("5", p.next());
        assertEquals("7", p.next());
        assertEquals("9", p.next());
    }

    @Test
    public void rectoVersoOnOnePagePagination() {
        Paginator p = new Paginator("1`v` 2°r");
        assertEquals("1v 2r", p.next());
        assertEquals("2v 3r", p.next());
        assertEquals("3v 4r", p.next());
    }

    @Test
    public void rectoVersoPagination() {
        Paginator p = new Paginator("1° ¡r¿v½");
        assertEquals("1 r", p.next());
        assertEquals("1 v", p.next());
        assertEquals("2 r", p.next());
        assertEquals("2 v", p.next());
    }

    @Test
    public void rectoVersoPaginationStartRight() {
        Paginator p = new Paginator("½1° ¡r¿v½");
        assertEquals("1 v", p.next());
        assertEquals("2 r", p.next());
        assertEquals("2 v", p.next());
        assertEquals("3 r", p.next());
        assertEquals("3 v", p.next());
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
        Paginator p = new Paginator("I");
        assertEquals("I", p.next());
        assertEquals("II", p.next());
        assertEquals("III", p.next());
        assertEquals("IV", p.next());
        assertEquals("V", p.next());
    }

    @Test
    public void simplePagination() {
        Paginator p = new Paginator("1");
        assertEquals("1", p.next());
        assertEquals("2", p.next());
        assertEquals("3", p.next());
        assertEquals("4", p.next());
        assertEquals("5", p.next());
        assertEquals("6", p.next());
        assertEquals("7", p.next());
    }

    @Test
    public void twoColumnPagination() {
        Paginator p = new Paginator("1 2");
        assertEquals("1 2", p.next());
        assertEquals("3 4", p.next());
        assertEquals("5 6", p.next());
        assertEquals("7 8", p.next());
        assertEquals("9 10", p.next());
    }

    @Test
    public void twoColumnPaginationArabic() {
        Paginator p = new Paginator("2 1");
        assertEquals("2 1", p.next());
        assertEquals("4 3", p.next());
        assertEquals("6 5", p.next());
        assertEquals("8 7", p.next());
        assertEquals("10 9", p.next());
    }
}
