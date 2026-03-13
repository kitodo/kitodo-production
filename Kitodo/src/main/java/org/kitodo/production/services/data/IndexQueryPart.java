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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.data.database.beans.ProcessKeywords;

/**
 * A portion of the filter entered by the user that is resolved through the
 * search index.
 */
class IndexQueryPart implements UserSpecifiedFilter {

    private static final char VALUE_SEPARATOR = 'q';
    private final List<String> lookfor = new ArrayList<>();
    private final FilterField filterField;
    private final boolean operand;

    /**
     * Constructor. Creates a new IndexQueryPart.
     * 
     * @param filterField
     *            search field selected by the user
     * @param values
     *            search terms
     * @param operand
     *            whether the search must match
     */
    IndexQueryPart(FilterField filterField, String values, boolean operand) {
        this.filterField = filterField;
        for (String value : splitValues(values)) {
            if (value.length() >= ProcessKeywords.LENGTH_MIN_DEFAULT) {
                this.lookfor.add(normalize(value));
            }
        }
        this.operand = operand;
    }

    /**
     * Constructor. Creates a new IndexQueryPart.
     * 
     * @param key
     *            in the ruleset
     * @param filterField
     *            search field selected by the user
     * @param values
     *            search terms
     * @param operand
     *            whether the search must match
     */
    IndexQueryPart(String key, FilterField filterField, String values, boolean operand) {
        this.filterField = filterField;
        for (String value : splitValues(values)) {
            lookfor.add(normalize(key) + VALUE_SEPARATOR + normalize(value));
        }
        this.operand = operand;
    }

    private List<String> splitValues(String value) {
        String i = value != null ? value : "";
        return Arrays.asList(i.split("[ ,\\-._]+"));
    }

    private String normalize(String string) {
        return string.toLowerCase().replaceAll("[\0-/:-`{-¿]", "");
    }

    @Override
    public FilterField getFilterField() {
        return filterField;
    }

    /**
     * Adds the prepared index search terms for this filter to the list of index queries.
     *
     * @param indexQueries
     *            puts the prepared tokens for the search queries here
     */
    void putQueryParameters(List<Pair<FilterField, String>> indexQueries) {
        for (String lookingFor : lookfor) {
            indexQueries.add(Pair.of(filterField, lookingFor));
        }
    }

    @Override
    public String toString() {
        return "~(" + filterField.getSearchField() + ')' + String.join(" ", lookfor) + "~";
    }
}
