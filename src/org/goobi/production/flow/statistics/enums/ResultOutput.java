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
 * Enum of all result output possibilities for the statistics
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum ResultOutput {

	chart("1", "chart"), table("2", "table"), chartAndTable("3",
			"chartAndTable");

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
	private ResultOutput(String inId, String inTitle) {
		id = inId;
		title = inTitle;
	}

	/**
	 * return unique ID for result output
	 * 
	 * @return unique ID as String
	 ****************************************************************************/
	public String getId() {
		return id;
	}

	/**
	 * return localized title for result output
	 * 
	 * @return localized title
	 ****************************************************************************/
	public String getTitle() {
		return Messages.getString(title);
	}

	/**
	 * get presentation output by unique ID
	 * 
	 * @param inId
	 *            the unique ID
	 * @return {@link ResultOutput} with given ID
	 ****************************************************************************/
	public static ResultOutput getById(String inId) {
		for (ResultOutput unit : ResultOutput.values()) {
			if (unit.getId().equals(inId)) {
				return unit;
			}
		}
		return table;
	}

}
