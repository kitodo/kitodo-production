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

package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

public class PaginatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnEmptyPageSelection() {
        Paginator paginator = new Paginator();
        paginator.run();
    }

    @Test(expected = NumberFormatException.class)
    public void throwsExceptionWhenCalledWithInvalidStartValue() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {1, 2 })
                .setPaginationType(Paginator.Type.ARABIC).setPaginationStartValue("II");
        paginator.run();
    }

    @Test
    public void setsSelectedPagesToUncounted() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0, 1, 2 })
                .setPaginationType(Paginator.Type.UNCOUNTED).setPaginationScope(Paginator.Scope.SELECTED)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsSelectedPagesToUncountedNoMatterWhatStartValueIsGiven() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0, 1, 2 })
                .setPaginationType(Paginator.Type.UNCOUNTED).setPaginationScope(Paginator.Scope.SELECTED)
                .setPaginationStartValue("Foo")
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsAllPagesToUncounted() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 })
                .setPaginationType(Paginator.Type.UNCOUNTED).setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsPagesToSequenceOfArabicNumbers() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("50").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.PAGES)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"50", "51", "52" });
    }

    @Test
    public void setsPagesToSequenceOfRomanNumbers() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ROMAN)
                .setPaginationStartValue("II").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.PAGES)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"II", "III", "IV" });
    }

    @Test
    public void paginateCountingColumns() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.COLUMNS)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1", "3", "5" });
    }

    @Test
    public void paginateUsingFoliation() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.FOLIATION).setPagesToPaginate(new Metadatum[] {new MockMetadatum(),
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1", "1", "2", "2" });
    }

    @Test
    public void paginateRectoVersoFoliation() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0, 1, 2, 3 })
                .setPaginationType(Paginator.Type.ARABIC).setPaginationStartValue("1")
                .setPaginationScope(Paginator.Scope.SELECTED).setPaginationMode(Paginator.Mode.RECTOVERSO)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum(),
                        new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1r", "1v", "2r", "2v" });
    }

    @Test
    public void paginateRectoVersoFoliationOnSinglePage() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum() }).setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1v 2r" });
    }

    @Test
    public void setsAllToUncountedWhenRectoVersoFoliationModeAndTypeUncounted() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 })
                .setPaginationType(Paginator.Type.UNCOUNTED).setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO).setPagesToPaginate(new Metadatum[] {new MockMetadatum(),
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void rectoVersoPaginationShouldStartWithRectoOnSkippedPages() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {1, 2, 3 })
                .setPaginationType(Paginator.Type.ARABIC).setPaginationScope(Paginator.Scope.SELECTED)
                .setPaginationMode(Paginator.Mode.RECTOVERSO).setPaginationStartValue("1");
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum("uncounted"), new MockMetadatum(),
                new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"uncounted", "1r", "1v", "2r" });
    }

    @Test
    public void fictitiousArabicPagination() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("50").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.PAGES).setFictitious(true)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[50]", "[51]", "[52]" });
    }

    @Test
    public void fictitiousArabicRectoVersoPagination() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("4711").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO).setFictitious(true).setPagesToPaginate(new Metadatum[] {
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[4711]r", "[4711]v", "[4712]r", "[4712]v" });
    }

    @Test
    public void fictitiousRomanNumberPagination() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ROMAN)
                .setPaginationStartValue("III").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.PAGES).setFictitious(true)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[III]", "[IV]", "[V]" });
    }

    @Test
    public void fictitiousPaginationUsingFoliation() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.FOLIATION).setFictitious(true).setPagesToPaginate(new Metadatum[] {
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[1]", "[1]", "[2]", "[2]" });
    }

    @Test
    public void rectoVersoPagination() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() })
                .setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1v 2r", "2v 3r", "3v 4r" });
    }

    @Test
    public void fictitiousRectoVersoPagination() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION).setFictitious(true)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() })
                .setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[1]v [2]r", "[2]v [3]r", "[3]v [4]r" });
    }

    @Test
    public void fictitiousRomanRectoVersoPagination() {
        Paginator paginator = new Paginator().setPageSelection(new int[] {0 }).setPaginationType(Paginator.Type.ROMAN)
                .setPaginationStartValue("XX").setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION).setFictitious(true)
                .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() })
                .setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[XX]v [XXI]r", "[XXI]v [XXII]r", "[XXII]v [XXIII]r" });
    }

    private void assertPagenumberSequence(Paginator paginator, String[] sequence) {

        Metadatum[] newPaginated = paginator.getPagesToPaginate();

        assertNotNull("Expected paginator result set.", newPaginated);

        assertEquals("Unexpected number of paginated pages.", sequence.length, newPaginated.length);

        for (int i = 0; i < sequence.length; i++) {
            assertEquals("Actual paginator value did not match expected.", sequence[i], newPaginated[i].getValue());
        }

    }

    private void assertAllPagenumbersSetToValue(Paginator paginator, String expectedValue)
            throws ArrayComparisonFailure {

        Metadatum[] newPaginated = paginator.getPagesToPaginate();

        assertNotNull("Expected paginator result set.", newPaginated);

        for (Metadatum m : newPaginated) {
            assertEquals("Actual paginator value did not match expected.", expectedValue, m.getValue());
        }

    }

}
