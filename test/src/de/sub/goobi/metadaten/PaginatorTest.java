/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.metadaten;

import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaginatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnEmptyPageSelection() {
        Paginator paginator = new Paginator();
        paginator.run();
    }

    @Test(expected = NumberFormatException.class)
    public void throwsExceptionWhenCalledWithInvalidStartValue() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{1, 2})
                .setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("II");
        paginator.run();
    }

    @Test
    public void setsSelectedPagesToUncounted() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0, 1, 2})
                .setPaginationType(Paginator.Type.UNCOUNTED)
                .setPaginationScope(Paginator.Scope.SELECTED)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsSelectedPagesToUncountedNoMatterWhatStartValueIsGiven() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0, 1, 2})
                .setPaginationType(Paginator.Type.UNCOUNTED)
                .setPaginationScope(Paginator.Scope.SELECTED)
                .setPaginationStartValue("Foo")
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsAllPagesToUncounted() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0})
                .setPaginationType(Paginator.Type.UNCOUNTED)
                .setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void setsPagesToSequenceOfArabicNumbers() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0})
                .setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("50")
                .setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.PAGES)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertPagenumberSequence(paginator, new String[]{"50", "51", "52"});
    }

    @Test
    public void setsPagesToSequenceOfRomanNumbers() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0})
                .setPaginationType(Paginator.Type.ROMAN)
                .setPaginationStartValue("II")
                .setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.PAGES)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertPagenumberSequence(paginator, new String[]{"II", "III", "IV"});
    }

    @Test
    public void paginateCountingColumns() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0})
                .setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1")
                .setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.COLUMNS)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertPagenumberSequence(paginator, new String[]{"1", "3", "5"});
    }

    @Test
    public void paginateUsingFoliation() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0})
                .setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1")
                .setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.FOLIATION)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertPagenumberSequence(paginator, new String[]{"1", "1", "2", "2"});
    }

    @Test
    public void paginateRectoVersoFoliation() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0, 1, 2, 3})
                .setPaginationType(Paginator.Type.ARABIC)
                .setPaginationStartValue("1")
                .setPaginationScope(Paginator.Scope.SELECTED)
                .setPaginationMode(Paginator.Mode.RECTOVERSO)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertPagenumberSequence(paginator, new String[]{"1r", "1v", "2r", "2v"});
    }

    @Test
    public void setsAllToUncountedWhenRectoVersoFoliationModeAndTypeUncounted() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{0})
                .setPaginationType(Paginator.Type.UNCOUNTED)
                .setPaginationScope(Paginator.Scope.FROMFIRST)
                .setPaginationMode(Paginator.Mode.RECTOVERSO)
                .setPagesToPaginate(new Metadatum[]{
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum(),
                        new MockMetadatum()
                });
        paginator.run();
        assertAllPagenumbersSetToValue(paginator, "uncounted");
    }

    @Test
    public void rectoVersoPaginationShouldStartWithRectoOnSkippedPages() {
        Paginator paginator = new Paginator()
                .setPageSelection(new int[]{1, 2, 3})
                .setPaginationType(Paginator.Type.ARABIC)
                .setPaginationScope(Paginator.Scope.SELECTED)
                .setPaginationMode(Paginator.Mode.RECTOVERSO)
                .setPaginationStartValue("1");
        paginator.setPagesToPaginate(new Metadatum[]{
                new MockMetadatum("uncounted"),
                new MockMetadatum(),
                new MockMetadatum(),
                new MockMetadatum()
        });
        paginator.run();
        assertPagenumberSequence(paginator, new String[]{"uncounted", "1r", "1v", "2r"});
    }

    private void assertPagenumberSequence(Paginator paginator,
                                          String[] sequence) {

        Metadatum[] newPaginated = paginator.getPagesToPaginate();

        assertNotNull("Expected paginator result set.", newPaginated);

        assertEquals("Unexpected number of paginated pages.", sequence.length,
                newPaginated.length);

        for (int i = 0; i < sequence.length; i++) {
            assertEquals("Actual paginator value did not match expected.",
                    sequence[i], newPaginated[i].getValue());
        }

    }

    private void assertAllPagenumbersSetToValue(Paginator paginator,
                                                String expectedValue) throws ArrayComparisonFailure {

        Metadatum[] newPaginated = paginator.getPagesToPaginate();

        assertNotNull("Expected paginator result set.", newPaginated);

        for (Metadatum m : newPaginated) {
            assertEquals("Actual paginator value did not match expected.",
                    expectedValue, m.getValue());
        }

    }

}
