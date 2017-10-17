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
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {1, 2 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("II");
        paginator.run();
    }

    @Test
    public void setsSelectedPagesToUncounted() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0, 1, 2 });
        paginator.setPaginationType(Paginator.Type.UNCOUNTED);
        paginator.setPaginationScope(Paginator.Scope.SELECTED);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsSelectedPagesToUncountedNoMatterWhatStartValueIsGiven() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0, 1, 2 });
        paginator.setPaginationType(Paginator.Type.UNCOUNTED);
        paginator.setPaginationScope(Paginator.Scope.SELECTED);
        paginator.setPaginationStartValue("Foo");
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsAllPagesToUncounted() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.UNCOUNTED);
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsPagesToSequenceOfArabicNumbers() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("50");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.PAGES);
        paginator .setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"50", "51", "52" });
    }

    @Test
    public void setsPagesToSequenceOfRomanNumbers() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ROMAN);
        paginator.setPaginationStartValue("II");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.PAGES);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"II", "III", "IV" });
    }

    @Test
    public void paginateCountingColumns() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.COLUMNS);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1", "3", "5" });
    }

    @Test
    public void paginateUsingFoliation() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.FOLIATION);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(),
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1", "1", "2", "2" });
    }

    @Test
    public void paginateRectoVersoFoliation() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0, 1, 2, 3 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.SELECTED);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum(),
                        new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1r", "1v", "2r", "2v" });
    }

    @Test
    public void paginateRectoVersoFoliationOnSinglePage() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum() });
        paginator.setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1v 2r" });
    }

    @Test
    public void setsAllToUncountedWhenRectoVersoFoliationModeAndTypeUncounted() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.UNCOUNTED);
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(),
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void rectoVersoPaginationShouldStartWithRectoOnSkippedPages() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {1, 2, 3 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationScope(Paginator.Scope.SELECTED);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO);
        paginator.setPaginationStartValue("1");
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum("uncounted"), new MockMetadatum(),
                new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"uncounted", "1r", "1v", "2r" });
    }

    @Test
    public void fictitiousArabicPagination() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("50");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.PAGES);
        paginator.setFictitious(true);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[50]", "[51]", "[52]" });
    }

    @Test
    public void fictitiousArabicRectoVersoPagination() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("4711");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO);
        paginator.setFictitious(true);
        paginator.setPagesToPaginate(new Metadatum[] {
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[4711]r", "[4711]v", "[4712]r", "[4712]v" });
    }

    @Test
    public void fictitiousRomanNumberPagination() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ROMAN);
        paginator.setPaginationStartValue("III");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.PAGES);
        paginator.setFictitious(true);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[III]", "[IV]", "[V]" });
    }

    @Test
    public void fictitiousPaginationUsingFoliation() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.FOLIATION);
        paginator.setFictitious(true);
        paginator.setPagesToPaginate(new Metadatum[] {
                        new MockMetadatum(), new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[1]", "[1]", "[2]", "[2]" });
    }

    @Test
    public void rectoVersoPagination() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"1v 2r", "2v 3r", "3v 4r" });
    }

    @Test
    public void fictitiousRectoVersoPagination() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ARABIC);
        paginator.setPaginationStartValue("1");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION);
        paginator.setFictitious(true);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.setPaginationSeparator(" ");
        paginator.run();
        assertPagenumberSequence(paginator, new String[] {"[1]v [2]r", "[2]v [3]r", "[3]v [4]r" });
    }

    @Test
    public void fictitiousRomanRectoVersoPagination() {
        Paginator paginator = new Paginator();
        paginator.setPageSelection(new int[] {0 });
        paginator.setPaginationType(Paginator.Type.ROMAN);
        paginator.setPaginationStartValue("XX");
        paginator.setPaginationScope(Paginator.Scope.FROMFIRST);
        paginator.setPaginationMode(Paginator.Mode.RECTOVERSO_FOLIATION);
        paginator.setFictitious(true);
        paginator.setPagesToPaginate(new Metadatum[] {new MockMetadatum(), new MockMetadatum(), new MockMetadatum() });
        paginator.setPaginationSeparator(" ");
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
