package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

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
import java.net.URLEncoder;

import com.sharkysoft.util.UnreachableCodeException;

class Query {
	private static final String FIELDLESS = "Fieldless query isn’t supported";
	private static final String BRACKET = "Brackets aren’t supported";
	private static final String INCOMPLETE = "Query is syntactically incomplete";

	private String queryUrl;
	private int queryTermNumber = 0;
	
	private static final String AND = "*";
	private static final String OR = "%2B"; //URL-encoded +
	private static final String NOT = "-";

	private static final String FIRST_OPERATOR = "SRCH";
	
	private static final String OPERATOR = "&ACT";
	private static final String QUERY = "&TRM";
	private static final String FIELD = "&IKT";
	
	private Query() {
	}

	Query(String query, String fieldNumber) {
		this.addQuery(null, query, fieldNumber);
	}

	Query(String inString) {
		int state = 0;
		String operator = null;
		StringBuilder field = new StringBuilder();
		StringBuilder term = new StringBuilder(32);
		for (int i = 0; i < inString.length(); i++) {
			int c = inString.codePointAt(i);
			switch (state) {
			case 0:
				switch (c) {
				case ' ':
					continue;
				case '"':
					throw new IllegalArgumentException(FIELDLESS);
				case '(':
					throw new IllegalArgumentException(BRACKET);
				case '-':
					operator = NOT;
				default:
					field.appendCodePoint(c);
				}
				state = 1;
				break;
			case 1:
				switch (c) {
				case ' ':
					throw new IllegalArgumentException(FIELDLESS);
				case ':':
					state = 2;
					break;
				default:
					field.appendCodePoint(c);
				}
				break;
			case 2:
				switch (c) {
				case ' ':
					continue;
				case '"':
					state = 4;
					break;
				case '(':
					throw new IllegalArgumentException(BRACKET);
				default:
					term.appendCodePoint(c);
					state = 3;
				}
				break;
			case 3:
				if (c == ' ') {
					if (term.length() == 0)
						throw new IllegalArgumentException(INCOMPLETE);
					addQuery(operator, term.toString(), field.toString());
					operator = AND;
					field = new StringBuilder();
					term = new StringBuilder(32);
					state = 5;
				} else
					term.appendCodePoint(c);
				break;
			case 4:
				if (c == '"') {
					addQuery(operator, term.toString(), field.toString());
					operator = AND;
					field = new StringBuilder();
					term = new StringBuilder(32);
					state = 5;
				} else
					term.appendCodePoint(c);
				break;
			case 5:
				switch (c) {
				case ' ':
					continue;
				case '-':
					operator = NOT;
					break;
				case '|':
					operator = OR;
					break;
				default:
					field.appendCodePoint(c);
				}
				state = 1;
				break;
			default:
				throw new UnreachableCodeException();
			}
			if (state == 3) {
				addQuery(operator, term.toString(), field.toString());
			}
			if (state != 3 && state != 5)
				throw new IllegalArgumentException(INCOMPLETE);
		}
	}

	//operation must be Query.AND, .OR, .NOT 
	private void addQuery(String operation, String query, String fieldNumber) {
		 
		 //ignore boolean operation for first term
		 if (this.queryTermNumber == 0){
			 this.queryUrl = OPERATOR + this.queryTermNumber + "=" + FIRST_OPERATOR;
		 }else{
			 this.queryUrl += OPERATOR + this.queryTermNumber + "=" + operation;
		 }
		 
		 
		 this.queryUrl += FIELD + this.queryTermNumber + "=" + fieldNumber;
		 
		 try{
			 this.queryUrl += QUERY + this.queryTermNumber + "=" + 
			 	URLEncoder.encode(query , GetOpac.URL_CHARACTER_ENCODING);
		 }catch (Exception e) {
			 e.printStackTrace();
		}
		 
		 this.queryTermNumber++;
	 }
	 
	String getQueryUrl() {
		 return this.queryUrl;
	 }
	 
}
