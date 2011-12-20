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

package de.unigoettingen.sub.search.opac;

import java.net.URLEncoder;

public class Query {

	private String queryUrl;
	private int queryTermNumber = 0;
	
	public static final String AND = "*";
	public static final String OR = "%2B"; //URL-encoded +
	public static final String NOT = "-";
//	public static final String AND = "AND";
//	public static final String OR = "OR";
//	public static final String NOT = "-";
	private static final String FIRST_OPERATOR = "SRCH";
	
	
	private static final String OPERATOR = "&ACT";
	private static final String QUERY = "&TRM";
	private static final String FIELD = "&IKT";
	
	public Query() {
		super();
	}

	public Query(String query, String fieldNumber) {
		super();
		this.addQuery(null, query, fieldNumber);
	}

	//operation must be Query.AND, .OR, .NOT 
	 public void addQuery(String operation, String query, String fieldNumber){
		 
		 //ignore boolean operation for first term
		 if (queryTermNumber == 0){
			 queryUrl = OPERATOR + queryTermNumber + "=" + FIRST_OPERATOR;
		 }else{
			 queryUrl += OPERATOR + queryTermNumber + "=" + operation;
		 }
		 
		 
		 queryUrl += FIELD + queryTermNumber + "=" + fieldNumber;
		 
		 try{
			 queryUrl += QUERY + queryTermNumber + "=" + 
			 	URLEncoder.encode(query , GetOpac.URL_CHARACTER_ENCODING);
		 }catch (Exception e) {
			 e.printStackTrace();
		}
		 
		 queryTermNumber++;
	 }
	 
	 public String getQueryUrl(){
		 return queryUrl;
	 }
	 
}
