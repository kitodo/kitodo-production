/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *    - http://gdz.sub.uni-goettingen.de
 *    - http://www.goobi.org
 *    - http://launchpad.net/goobi-production
 *
 * Copyright 2011, Center for Retrospective Digitization, Göttingen (GDZ),
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

import org.goobi.pagination.IntegerSequence;
import org.goobi.pagination.RomanNumberSequence;
import ugh.dl.RomanNumeral;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Sets new labels to a given set of pages.
 */
public class Paginator {

    public enum Mode {
		PAGES, COLUMNS, FOLIATION, RECTOVERSO_FOLIATION, RECTOVERSO
	}

	public enum Type {ARABIC, ROMAN, UNCOUNTED}

	public enum Scope {FROMFIRST, SELECTED}

	private int[] selectedPages;

	private Metadatum[] pagesToPaginate;

	private Mode paginationMode = Paginator.Mode.PAGES;

	private Scope paginationScope = Paginator.Scope.FROMFIRST;

	private String paginationStartValue = "uncounted";

	private Type paginationType = Paginator.Type.UNCOUNTED;

    private boolean fictitiousPagination = false;

	/**
	 * Perform pagination.
	 *
	 * @throws IllegalArgumentException Thrown if invalid config parameters have been set.
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

	private void assertValidPaginationStartValue() {
		// arabic numbers
		if (paginationType == Paginator.Type.ARABIC) {
			Integer.parseInt(paginationStartValue);
		}
		// roman numbers
		if (paginationType == Paginator.Type.ROMAN) {
			RomanNumeral roman = new RomanNumeral();
			roman.setValue(paginationStartValue);
		}
	}

	private List createPaginationSequence() {

		int increment =
				determineIncrementFromPaginationMode();
		int start =
				determinePaginationBaseValue();
		int end =
				determinePaginationEndValue(increment, start);
		List sequence =
				determineSequenceFromPaginationType(increment, start, end);

		if (fictitiousPagination) {
			sequence = addSquareBracketsToEachInSequence(sequence);
		}

		if (paginationMode == Paginator.Mode.FOLIATION) {

			sequence = cloneEachInSequence(sequence);

		} else if (paginationType != Paginator.Type.UNCOUNTED) {
			if (paginationMode == Paginator.Mode.RECTOVERSO) {

				sequence = addAlternatingRectoVersoSuffixToEachInSequence(sequence);

			} else if (paginationMode == Mode.RECTOVERSO_FOLIATION) {

				sequence = addRectoVersoSuffixToEachInSequence(sequence);

			}
		}

		return sequence;
	}

    private List addSquareBracketsToEachInSequence(List sequence) {
        List<Object> fictitiousSequence = new ArrayList<Object>(sequence.size());
        for (Object o : sequence) {
			String newLabel = o.toString();
			fictitiousSequence.add("[" + newLabel + "]");
        }
		return fictitiousSequence;
    }

    private List addAlternatingRectoVersoSuffixToEachInSequence(List sequence) {
		List<Object> rectoversoSequence = new ArrayList<Object>(sequence.size() * 2);
		for (Object o : sequence) {
			String newLabel = o.toString();
			rectoversoSequence.add(newLabel + "r");
			rectoversoSequence.add(newLabel + "v");
		}

		sequence = rectoversoSequence;
		return sequence;
	}

    private List addRectoVersoSuffixToEachInSequence(List sequence) {
		List<Object> rectoversoSequence = new ArrayList<Object>(sequence.size() * 2);
		for (Object o : sequence) {
			String newLabel = o.toString();
			rectoversoSequence.add(newLabel + "r " + newLabel + "v");
		}

		sequence = rectoversoSequence;
		return sequence;
	}

	private List cloneEachInSequence(List sequence) {
		List<Object> foliationSequence = new ArrayList<Object>(sequence.size() * 2);
		for (Object o : sequence) {
			foliationSequence.add(o);
			foliationSequence.add(o);
		}

		sequence = foliationSequence;
		return sequence;
	}

	private List determineSequenceFromPaginationType(int increment, int start, int end) {
		List sequence = null;

		switch (paginationType) {
			case UNCOUNTED:
				sequence = new ArrayList(1);
				sequence.add("uncounted");
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

	private int determineIncrementFromPaginationMode() {
		int increment = 1;
		if (paginationMode == Paginator.Mode.COLUMNS) {
			increment = 2;
		}
		return increment;
	}

	private int determinePaginationEndValue(int increment, int start) {
		int numSelectedPages = selectedPages.length;
		if (paginationScope == Paginator.Scope.FROMFIRST) {
			int first = selectedPages[0];
			numSelectedPages = pagesToPaginate.length - first;
		}
		return start + numSelectedPages + increment;
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
			RomanNumeral r = new RomanNumeral();
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
	 * @param selectedPages Array numbers, each pointing to a given page set via <code>setPagesToPaginate</code>
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPageSelection(int[] selectedPages) {
		this.selectedPages = selectedPages;
		return this;
	}

	/**
	 * Give page objects to apply new page labels on.
	 *
	 * @param newPaginated Array of page objects.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPagesToPaginate(Metadatum[] newPaginated) {
		this.pagesToPaginate = newPaginated;
		return this;
	}

	/**
	 * Set pagination mode.
	 *
	 * @param paginationMode Mode of counting pages.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationMode(Mode paginationMode) {
		this.paginationMode = paginationMode;
		return this;
	}

	/**
	 * Set scope of pagination.
	 *
	 * @param paginationScope Set which pages from a selection get labeled.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationScope(Scope paginationScope) {
		this.paginationScope = paginationScope;
		return this;
	}

	/**
	 * Set start value of pagination. Counting up starts here depending on the pagination mode set.
	 *
	 * @param paginationStartValue May contain arabic or roman number.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationStartValue(String paginationStartValue) {
		this.paginationStartValue = paginationStartValue;
		return this;
	}

	/**
	 * Determine weather arabic or roman numbers should be used when counting.
	 *
	 * @param paginationType Set style of pagination numbers.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationType(Type paginationType) {
		this.paginationType = paginationType;
		return this;
	}

	/**
	 * Enable or disable fictitious pagination using square bracktes around numbers.
	 *
	 * @param b True, fictitious pagination. False, regular pagination.
	 * @return	This object for fluent interfacing.
	 */
	public Paginator setFictitious(boolean b) {
		this.fictitiousPagination = b;
		return this;
	}

}
