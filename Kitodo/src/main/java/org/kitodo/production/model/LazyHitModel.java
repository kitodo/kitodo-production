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

package org.kitodo.production.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.config.OPACConfig;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyHitModel extends LazyDataModel<Object> {

    private String selectedCatalog = "";
    private String selectedField = "";
    private String searchTerm = "";

    private SearchResult searchResult = null;

    @Override
    public Object getRowData(String rowKey) {
        return null;
    }

    @Override
    public Object getRowKey(Object inObject) {
        return null;
    }

    @Override
    public int getRowCount() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getNumberOfHits();
        } else {
            return 0;
        }
    }

    @Override
    public List<Object> load(int first, int resultSize, String sortField, SortOrder sortOrder, Map filters) {

        searchResult = ServiceManager.getImportService().performSearch(
                this.selectedField, getSearchTermWithDelimiter(this.searchTerm), this.selectedCatalog, first, resultSize);

        if (Objects.isNull(searchResult) || Objects.isNull(searchResult.getHits())) {
            return Collections.emptyList();
        }
        return searchResult.getHits().stream().map(r -> (Object)r).collect(Collectors.toList());
    }

    /**
     * Returns the searchTerm with configured Delimiter.
     * @param searchTerm the searchterm to add delimiters.
     * @return searchTermWithDelimiter
     */
    public String getSearchTermWithDelimiter(String searchTerm) {
       return OPACConfig.getSearchTermWithDelimiter(searchTerm, this.selectedCatalog);
    }

    /**
     * Get list of hits from last search result.
     *
     * @return list of hits
     */
    public List<SingleHit> getHits() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getHits();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Getter for selectedCatalog.
     *
     * @return value of selectedCatalog
     */
    public String getSelectedCatalog() {
        return selectedCatalog;
    }

    /**
     * Setter for selectedCatalog.
     *
     * @param catalog as java.lang.String
     */
    public void setSelectedCatalog(String catalog) {
        this.selectedCatalog = catalog;
    }

    /**
     * Get searchTerm.
     *
     * @return value of searchTerm
     */
    public String getSearchTerm() {
        return this.searchTerm;
    }

    /**
     * Set searchTerm.
     *
     * @param searchTerm as java.lang.String
     */
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    /**
     * Get selectedField.
     *
     * @return value of selectedField
     */
    public String getSelectedField() {
        return this.selectedField;
    }

    /**
     * Set selectedField.
     *
     * @param field as java.lang.String
     */
    public void setSelectedField(String field) {
        this.selectedField = field;
    }
}
