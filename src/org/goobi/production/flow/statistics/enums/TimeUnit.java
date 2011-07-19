package org.goobi.production.flow.statistics.enums;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of 
 * mass digitization.
 * 
 * Visit the websites for more information. 
 *   - http://gdz.sub.uni-goettingen.de 
 *   - http://www.intranda.com 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 * Boston, MA 02111-1307 USA
 * 
 */

import de.sub.goobi.helper.Helper;

/**
 * Enum of all time units for the statistics
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum TimeUnit {

	days("1", "days", "day", "day"), weeks("2", "weeks", "week", "week"), months(
			"3", "months", "month", "month"), quarters("4", "quarters",
			"quarter", "quarter"), years("5", "years", "year", "year");

	private String id;
	private String title;
	private String sqlKeyword;
	private String singularTitle;

	/**
	 * private constructor for setting id and title
	 * 
	 * @param inTitle
	 *            title as String
	 ****************************************************************************/
	private TimeUnit(String inId, String inTitle, String inKeyword,
			String inSingularTitle) {
		id = inId;
		title = inTitle;
		singularTitle = inSingularTitle;
		sqlKeyword = inKeyword;
	}

	/**
	 * return unique ID for TimeUnit
	 * 
	 * @return unique ID as String
	 ****************************************************************************/
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return sqlKeyword for use in querys
	 */
	public String getSqlKeyword() {
		return sqlKeyword;
	}

	/**
	 * return singular name for timeUnit
	 *  
	 * @return singularTitle
	 ****************************************************************************/
	public String getSingularTitle() {
		return singularTitle;
	}

	/**
	 * return localized title for TimeUnit from standard-jsf-messages-files
	 * 
	 * @return localized title
	 ****************************************************************************/
	public String getTitle() {
		return Helper.getTranslation(title);
	}

	/**
	 * get TimeUnit by unique ID 
	 * 
	 * @param inId the unique ID
	 * @return {@link TimeUnit} with given ID
	 ****************************************************************************/
	public static TimeUnit getById(String inId) {
		for (TimeUnit unit : TimeUnit.values()) {
			if (unit.getId().equals(inId)) {
				return unit;
			}
		}
		return days;
	}

}
