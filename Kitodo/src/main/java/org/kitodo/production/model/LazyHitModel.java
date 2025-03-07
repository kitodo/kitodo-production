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
import org.kitodo.production.forms.createprocess.CatalogImportDialog;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class LazyHitModel extends LazyDataModel<Object> {

    private final CatalogImportDialog catalogImportDialog;
    private SearchResult searchResult = null;

    /**
     * Constructor setting this LazyHitModels 'CatalogImportDialog'.
     *
     * @param dialog as CatalogImportDialog
     */
    public LazyHitModel(CatalogImportDialog dialog) {
        this.catalogImportDialog = dialog;
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
                catalogImportDialog.getSelectedField(), catalogImportDialog.getSearchTerm(),
                catalogImportDialog.createProcessForm.getCurrentImportConfiguration(), first, resultSize);

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
}
