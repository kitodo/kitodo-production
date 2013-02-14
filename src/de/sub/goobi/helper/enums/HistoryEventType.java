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
 * Enum of all history event types for all history events for processes
 * 
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
public enum HistoryEventType {
	/**
	 * default type is unknown for all properties, which still dont have a specific
	 * type
	 */
	unknown(0, "unknown",false, false, null),

	/** storageDifference */
	storageDifference(1, "storageDifference", true, false, null),

	/** imagesWorkDiff */
	imagesWorkDiff(2, "imagesWorkDiff", true, false, null),

	/** imagesMasterDiff */
	imagesMasterDiff(3, "imagesMasterDiff", true, false, null),

	/** metadataDiff */
	metadataDiff(4, "metadataDiff", true, false, null),
	
	/** docstructDiff, */
	docstructDiff(5, "docstructDiff", true, false, null),
	
	/** stepDone, order number and title */
	stepDone(6, "stepDone", true, true, "min"),
	
	/** stepOpen, order number and title */
	stepOpen(7, "stepOpen", true, true, "min"),
	
	/** stepInWork, order number and title */
	stepInWork(8, "stepInWork", true, true, null ),
	
	/** stepError, step order number, step title */
	stepError(9, "stepError", true, true, null),
	
	/** stepError, step order number, step title */
	stepLocked(10, "stepLocked", true, true, "max"),
	
	/** bitonal Difference - without function yet */
	bitonal(11, "imagesBitonalDiff", true, false, null),
	
	/** grayscale Difference - without function yet */
	grayScale(12, "imagesGrayScaleDiff", true, false, null),
	
	/** color Difference - without function yet */
	color(13, "imagesColorDiff", true, false, null)
	
	
	
	;
	
	

	private int value;
	private String title;
	private Boolean isNumeric;
	private Boolean isString;
	private String groupingExpression;

	/**
	 * private constructor, initializes integer value, title and sets boolean, 
	 * if EventType contains string and/or numeric content
	 */
	private HistoryEventType(int inValue, String inTitle, Boolean inIsNumeric, Boolean inIsString, String groupingExpression) {
		value = inValue;
		title = inTitle;
		isNumeric = inIsNumeric;
		isString = inIsString;
		this.groupingExpression = groupingExpression;
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
	 * get title from type
	 * 
	 * @return title as translated string for current locale from standard-jsf-messages
	 */
	public String getTitle() {
		return Messages.getString(title);
	}

	
	/**
	 * return if type contains numeric content
	 * 
	 * @return isNumeric as {@link Boolean}
	 */
	
	public Boolean isNumeric() {
		return isNumeric;
	}

	/**
	 * return if type contains string content
	 * 
	 * @return isNumeric as {@link String}
	 */
	public Boolean isString() {
		return isString;
	}
	
	/**
	 * return grouping function if needed
	 * 
	 * @return groupingExpression as{@link String}
	 */
	public String getGroupingFunction(){
		return this.groupingExpression;
	}

	/**
	 * retrieve history event type by integer value, neccessary for database handlings,
	 * where only integer is saved but not type safe
	 * 
	 * @param inType
	 *            as integer value
	 * @return {@link HistoryEventType} for given integer
	 */
	public static HistoryEventType getTypeFromValue(Integer inType) {
		if (inType != null) {
			for (HistoryEventType ss : values()) {
				if (ss.getValue() == inType.intValue())
					return ss;
			}
		}
		return unknown;
	}

}