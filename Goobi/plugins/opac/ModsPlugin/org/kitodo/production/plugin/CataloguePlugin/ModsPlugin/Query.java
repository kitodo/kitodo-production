/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import org.apache.commons.lang.CharEncoding;

import com.sharkysoft.util.UnreachableCodeException;

class Query {
    private static final String FIELDLESS = "Fieldless query isn’t supported";
    private static final String BRACKET = "Brackets aren’t supported";
    private static final String INCOMPLETE = "Query is syntactically incomplete";

    private String queryUrl = "&query=";

    private static final String AND = "AND";
    private static final String OR = "%2B"; // URL-encoded +
    private static final String NOT = "-";

    private int maximumRecords = 0;

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
        // resulting "queryURL" should look something like this when correctly
        // created: "ead.id=DE-611-HS-2256337"
    }

    // operation must be Query.AND, .OR, .NOT
    void addQuery(String operation, String fieldValue, String fieldName) {
        try {
            if (!Objects.equals(this.queryUrl, "&query=")) {
                this.queryUrl += "+" + operation + "+";
            }
            this.queryUrl += fieldName + "=%22" + URLEncoder.encode(fieldValue, CharEncoding.ISO_8859_1) + "%22";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set maximum records to retrieve for a query.
     * Value is added to getQueryUrl() call if value is greater then 0.
     *
     * @param maximumRecords
     *                  number of maximum records for retrieving
     */
    void setMaximumRecords(int maximumRecords) {
        this.maximumRecords = maximumRecords;
    }

    String getQueryUrl() {
        if (this.maximumRecords > 0) {
            return this.queryUrl + "&maximumRecords=" + this.maximumRecords;
        } else {
            return this.queryUrl;
        }
    }
}
