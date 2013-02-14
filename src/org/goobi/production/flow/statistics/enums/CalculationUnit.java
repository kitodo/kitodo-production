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

package org.goobi.production.flow.statistics.enums;

import de.sub.goobi.helper.Messages;

/**
 * Enum of all calculation units for the statistics
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum CalculationUnit {

	volumes("1", "volumes"), pages("2", "pages"), volumesAndPages("3",
			"volumesAndPages");

	private String id;
	private String title;

	/**
	 * private constructor for setting id and title
	 * 
	 * @param inTitle
	 *            title as String
	 * @param inId
	 *            id as string
	 ****************************************************************************/
	private CalculationUnit(String inId, String inTitle) {
		id = inId;
		title = inTitle;
	}

	/**
	 * return unique ID for CalculationUnit
	 * 
	 * @return unique ID as String
	 ****************************************************************************/
	public String getId() {
		return id;
	}

	/**
	 * return localized title for CalculationUnit
	 * 
	 * @return localized title
	 ****************************************************************************/
	public String getTitle() {
		return Messages.getString(title);
	}

	/**
	 * get CalculationUnit by unique ID
	 * 
	 * @param inId
	 *            the unique ID
	 * @return {@link CalculationUnit} with given ID
	 ****************************************************************************/
	public static CalculationUnit getById(String inId) {
		for (CalculationUnit unit : CalculationUnit.values()) {
			if (unit.getId().equals(inId)) {
				return unit;
			}
		}
		return volumes;
	}

}
