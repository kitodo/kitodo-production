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

package org.kitodo.production.forms;

import static java.util.Map.entry;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.primefaces.PrimeFaces;
import org.primefaces.event.data.SortEvent;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class BaseListView extends BaseForm {
    
    /**
     * Return the field that a list view is currently sorted by as URL query parameter.
     * 
     * @return the field name as URL query parameter (e.g. "title")
     */
    public String getSortByField() {
        return sortBy.getField();
    }

    /**
     * Return the sort order that a list view is currently sorted in as URL query parameter.
     * 
     * @return the sort order as URL query parameter (either "asc" or "desc")
     */
    public String getSortByOrder() {
        return Map.ofEntries(entry(SortOrder.ASCENDING, "asc"), entry(SortOrder.DESCENDING, "desc")).getOrDefault(sortBy.getOrder(), "");
    }

    /**
     * The set of allowed sort fields for a specific list view. Needs to be overwritten by each list view.
     * 
     * @return the set of allowed sort fields
     */
    protected Set<String> getAllowedSortFields() {
        return Collections.emptySet();
    }

    /**
     * Set list view sort configuration based on URL query parameters "sortField" and "sortOrder".
     * @param field the field to sort on
     * @param order the order to sort in
     */
    public void setSortByFromTemplate(String field, String order) {
        if (Objects.nonNull(field) && !field.isEmpty() && Objects.nonNull(order) && !order.isEmpty()) {
            if (getAllowedSortFields().contains(field)) {
                SortOrder sortOrder = Map.ofEntries(
                        entry("asc", SortOrder.ASCENDING), entry("desc", SortOrder.DESCENDING)
                    ).getOrDefault(order, SortOrder.ASCENDING);
                sortBy = SortMeta.builder().field(field).order(sortOrder).build();
            }
        }
    }

    /**
     * Event listener in case a user changes the sortBy settings.
     * 
     * @param sortEvent SortEvent triggered by data tables
     */
    public void onSortChange(SortEvent sortEvent) {
        Map<String, SortMeta> sortByMap = sortEvent.getSortBy();
        for (SortMeta sortMeta : sortByMap.values()) {
            if (sortMeta.getOrder() != SortOrder.UNSORTED) {
                String order = Map.ofEntries(
                        entry(SortOrder.ASCENDING, "asc"), entry(SortOrder.DESCENDING, "desc")
                    ).getOrDefault(sortMeta.getOrder(), "");
                String script = "kitodo.updateQueryParameter('sortField', '" + sortMeta.getField() + "');";
                script += "kitodo.updateQueryParameter('sortOrder', '" + order + "');";
                script += "kitodo.updateQueryParameter('firstRow', 0);";
                PrimeFaces.current().executeScript(script);
                this.firstRow = 0;
                this.sortBy = sortMeta;
                break;
            }
        }
    }

    /**
     * Return combined the list options (URL query parameters) that can be forwarded to edit view or used to reload page.
     * 
     * @return the combined list view options (URL query parameters)
     */
    public String getCombinedListOptions() {
        return Map.ofEntries(
            entry("firstRow",  String.valueOf(getFirstRow())),
            entry("sortField", getSortByField()), 
            entry("sortOrder", getSortByOrder()), 
            entry("filter", getFilter())
        ).entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getValue()) && !entry.getValue().isEmpty())
            .map(entry -> entry.getKey() + "=" + entry.getValue().replace("&", "%26"))
            .collect(Collectors.joining("&"));
    }

}
