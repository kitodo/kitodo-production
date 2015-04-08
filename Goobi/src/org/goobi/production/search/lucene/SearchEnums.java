//package org.goobi.production.search.lucene;
//
///**
// * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
// * 
// * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
// * 
// * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
// * 
// * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// * 
// * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
// * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
// * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
// * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
// * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
// * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
// * exception statement from your version.
// */
//
///**
// * this class provides enums to index and search with lucene
// * 
// * @author Robert Sehr
// */
//
//@Deprecated
//public enum SearchEnums {
//
//	processId("1", "id"), processTitle("2", "title"), project("3", "proj"), user("4", "user"), step("5", "step"), stepTitle("6", "steptitle"), werkId(
//			"9", "productid"), werk("10", "product"), werkWert("11", "productvalue"), property("12", "property"), propertyValue("13", "propertyvalue"), ruleset(
//			"14", "ruleset"), pages("15", "pages"), template("16", "template"), vorlId("17", "vorlID"), vorl("18", "vorl"), vorlWert("19",
//			"vorlValue"), stepdone("20", "stepdone"), stepopen("21", "stepopen"), stepinwork("22", "stepinwork"), steplocked("23", "steplocked");
//
//	private String id;
//	private String luceneTitle;
//
//	/**
//	 * private constructor
//	 * 
//	 * @param inId
//	 * @param inLuceneTitle
//	 */
//	private SearchEnums(String inId, String inLuceneTitle) {
//		id = inId;
//		luceneTitle = inLuceneTitle;
//	}
//
//	/**
//	 * returns title of enum
//	 * 
//	 * @return title as string
//	 */
//	public String getLuceneTitle() {
//		return luceneTitle;
//	}
//
//	/**
//	 * returns id of enum
//	 * 
//	 * @return id as string
//	 */
//	public String getId() {
//		return id;
//	}
//
//	/**
//	 * returns enum of given id
//	 * 
//	 * @param inId
//	 *            id of enum
//	 * @return enum
//	 */
//	public static SearchEnums getById(String inId) {
//		for (SearchEnums unit : SearchEnums.values()) {
//			if (unit.getId().equals(inId)) {
//				return unit;
//			}
//		}
//		return processId;
//	}
//
//}
