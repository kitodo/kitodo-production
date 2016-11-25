/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.lang.CharEncoding;

import com.sharkysoft.util.UnreachableCodeException;

class Query {
	private static final String FIELDLESS = "Fieldless query isn’t supported";
	private static final String BRACKET = "Brackets aren’t supported";
	private static final String INCOMPLETE = "Query is syntactically incomplete";

	private String queryUrl = "&query=";

	private static final String AND = "*";
	private static final String OR = "%2B"; //URL-encoded +
	private static final String NOT = "-";

	// Identifier fieldNumber from UI: 12; should use "EAD.ID" with unique number instead, perhaps
	private static final HashMap<String, String> fieldMappings = new HashMap<>();
	static
	{
		fieldMappings.put("4", "ead.title");
		fieldMappings.put("12", "ead.id");
		fieldMappings.put("20", "ead.repository");
	}

	// Example: Kalliope-URL returning the mods data for a given ead.id
	// http://kalliope-verbund.info/sru?version=1.2&operation=searchRetrieve&query=ead.id=DE-611-HS-2256337&recordSchema=mods

	Query(String query, String fieldNumber) {
		addQuery(null, query, fieldNumber);
	}

	/**
	 * Query constructor. Constructs a query from a String. For the query
	 * semantics, see
	 * {@link org.goobi.production.plugin.CataloguePlugin.QueryBuilder}.
	 *
	 * @param queryString
	 *            Query string to parse
	 * @throws IllegalArgumentException
	 *             if the query is syntactically incomplete (i.e. unterminated
	 *             String literal), contains fieldless tokens or bracket
	 *             expressions
	 */
	Query(String queryString) {
		int state = 0;
		String operator = null;
		StringBuilder field = new StringBuilder();
		StringBuilder term = new StringBuilder(32);
		for (int index = 0; index < queryString.length(); index++) {
			int codePoint = queryString.codePointAt(index);
			switch (state) {
			case 0:
				switch (codePoint) {
				case ' ':
					continue;
				case '"':
					throw new IllegalArgumentException(FIELDLESS);
				case '(':
					throw new IllegalArgumentException(BRACKET);
				case '-':
					operator = NOT;
				default:
					field.appendCodePoint(codePoint);
				}
				state = 1;
				break;
			case 1:
				switch (codePoint) {
				case ' ':
					throw new IllegalArgumentException(FIELDLESS);
				case ':':
					state = 2;
					break;
				default:
					field.appendCodePoint(codePoint);
				}
				break;
			case 2:
				switch (codePoint) {
				case ' ':
					continue;
				case '"':
					state = 4;
					break;
				case '(':
					throw new IllegalArgumentException(BRACKET);
				default:
					term.appendCodePoint(codePoint);
					state = 3;
				}
				break;
			case 3:
				if (codePoint == ' ') {
					if (term.length() == 0)
						throw new IllegalArgumentException(INCOMPLETE);
					addQuery(operator, term.toString(), field.toString());
					operator = AND;
					field = new StringBuilder();
					term = new StringBuilder(32);
					state = 5;
				} else
					term.appendCodePoint(codePoint);
				break;
			case 4:
				if (codePoint == '"') {
					addQuery(operator, term.toString(), field.toString());
					operator = AND;
					field = new StringBuilder();
					term = new StringBuilder(32);
					state = 5;
				} else
					term.appendCodePoint(codePoint);
				break;
			case 5:
				switch (codePoint) {
				case ' ':
					continue;
				case '-':
					operator = NOT;
					break;
				case '|':
					operator = OR;
					break;
				default:
					field.appendCodePoint(codePoint);
				}
				state = 1;
				break;
			default:
				throw new UnreachableCodeException();
			}
		}
		if (state == 3) {
			addQuery(operator, term.toString(), field.toString());
		}
		if (state != 3 && state != 5) {
			throw new IllegalArgumentException(INCOMPLETE);
		}
		// resulting "queryURL" should look something like this when correctly created: "ead.id=DE-611-HS-2256337"
	}

	//operation must be Query.AND, .OR, .NOT
	void addQuery(String operation, String query, String fieldNumber) {
		 try{
			 if(fieldMappings.containsKey(fieldNumber)){
				 if(!Objects.equals(this.queryUrl, "&query=")){
					 this.queryUrl += "+"+operation+"+";
				 }
				 this.queryUrl += fieldMappings.get(fieldNumber) + "=%22" + URLEncoder.encode(query, CharEncoding.ISO_8859_1) + "%22";
			 }
		 }catch (UnsupportedEncodingException e) {
			 e.printStackTrace();
		 }
	 }

	String getQueryUrl() {
		 return this.queryUrl;
	 }
}
