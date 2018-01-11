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

import de.sub.goobi.helper.Helper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.goobi.pagination.IntegerSequence;
import org.goobi.pagination.RomanNumberSequence;
import org.kitodo.api.ugh.RomanNumeral;
import org.kitodo.api.ugh.UghImplementation;

/**
 * Sets new labels to a given set of pages.
 */
public class Paginator {

    public enum Mode {
        PAGES(Helper.getTranslation("seitenzaehlung"), "paginierung_seite.svg"),
        COLUMNS(Helper.getTranslation("spaltenzaehlung"), "paginierung_spalte.svg"),
        FOLIATION(Helper.getTranslation("blattzaehlung"), "paginierung_blatt.svg"),
        RECTOVERSO_FOLIATION(Helper.getTranslation("blattzaehlungrectoverso"), "paginierung_blatt_rectoverso.svg"),
        RECTOVERSO(Helper.getTranslation("seitenzaehlungrectoverso"), "paginierung_seite_rectoverso.svg"),
        DOUBLE_PAGES(Helper.getTranslation("seitenzaehlungdoppelseiten"), "paginierung_doppelseite.svg");

        private String label;
        private String image;

        Mode(String label, String image) {
            this.label = label;
            this.image = image;
        }

        /**
         * Gets label of paginator mode.
         *
         * @return The label of paginator mode.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Gets image of paginator mode for displaying at frontend.
         *
         * @return The label of paginator mode.
         */
        public String getImage() {
            return image;
        }
    }

    public enum Type {
        ARABIC(Helper.getTranslation("arabisch")),
        ROMAN(Helper.getTranslation("roemisch")),
        UNCOUNTED(Helper.getTranslation("unnummeriert")),
        FREETEXT(Helper.getTranslation("paginationFreetext"));

        private String label;

        Type(String label) {
            this.label = label;
        }

        /**
         * Gets label of paginator type.
         *
         * @return The label of paginator type.
         */
        public String getLabel() {
            return label;
        }
    }

    public enum Scope {
        FROMFIRST(Helper.getTranslation("abDerErstenMarkiertenSeite")),
        SELECTED(Helper.getTranslation("nurDieMarkiertenSeiten"));

        private String label;

        Scope(String label) {
            this.label = label;
        }

        /**
         * Gets label of paginator scope.
         *
         * @return The label of paginator scope.
         */
        public String getLabel() {
            return label;
        }
    }

    private int[] selectedPages;

    private Metadatum[] pagesToPaginate;

    private Mode paginationMode = Paginator.Mode.PAGES;

    private Scope paginationScope = Paginator.Scope.FROMFIRST;

    private String paginationStartValue = "uncounted";

    private Type paginationType = Paginator.Type.UNCOUNTED;

    private boolean fictitiousPagination = false;

    private String sep;

    /**
     * Perform pagination.
     *
     * @throws IllegalArgumentException
     *             Thrown if invalid config parameters have been set.
     */
    public void run() throws IllegalArgumentException {

        assertSelectionIsNotNull();
        assertValidPaginationStartValue();

        List sequence = createPaginationSequence();

        applyPaginationSequence(sequence);

    }

    private void applyPaginationSequence(List sequence) {
        if (paginationScope == Scope.SELECTED) {
            applyToSelected(sequence);
        } else if (paginationScope == Scope.FROMFIRST) {
            applyFromFirstSelected(sequence);
        }
    }

    private void assertSelectionIsNotNull() {
        if (selectedPages == null || selectedPages.length == 0) {
            throw new IllegalArgumentException("No pages selected for pagination.");
        }
    }

    /**
     * Throws NumberFormatException if `paginationStartValue` isn’t a valid
     * number of the type specified by `paginationType`.
     *
     * @throws NumberFormatException
     *             if `paginationStartValue` isn’t valid
     */
    private void assertValidPaginationStartValue() {
        // arabic numbers
        if (paginationType == Paginator.Type.ARABIC) {
            /*
             * coverity[USELESS_CALL] Integer.parseInt() throws
             * NumberFormatException if paginationStartValue cannot be parsed to
             * an `int`, and this is what we want here.
             */
            Integer.parseInt(paginationStartValue);
        }
        // roman numbers
        if (paginationType == Paginator.Type.ROMAN) {
            RomanNumeral roman = UghImplementation.INSTANCE.createRomanNumeral();
            roman.setValue(paginationStartValue);
        }
    }

    private List createPaginationSequence() {

        int start = determinePaginationBaseValue();
        int end = determinePaginationEndValue(start);
        List sequence = determineSequenceFromPaginationType(start, end);

        if (fictitiousPagination) {
            sequence = addSquareBracketsToEachInSequence(sequence);
        }

        if ((paginationMode == Paginator.Mode.PAGES) || (paginationMode == Paginator.Mode.COLUMNS)) {
            return sequence;
        }

        if (paginationMode.equals(Mode.DOUBLE_PAGES)) {
            if (paginationType.equals(Type.UNCOUNTED) || paginationType.equals(Type.FREETEXT)) {
                sequence = cloneEachInSequence(sequence);
            }
            return scrunchSequence(sequence);
        }

        sequence = cloneEachInSequence(sequence);

        if (paginationType == Paginator.Type.UNCOUNTED || paginationType == Paginator.Type.FREETEXT) {
            return sequence;
        }

        if ((paginationMode == Paginator.Mode.RECTOVERSO) || (paginationMode == Paginator.Mode.RECTOVERSO_FOLIATION)) {
            sequence = addAlternatingRectoVersoSuffixToEachInSequence(sequence);
        }

        if (paginationMode == Paginator.Mode.RECTOVERSO_FOLIATION) {
            sequence.remove(0);
            sequence = scrunchSequence(sequence);
        }

        return sequence;
    }

    private List addSquareBracketsToEachInSequence(List sequence) {
        List<Object> fictitiousSequence = new ArrayList<>(sequence.size());
        for (Object o : sequence) {
            String newLabel = o.toString();
            fictitiousSequence.add("[" + newLabel + "]");
        }
        return fictitiousSequence;
    }

    private List addAlternatingRectoVersoSuffixToEachInSequence(List sequence) {
        List<Object> rectoversoSequence = new ArrayList<>(sequence.size());
        Boolean toggle = false;
        for (Object o : sequence) {
            String label = o.toString();
            toggle = !toggle;
            rectoversoSequence.add(label + (toggle ? "r" : "v"));
        }

        sequence = rectoversoSequence;
        return sequence;
    }

    private List scrunchSequence(List sequence) {
        List<Object> scrunchedSequence = new ArrayList<>((sequence.size() / 2));
        String prev = "";
        boolean scrunch = false;
        for (Object o : sequence) {
            if (scrunch) {
                scrunchedSequence.add(prev + sep + o.toString());
            } else {
                prev = o.toString();
            }
            scrunch = !scrunch;
        }
        return scrunchedSequence;
    }

    private List cloneEachInSequence(List sequence) {
        List<Object> foliationSequence = new ArrayList<>(sequence.size() * 2);
        for (Object o : sequence) {
            foliationSequence.add(o);
            foliationSequence.add(o);
        }

        sequence = foliationSequence;
        return sequence;
    }

    @SuppressWarnings("unchecked")
    private List determineSequenceFromPaginationType(int start, int end) {
        List sequence = null;
        int increment = paginationMode.equals(Mode.COLUMNS) ? 2 : 1;

        switch (paginationType) {
            case UNCOUNTED:
                sequence = new ArrayList(1);
                sequence.add("uncounted");
                break;
            case FREETEXT:
                sequence = new ArrayList(1);
                sequence.add(paginationStartValue);
                break;
            case ROMAN:
                sequence = new RomanNumberSequence(start, end, increment);
                break;
            case ARABIC:
                sequence = new IntegerSequence(start, end, increment);
                break;
        }
        return sequence;
    }

    private int determinePaginationEndValue(int start) {
        int increment = paginationMode.equals(Mode.COLUMNS) || paginationMode.equals(Mode.DOUBLE_PAGES) ? 2 : 1;
        int numSelectedPages = selectedPages.length;
        if (paginationScope == Paginator.Scope.FROMFIRST) {
            int first = selectedPages[0];
            numSelectedPages = pagesToPaginate.length - first;
        }
        return start + (numSelectedPages * increment);
    }

    private void applyFromFirstSelected(List sequence) {
        int first = selectedPages[0];
        Iterator seqit = sequence.iterator();
        for (int pageNum = first; pageNum < pagesToPaginate.length; pageNum++) {
            if (!seqit.hasNext()) {
                seqit = sequence.iterator();
            }
            pagesToPaginate[pageNum].setWert(String.valueOf(seqit.next()));
        }
    }

    private void applyToSelected(List sequence) {
        Iterator seqit = sequence.iterator();
        for (int num : selectedPages) {
            if (!seqit.hasNext()) {
                seqit = sequence.iterator();
            }
            pagesToPaginate[num].setWert(String.valueOf(seqit.next()));
        }
    }

    private int determinePaginationBaseValue() {

        int paginationBaseValue = 1;

        if (paginationType == Paginator.Type.ARABIC) {
            paginationBaseValue = Integer.parseInt(paginationStartValue);
        } else if (paginationType == Paginator.Type.ROMAN) {
            RomanNumeral r = UghImplementation.INSTANCE.createRomanNumeral();
            r.setValue(paginationStartValue);
            paginationBaseValue = r.intValue();
        }

        return paginationBaseValue;

    }

    /**
     * Get pages provided with new pagination label.
     *
     * @return Array of <code>Metadatum</code> instances.
     */
    public Metadatum[] getPagesToPaginate() {
        return pagesToPaginate;
    }

    /**
     * Give a list of page numbers to select pages to actually paginate.
     *
     * @param selectedPages
     *            Array numbers, each pointing to a given page set via
     *            <code>setPagesToPaginate</code>
     */
    public void setPageSelection(int[] selectedPages) {
        this.selectedPages = selectedPages;
    }

    /**
     * Give page objects to apply new page labels on.
     *
     * @param newPaginated
     *            Array of page objects.
     */
    public void setPagesToPaginate(Metadatum[] newPaginated) {
        this.pagesToPaginate = newPaginated;
    }

    /**
     * Set pagination mode.
     *
     * @param paginationMode
     *            Mode of counting pages.
     */
    public void setPaginationMode(Mode paginationMode) {
        this.paginationMode = paginationMode;
    }

    /**
     * Set scope of pagination.
     *
     * @param paginationScope
     *            Set which pages from a selection get labeled.
     */
    public void setPaginationScope(Scope paginationScope) {
        this.paginationScope = paginationScope;
    }

    /**
     * Set separator of pagination.
     *
     * @param sep
     *            Set the separator to separate pages.
     */
    public void setPaginationSeparator(String sep) {
        this.sep = sep;
    }

    /**
     * Set start value of pagination. Counting up starts here depending on the
     * pagination mode set.
     *
     * @param paginationStartValue
     *            May contain arabic or roman number.
     */
    public void setPaginationStartValue(String paginationStartValue) {
        this.paginationStartValue = paginationStartValue;
    }

    /**
     * Determine whether arabic or roman numbers should be used when counting.
     *
     * @param paginationType
     *            Set style of pagination numbers.
     */
    public void setPaginationType(Type paginationType) {
        this.paginationType = paginationType;
    }

    /**
     * Enable or disable fictitious pagination using square bracktes around
     * numbers.
     *
     * @param b
     *            True, fictitious pagination. False, regular pagination.
     */
    public void setFictitious(boolean b) {
        this.fictitiousPagination = b;
    }

    /**
     * Gets selected Pages.
     *
     * @return Array of selected Pages.
     */
    public int[] getSelectedPages() {
        return selectedPages;
    }

    /**
     * Gets pagination mode.
     *
     * @return The pagination mode.
     */
    public Mode getPaginationMode() {
        return paginationMode;
    }

    /**
     * Gets pagination scope.
     *
     * @return The pagination scope.
     */
    public Scope getPaginationScope() {
        return paginationScope;
    }

    /**
     * Gets pagination strat value.
     *
     * @return The pagination start value.
     */
    public String getPaginationStartValue() {
        return paginationStartValue;
    }

    /**
     * Gets pagination type.
     *
     * @return The pagination type.
     */
    public Type getPaginationType() {
        return paginationType;
    }

    /**
     * Gets all pagination modes.
     *
     * @return The pagination modes.
     */
    public Paginator.Mode[] getPaginationModes() {
        return Paginator.Mode.values();
    }

    /**
     * Gets all pagination types.
     *
     * @return The pagination types.
     */
    public Paginator.Type[] getPaginationTypes() {
        return Paginator.Type.values();
    }

    /**
     * Gets all pagination scopes.
     *
     * @return The pagination scopes.
     */
    public Paginator.Scope[] getPaginationScopes() {
        return Paginator.Scope.values();
    }
}
