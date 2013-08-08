package de.sub.goobi.metadaten;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import ugh.dl.RomanNumeral;
import de.sub.goobi.helper.Helper;

public class Pagination {

	/**
	 * 1, 2, 3, ..
	 */
	public static final String PAGINATION_ARABIC = "1";

	/**
	 * i, ii, iii, iv,...
	 */
	public static final String PAGINATION_ROMAN = "2";

	/**
	 * " ", "uncounted", "-", etc
	 */

	public static final String PAGINATION_UNCOUNTED = "3";

	/**
	 * [1], [2], [3], ...
	 */
	public static final String PAGINATION_ARABIC_BRACKET = "4";

	/**
	 * [i], [ii], [iii], [iv],...
	 */
	public static final String PAGINATION_ROMAN_BRACKET = "5";

	public static final String PAGINATION_FREETEXT = "6";

	public static final int ALL_PAGES = 1;

	public static final int SELECTED_PAGES = 2;

	public static final int COUNTING_PAGINATION = 1;

	public static final int COUNTING_COLUMNS = 2;

	public static final int COUNTING_FOLIATION = 3;

	public static final int COUNTING_FOLIATION_RECTOVERSO = 4;

	public static final int COUNTING_PAGINATION_RECTOVERSO = 5;

	private String[] allSelectedPages;

	private MetadatumImpl[] newPaginated;

	private int paginationMode;

	private int paginationScope;

	private String paginationStartValue;

	private String paginationType;

	/**
	 * Consturctor
	 * 
	 * @param allSelectedPages
	 *            selected pages as string
	 * @param newPaginated
	 *            metadata of physical DocStrct 'BoundBook'
	 * @param paginationScope
	 *            scope of pagination ( all pages = 1,only selected pages = 2)
	 * @param paginationType
	 *            type of pagination label (arabic = 1, roman = 2, uncounted = 3)
	 * @param paginationMode
	 *            type of pagination (pages = 1, columns = 2, foliation = 3, foliation rectoVerso = 4, pagination rectoVerso = 5)
	 * @param paginationStartValue
	 *            start value
	 */

	public Pagination(String[] allSelectedPages, MetadatumImpl[] newPaginated, int paginationScope, String paginationType, int paginationMode,
			String paginationStartValue) {
		this.allSelectedPages = allSelectedPages;
		this.newPaginated = newPaginated;
		this.paginationScope = paginationScope;
		this.paginationType = paginationType;
		this.paginationMode = paginationMode;
		this.paginationStartValue = paginationStartValue;
	}

	/**
	 * creates pagination for scope
	 * 
	 * @return
	 */

	public String doPagination() {

		// return on empty selection or invalid pagination start value
		if (isSelectionEmpty() || !isValidPaginationStartValue()) {
			return null;
		}

		// set "uncounted" pagination value
		if (this.paginationType.equals(PAGINATION_UNCOUNTED)) {
			this.paginationStartValue = "uncounted";
		}

		// set all selected pages to start value
		if (this.paginationScope == SELECTED_PAGES) {
			setSelectedPagesToPaginationStartValue();
			return null;
		}

		// determining first page number
		int firstPageNumber = Integer.parseInt(this.allSelectedPages[0]);

		// determine pagination base
		int paginationBaseValue = getPaginationBaseValue();

		double currentPageNumber = firstPageNumber;

		for (int i = firstPageNumber; i < this.newPaginated.length; i++) {
			String nextPaginationLabel = "";
			if (this.paginationMode == COUNTING_FOLIATION_RECTOVERSO || this.paginationMode == COUNTING_PAGINATION_RECTOVERSO) {
				nextPaginationLabel = getNextPaginationLabel(firstPageNumber, paginationBaseValue, currentPageNumber);
			} else {
				nextPaginationLabel = getNextPaginationLabel(firstPageNumber, paginationBaseValue, currentPageNumber);
			}

			if (this.paginationMode == COUNTING_FOLIATION_RECTOVERSO) {
				if (this.paginationType.equals(PAGINATION_UNCOUNTED)) {
					Helper.setFehlerMeldung("pagination.error.uncountedFoliationNotAllowed");
					return "";
				} else {
					nextPaginationLabel = getRectoVersoSuffixForFoliation(Integer.valueOf(nextPaginationLabel));
				}
			}
			if (this.paginationMode == COUNTING_PAGINATION_RECTOVERSO) {
				if (this.paginationType.equals(PAGINATION_UNCOUNTED)) {
					Helper.setFehlerMeldung("pagination.error.uncountedFoliationNotAllowed");
					return "";
				} else {
					nextPaginationLabel += getRectoVersoSuffixForPagination(currentPageNumber);
				}
			}

			this.newPaginated[i].setWert(nextPaginationLabel);

			currentPageNumber = getNextPageNumber(currentPageNumber);
		}

		return "";
	}

	private String getNextPaginationLabel(int firstPageNumber, int paginationBaseValue, double currentPageNumber) {
		String actualPaginationLabel;
		if (this.paginationType.equals(PAGINATION_ARABIC)) {
			actualPaginationLabel = String.valueOf(paginationBaseValue + (int) currentPageNumber - firstPageNumber);
		} else if (this.paginationType.equals(PAGINATION_ROMAN)) {
			RomanNumeral r = new RomanNumeral();
			r.setValue(paginationBaseValue + (int) currentPageNumber - firstPageNumber);
			actualPaginationLabel = r.toString();
		} else if (this.paginationType.equals(PAGINATION_ARABIC_BRACKET)) {
			actualPaginationLabel = String.valueOf(paginationBaseValue + (int) currentPageNumber - firstPageNumber);
			actualPaginationLabel = "[" + actualPaginationLabel + "]";
		} else if (this.paginationType.equals(PAGINATION_ROMAN_BRACKET)) {
			RomanNumeral r = new RomanNumeral();
			r.setValue(paginationBaseValue + (int) currentPageNumber - firstPageNumber);
			actualPaginationLabel = "[" + r.toString() + "]";

		} else {
			// free text
			actualPaginationLabel = this.paginationStartValue;
		}

		return actualPaginationLabel;
	}

	private String getRectoVersoSuffixForFoliation(int imageNumber) {
		return imageNumber + "v " + (imageNumber + 1) + "r";
	}

	private String getRectoVersoSuffixForPagination(double number) {
		if (((number * 2) % 2) == 0) {
			return "r";
		} else {
			return "v";
		}
	}

	private String getRectoVersoSuffixForSelectedPages(double number) {
		if (((number * 2) % 2) == 0) {
			return "v";
		} else {
			return "r";
		}
	}

	private double getNextPageNumber(double currentPageNumber) {
		switch (this.paginationMode) {
		case COUNTING_PAGINATION:
		case COUNTING_FOLIATION_RECTOVERSO:
			currentPageNumber++;
			break;
		case COUNTING_COLUMNS:
			currentPageNumber = currentPageNumber + 2;
			break;
		case COUNTING_FOLIATION:
		case COUNTING_PAGINATION_RECTOVERSO:
			currentPageNumber = currentPageNumber + 0.5;
			break;
		}
		return currentPageNumber;
	}

	private int getPaginationBaseValue() {

		int paginationBaseValue = 1;

		if (this.paginationType.equals(PAGINATION_ARABIC) || this.paginationType.equals(PAGINATION_ARABIC_BRACKET)) {
			paginationBaseValue = Integer.parseInt(this.paginationStartValue);
		} else if (this.paginationType.equals(PAGINATION_ROMAN) || this.paginationType.equals(PAGINATION_ROMAN_BRACKET)) {
			RomanNumeral r = new RomanNumeral();
			r.setValue(this.paginationStartValue);
			paginationBaseValue = r.intValue();
		}

		return paginationBaseValue;

	}

	public String[] getAllSelectedPages() {
		return this.allSelectedPages;
	}

	public MetadatumImpl[] getNewPaginated() {
		return this.newPaginated;
	}

	public int getPaginationMode() {
		return this.paginationMode;
	}

	public int getPaginationScope() {
		return this.paginationScope;
	}

	public String getPaginationStartValue() {
		return this.paginationStartValue;
	}

	public String getPaginationType() {
		return this.paginationType;
	}

	private boolean isSelectionEmpty() {
		return this.allSelectedPages == null || this.allSelectedPages.length == 0;
	}

	private boolean isValidArabicNumber() {
		try {
			Integer.parseInt(this.paginationStartValue);
			return true;
		} catch (NumberFormatException nfe) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", nfe.getMessage());
			return false;
		}
	}

	private boolean isValidPaginationStartValue() {
		// arabic numbers
		if (this.paginationType.equals(PAGINATION_ARABIC)) {
			return isValidArabicNumber();
		}

		// roman numbers
		if (this.paginationType.equals(PAGINATION_ROMAN)) {
			return isValidRomanNumber();
		}

		return true;
	}

	private boolean isValidRomanNumber() {
		try {
			RomanNumeral roman = new RomanNumeral();
			this.paginationStartValue = this.paginationStartValue.toUpperCase();
			roman.setValue(this.paginationStartValue);
			return true;
		} catch (NumberFormatException nfe) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", nfe.getMessage());
			return false;
		}
	}

	public void setAllSelectedPages(String[] selectedPages) {
		this.allSelectedPages = selectedPages;

	}

	public void setNewPaginated(MetadatumImpl[] newPaginated) {
		this.newPaginated = newPaginated;

	}

	public void setPaginationMode(int numberOfPagesPerImage) {
		this.paginationMode = numberOfPagesPerImage;

	}

	public void setPaginationScope(int paginationScope) {
		this.paginationScope = paginationScope;

	}

	public void setPaginationStartValue(String paginationStartValue) {
		this.paginationStartValue = paginationStartValue;

	}

	public void setPaginationType(String paginationType) {
		this.paginationType = paginationType;

	}

	private void setSelectedPagesToPaginationStartValue() {
		int firstPageNumber = Integer.parseInt(this.allSelectedPages[0]);
		int paginationBaseValue = getPaginationBaseValue();
		double currentPageNumber = firstPageNumber;

		if (this.paginationMode == COUNTING_PAGINATION_RECTOVERSO) {
			currentPageNumber = currentPageNumber + 0.5;
		}

		for (int i = 0; i < this.allSelectedPages.length; i++) {
			int aktuelleID = Integer.parseInt(this.allSelectedPages[i]);
			String nextPaginationLabel = "";
			if (this.paginationMode == COUNTING_FOLIATION_RECTOVERSO || this.paginationMode == COUNTING_PAGINATION_RECTOVERSO) {
				nextPaginationLabel = getNextPaginationLabel(firstPageNumber, paginationBaseValue, currentPageNumber);
			} else {
				nextPaginationLabel = getNextPaginationLabel(firstPageNumber, paginationBaseValue, currentPageNumber);
			}

			if (this.paginationMode == COUNTING_FOLIATION_RECTOVERSO) {
				if (this.paginationType.equals(PAGINATION_UNCOUNTED)) {
					Helper.setFehlerMeldung("pagination.error.uncountedFoliationNotAllowed");
					return;
				} else {
					nextPaginationLabel = getRectoVersoSuffixForFoliation(Integer.valueOf(nextPaginationLabel));
				}
			}
			if (this.paginationMode == COUNTING_PAGINATION_RECTOVERSO) {
				if (this.paginationType.equals(PAGINATION_UNCOUNTED)) {
					Helper.setFehlerMeldung("pagination.error.uncountedFoliationNotAllowed");
					return;
				} else {
					nextPaginationLabel += getRectoVersoSuffixForSelectedPages(currentPageNumber);
				}
			}
			this.newPaginated[aktuelleID].setWert(nextPaginationLabel);

			currentPageNumber = getNextPageNumber(currentPageNumber);
		}
	}

}
