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

package org.kitodo.production.filters;

import org.kitodo.production.enums.FilterPart;

public class Suggestion {

    private final String input;
    private final String suggestion;
    private final FilterPart filterPart;


    /**
     * Default constructor.
     */
    public Suggestion(String input, String suggestion, FilterPart filterPart) {
        this.input = input;
        this.suggestion = suggestion;
        this.filterPart = filterPart;
    }

    /**
     * Get input.
     *
     * @return value of input
     */
    public String getInput() {
        return input;
    }

    /**
     * Get suggestion.
     *
     * @return value of suggestion
     */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * Determine which part of the filter the suggestion is for.
     *
     * @return value of filterPart
     */
    public FilterPart getFilterPart() {
        return filterPart;
    }
}
