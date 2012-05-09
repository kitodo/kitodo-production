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

package de.sub.goobi.helper.enums;

import de.sub.goobi.helper.Messages;

/**
 * Enum for edit type of task steps each one has an integer value, and a title
 * 
 * @author Steffen Hankiewicz
 * @version 17.05.2009
 */
public enum StepEditType {
	/**
	 * default type is unknown for all steps, which still dont have a specific
	 * type
	 */
	UNNOWKN(0, "unbekannt"),

	/** manual single workflow for regular workflow handling */
	MANUAL_SINGLE(1, "manuellSingleWorkflow"),

	/**
	 * manual multi workflow for lots of data like image processing with pages
	 * of steps
	 */
	MANUAL_MULTI(2, "manuellMultiWorkflow"),

	/** administrativ = all kinds of steps changed through administrative gui */
	ADMIN(3, "administrativ"),

	/** automatic = all kinds of automatic steps */
	AUTOMATIC(4, "automatic");

	private int value;
	private String title;

	/**
	 * private constructor, initializes integer value and title
	 */
	private StepEditType(int inValue, String inTitle) {
		value = inValue;
		title = inTitle;
	}

	/**
	 * return integer value for database savings
	 * 
	 * @return value as integer
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * get title from editType
	 * 
	 * @return title as translated string for current locale
	 */
	public String getTitle() {
		return Messages.getString(title);
	}

	/**
	 * retrieve editType by integer value, neccessary for database handlings,
	 * where only integer is saved but not type safe
	 * 
	 * @param editType
	 *            as integer value
	 * @return {@link StepEditType} for given integer
	 */
	public static StepEditType getTypeFromValue(Integer editType) {
		if (editType != null) {
			for (StepEditType ss : values()) {
				if (ss.getValue() == editType.intValue())
					return ss;
			}
		}
		return UNNOWKN;
	}

}