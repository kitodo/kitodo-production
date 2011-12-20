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

package org.goobi.production.search.lucene;

/**
 * this class provides enums to index and search with lucene
 * 
 * @author Robert Sehr
 */
public enum SearchEnums {

	processId("1", "id"), processTitle("2", "title"), project("3", "proj"), user("4", "user"), step("5", "step"), stepTitle("6", "steptitle"), werkId("9", "productid"), werk("10", "product"), werkWert("11",
			"productvalue"), property("12", "property"), propertyValue("13", "propertyvalue"), ruleset("14", "ruleset"), pages("15", "pages"), template(
			"16", "template"), vorlId("17", "vorlID"), vorl("18", "vorl"), vorlWert("19", "vorlValue"), stepdone("20", "stepdone"), stepopen("21",
			"stepopen"), stepinwork("22", "stepinwork"), steplocked("23", "steplocked");

	private String id;
	private String luceneTitle;

	/**
	 * private constructor
	 * 
	 * @param inId
	 * @param inLuceneTitle
	 */
	private SearchEnums(String inId, String inLuceneTitle) {
		id = inId;
		luceneTitle = inLuceneTitle;
	}

	/**
	 * returns title of enum
	 * 
	 * @return title as string
	 */
	public String getLuceneTitle() {
		return luceneTitle;
	}

	/**
	 * returns id of enum
	 * 
	 * @return id as string
	 */
	public String getId() {
		return id;
	}

	/**
	 * returns enum of given id
	 * 
	 * @param inId
	 *            id of enum
	 * @return enum
	 */
	public static SearchEnums getById(String inId) {
		for (SearchEnums unit : SearchEnums.values()) {
			if (unit.getId().equals(inId)) {
				return unit;
			}
		}
		return processId;
	}

}
