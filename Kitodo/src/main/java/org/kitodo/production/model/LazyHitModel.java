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
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyHitModel extends LazyDataModel<Object> {

    private ImportConfiguration importConfiguration;
    private String selectedField = "";
    private String searchTerm = "";
    private int importDepth = 2;

    private SearchResult searchResult = null;

    /**
     * Empty default constructor. Sets default catalog and search field, if configured.
     */
    public LazyHitModel() {
    }

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
                this.selectedField, this.searchTerm, this.importConfiguration, first, resultSize);

        if (Objects.isNull(searchResult) || Objects.isNull(searchResult.getHits())) {
            return Collections.emptyList();
        }
        return searchResult.getHits().stream().map(r -> (Object)r).collect(Collectors.toList());
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
     * Getter for importConfiguration.
     *
     * @return value of importConfiguration
     */
    public ImportConfiguration getImportConfiguration() {
        return importConfiguration;
    }

    /**
     * Setter for importConfiguration. This also sets the catalogs default search field, if configured.
     *
     * @param importConfiguration ImportConfiguration
     */
    public void setImportConfiguration(ImportConfiguration importConfiguration) {
        this.importConfiguration = importConfiguration;
        if (Objects.nonNull(importConfiguration)) {
            this.setSelectedField(ImportService.getDefaultSearchField(importConfiguration));
            this.setImportDepth(ServiceManager.getImportService().getDefaultImportDepth(importConfiguration));
        }
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

    /**
     * Get import depth.
     *
     * @return import depth
     */
    public int getImportDepth() {
        return importDepth;
    }

    /**
     * Set import depth.
     *
     * @param depth import depth
     */
    public void setImportDepth(int depth) {
        importDepth = depth;
    }
}
