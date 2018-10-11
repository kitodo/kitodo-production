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

package org.kitodo.production.plugin.opac.pica;

import java.net.URLEncoder;

import org.apache.commons.lang.CharEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Query {
    private static final Logger logger = LogManager.getLogger(Query.class);

    private static final String FIELDLESS = "Fieldless query isn’t supported";
    private static final String BRACKET = "Brackets aren’t supported";
    private static final String INCOMPLETE = "Query is syntactically incomplete";

    private String queryUrl;
    private int queryTermNumber = 0;

    private static final String AND = "*";
    private static final String OR = "%2B"; // URL-encoded +
    private static final String NOT = "-";

    private static final String FIRST_OPERATOR = "SRCH";

    private static final String OPERATOR = "&ACT";
    private static final String QUERY = "&TRM";
    private static final String FIELD = "&IKT";

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
                            break;
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
                        if (term.length() == 0) {
                            throw new IllegalArgumentException(INCOMPLETE);
                        }
                        addQuery(operator, term.toString(), field.toString());
                        operator = AND;
                        field = new StringBuilder();
                        term = new StringBuilder(32);
                        state = 5;
                    } else {
                        term.appendCodePoint(codePoint);
                    }
                    break;
                case 4:
                    if (codePoint == '"') {
                        addQuery(operator, term.toString(), field.toString());
                        operator = AND;
                        field = new StringBuilder();
                        term = new StringBuilder(32);
                        state = 5;
                    } else {
                        term.appendCodePoint(codePoint);
                    }
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
    }

    // operation must be Query.AND, .OR, .NOT
    private void addQuery(String operation, String query, String fieldNumber) {

        // ignore boolean operation for first term
        if (this.queryTermNumber == 0) {
            this.queryUrl = OPERATOR + this.queryTermNumber + "=" + FIRST_OPERATOR;
        } else {
            this.queryUrl += OPERATOR + this.queryTermNumber + "=" + operation;
        }

        this.queryUrl += FIELD + this.queryTermNumber + "=" + fieldNumber;

        try {
            this.queryUrl += QUERY + this.queryTermNumber + "=" + URLEncoder.encode(query, CharEncoding.ISO_8859_1);
        } catch (Exception e) {
            logger.error(e);
        }

        this.queryTermNumber++;
    }

    String getQueryUrl() {
        return this.queryUrl;
    }

}
