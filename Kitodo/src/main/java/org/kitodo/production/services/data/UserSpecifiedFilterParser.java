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

package org.kitodo.production.services.data;

import static java.lang.Character.charCount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bytebuddy.utility.nullability.MaybeNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parser for breaking down the filter string entered by the user. The filter
 * string has a very interesting historical structure and there is no standard
 * method for breaking it down, so everything that needs to be encoded manually
 * has to be found again. This is possible but not trivial.
 */
class UserSpecifiedFilterParser {
    private static final Logger logger = LogManager.getLogger(UserSpecifiedFilterParser.class);

    /**
     * The string is parsed and converted to a map.
     * 
     * @param filterString
     *            user input
     * @return map of search on filter field
     */
    static Map<FilterField, Collection<UserSpecifiedFilter>> parse(String filterString) {
        var filters = new EnumMap<FilterField, Collection<UserSpecifiedFilter>>(FilterField.class);
        for (UserSpecifiedFilter filter : parseFilters(filterString)) {
            filters.computeIfAbsent(filter.getFilterField(), missingFilter -> new ArrayList<>()).add(filter);
        }
        return filters;
    }

    /**
     * Detects whether there are groups enclosed in quotation marks. A closing
     * quotation mark at the end may also be missing. Outside of quotation
     * marks, spaces are a token separator, inside they are not.
     * 
     * @param filter
     *            user-entered filter
     * @return search token
     */
    static List<UserSpecifiedFilter> parseFilters(String filter) {
        List<UserSpecifiedFilter> queryTokens = new ArrayList<>();
        StringBuilder tokenCollector = new StringBuilder();
        boolean inQuotes = false;
        for (int offset = 0; offset < filter.length(); offset += charCount(filter.codePointAt(offset))) {
            int glyph = filter.codePointAt(offset);
            if (glyph == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes && glyph <= ' ') {
                if (tokenCollector.length() > 0) {
                    queryTokens.addAll(parseParentheses(tokenCollector));
                    tokenCollector = new StringBuilder();
                }
            } else {
                // add characters but, no spaces at the beginning
                if (tokenCollector.length() > 0 || glyph > ' ') {
                    tokenCollector.appendCodePoint(glyph);
                }
            }
        }
        trimRight(tokenCollector);
        if (tokenCollector.length() > 0) {
            UserSpecifiedFilter userSpecifiedFilter = parseQueryPart(tokenCollector.toString());
            if (Objects.nonNull(userSpecifiedFilter)) {
                queryTokens.add(userSpecifiedFilter);
            }
        }
        logger.debug("`{}´ -> {}", filter, queryTokens);
        return queryTokens;
    }

    /**
     * Within a sequence marked with quotation marks, there can be inner groups
     * marked with parentheses. Within the sequence delimited by quotation
     * marks, a vertical bar is a separator, but within the parentheses, it is
     * not.
     * 
     * @param input
     *            a group that was marked with quotation marks
     * @return search token
     */
    private static List<UserSpecifiedFilter> parseParentheses(StringBuilder input) {
        List<UserSpecifiedFilter> queryTokens = new ArrayList<>();
        StringBuilder tokenCollector = new StringBuilder();
        boolean inParentheses = false;
        for (int offset = 0; offset < input.length(); offset += charCount(input.codePointAt(offset))) {
            int glyph = input.codePointAt(offset);
            if (glyph == '(' && !inParentheses) {
                inParentheses = !inParentheses;
            } else if (glyph == ')' && inParentheses) {
                inParentheses = !inParentheses;
            } else if (!inParentheses && glyph == '|') {
                if (tokenCollector.length() > 0) {
                    trimRight(tokenCollector);
                    queryTokens.add(parseQueryPart(tokenCollector.toString()));
                    tokenCollector = new StringBuilder();
                }
            } else {
                // add characters but, no spaces at the beginning
                if (tokenCollector.length() > 0 || glyph > ' ') {
                    tokenCollector.appendCodePoint(glyph);
                }
            }
        }
        if (tokenCollector.length() > 0) {
            trimRight(tokenCollector);
            queryTokens.add(parseQueryPart(tokenCollector.toString()));
        }
        return queryTokens;
    }

    /**
     * Removes tailing spaces in a string builder.
     * 
     * @param stringBuilder
     *            to modified string builder
     */
    private static void trimRight(StringBuilder stringBuilder) {
        // remove spaces at the end
        int lastPos = stringBuilder.length() - 1;
        do {
            if (lastPos < 0 || stringBuilder.charAt(lastPos) > ' ') {
                break;
            }
            stringBuilder.setLength(lastPos);
            lastPos--;
        } while (lastPos >= 0);
    }

    private static final Pattern ID_SEARCH_PATTERN = Pattern.compile("\\s*(\\d+)\\s*(?:-\\s*(\\d+)\\s)?");

    /**
     * Everything that belongs together according to the above rules is now
     * processed as one search token.
     * 
     * @param item
     *            search item
     * @return filter for search item, or {@code null} if it doesn’t make sense
     */
    @MaybeNull
    private static UserSpecifiedFilter parseQueryPart(String item) {
        boolean substract = item.startsWith("-");
        if (substract) {
            item = item.substring(1, item.length());
        }
        boolean operand = !substract;
        int colon = item.indexOf(":");
        if (colon < 0) {
            // if there is no colon this is a simple search keyword for the
            // index search
            return new IndexQueryPart(FilterField.MISC, item.toString(), operand);
        } else {
            // if there is a colon: disassemble the string
            String column = item.substring(0, colon).toLowerCase();
            String value = item.substring(colon + 1);
            return parseQueryPart(column, value, operand);
        }
    }

    private static UserSpecifiedFilter parseQueryPart(String column, String value, boolean operand) {
        // is the first one a known search field?
        FilterField filterField = FilterField.ofString(column);
        if (Objects.isNull(filterField)) {
            // if not, this is a search for a specific metadata
            return new IndexQueryPart(column, FilterField.MISC, value, operand);
        } else {
            // we found a known search field
            if (StringUtils.isBlank(value)) {
                // Value is empty: The search only requires that the field
                // exists. Only interesting for fields that may not exist.
                if (Objects.isNull(filterField.getSearchField())) {
                    return null;
                }
                return new IndexQueryPart(filterField, null, operand);
            }
            // If the search consists of exactly one number, or of a
            // number-to-number sequence, then the database is filtered
            // according to the ID or IDs.
            Matcher idSearch = ID_SEARCH_PATTERN.matcher(value);
            if (idSearch.matches()) {
                return new DatabaseQueryPart(filterField, idSearch.group(1), idSearch.group(2), operand);
            } else {
                // if the search allows an additional colon, search for it
                if (filterField.isDivisible()) {
                    // the field allows another colon: then search for it
                    int anotherColon = value.indexOf(":");
                    if (anotherColon >= 0) {
                        // a second colon was found
                        // then split the string
                        String metadataKey = value.substring(0, anotherColon);
                        String metadataValue = value.substring(anotherColon + 1);
                        return new IndexQueryPart(metadataKey, filterField, metadataValue, operand);
                    }
                }
                // the field does not allow another colon, or there was no
                return new IndexQueryPart(filterField, value, operand);
            }
        }
    }
}
