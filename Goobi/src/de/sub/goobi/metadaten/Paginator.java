/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *    - http://gdz.sub.uni-goettingen.de
 *    - http://www.goobi.org
 *    - https://github.com/goobi/goobi-production
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
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sub.goobi.metadaten;

import org.goobi.pagination.IntegerSequence;
import org.goobi.pagination.RomanNumberSequence;

import de.sub.goobi.config.ConfigMain;
import ugh.dl.RomanNumeral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Sets new labels to a given set of pages.
 */
public class Paginator {

	public enum Mode {
		PAGES, COLUMNS, FOLIATION, RECTOVERSO_FOLIATION, RECTOVERSO, DOUBLE_PAGES
	}

	public enum Type {
		ARABIC, ROMAN, UNCOUNTED, FREETEXT
	}

	public enum Scope {
		FROMFIRST, SELECTED
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
			if (paginationType.equals(Type.UNCOUNTED) || paginationType.equals(Type.FREETEXT)){
				sequence = cloneEachInSequence(sequence);
			}
			return scrunchSequence(sequence);
		}
		
		sequence = cloneEachInSequence(sequence);

		if (paginationType == Paginator.Type.UNCOUNTED || paginationType == Paginator.Type.FREETEXT ) {
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
		List<Object> fictitiousSequence = new ArrayList<Object>(sequence.size());
		for (Object o : sequence) {
			String newLabel = o.toString();
			fictitiousSequence.add("[" + newLabel + "]");
		}
		return fictitiousSequence;
	}

	private List addAlternatingRectoVersoSuffixToEachInSequence(List sequence) {
		List<Object> rectoversoSequence = new ArrayList<Object>(sequence.size());
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
		List<Object> scrunchedSequence = new ArrayList<Object>((sequence.size() / 2));
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
		List<Object> foliationSequence = new ArrayList<Object>(sequence.size() * 2);
		for (Object o : sequence) {
			foliationSequence.add(o);
			foliationSequence.add(o);
		}

		sequence = foliationSequence;
		return sequence;
	}

	private List determineSequenceFromPaginationType(int start, int end) {
		List sequence = null;
		int increment = paginationMode.equals(Mode.COLUMNS) ? 2 : 1;

		switch (paginationType) {
		case UNCOUNTED:
			sequence = new ArrayList(1);
			sequence.add("uncounted");
			break;
		case FREETEXT:
			sequence= new ArrayList(1);
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
	 * @param selectedPages
	 *            Array numbers, each pointing to a given page set via <code>setPagesToPaginate</code>
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPageSelection(int[] selectedPages) {
		this.selectedPages = selectedPages;
		return this;
	}

	/**
	 * Give page objects to apply new page labels on.
	 * 
	 * @param newPaginated
	 *            Array of page objects.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPagesToPaginate(Metadatum[] newPaginated) {
		this.pagesToPaginate = newPaginated;
		return this;
	}

	/**
	 * Set pagination mode.
	 * 
	 * @param paginationMode
	 *            Mode of counting pages.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationMode(Mode paginationMode) {
		this.paginationMode = paginationMode;
		return this;
	}

	/**
	 * Set scope of pagination.
	 * 
	 * @param paginationScope
	 *            Set which pages from a selection get labeled.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationScope(Scope paginationScope) {
		this.paginationScope = paginationScope;
		return this;
	}

	/**
	 * Set separator of pagination.
	 * 
	 * @param sep
	 *            Set the separator to separate pages.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationSeparator(String sep) {
		this.sep = sep;
		return this;
	}

	/**
	 * Set start value of pagination. Counting up starts here depending on the pagination mode set.
	 * 
	 * @param paginationStartValue
	 *            May contain arabic or roman number.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationStartValue(String paginationStartValue) {
		this.paginationStartValue = paginationStartValue;
		return this;
	}

	/**
	 * Determine whether arabic or roman numbers should be used when counting.
	 * 
	 * @param paginationType
	 *            Set style of pagination numbers.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setPaginationType(Type paginationType) {
		this.paginationType = paginationType;
		return this;
	}

	/**
	 * Enable or disable fictitious pagination using square bracktes around numbers.
	 * 
	 * @param b
	 *            True, fictitious pagination. False, regular pagination.
	 * @return This object for fluent interfacing.
	 */
	public Paginator setFictitious(boolean b) {
		this.fictitiousPagination = b;
		return this;
	}

}
