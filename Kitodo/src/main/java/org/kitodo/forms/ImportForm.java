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

package org.kitodo.forms;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.api.externaldatamanagement.Record;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.helper.Helper;
import org.kitodo.services.ServiceManager;
import org.primefaces.PrimeFaces;

@Named("ImportForm")
@ViewScoped
public class ImportForm implements Serializable {

    private String selectedCatalog;
    private String selectedField;
    private String searchTerm;
    private transient ServiceManager serviceManager = new ServiceManager();
    private SearchResult searchResult;

    public String getSelectedCatalog() {
        return selectedCatalog;
    }

    public void setSelectedCatalog(String catalog) {
        this.selectedCatalog = catalog;
    }

    public String getSearchTerm() {
        return this.searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getSelectedField() {
        return this.selectedField;
    }

    public void setSelectedField(String field) {
        this.selectedField = field;
    }

    /**
     * Get list of catalogs.
     * @return list of catalogs
     */
    public List<String> getCatalogs() {
        try {
            return serviceManager.getImportService().getAvailableCatalogs();
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Get list of search fields.
     * @return list of search fields
     */
    public LinkedList<String> getSearchFields() {
        try {
            return serviceManager.getImportService().getAvailableSearchFields(this.selectedCatalog);
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Call search method of ImportService.
     */
    public void search() throws IllegalArgumentException {
        try {
            this.searchResult = serviceManager.getImportService().performSearch(
                    this.selectedField, this.searchTerm, this.selectedCatalog);
            PrimeFaces.current().executeScript("PF('hitlist').show()");
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }

    /**
     * Get retrieved hits. Returns empty list if searchResult instance is null.
     * @return hits
     */
    public List<Record> getHits() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getHits();
        } else {
            return new LinkedList<>();
        }

    }

    /**
     * Get total number of hits for performed query. Returns 0 if searchResult instance is null.
     * @return total number of hits
     */
    public int getNumberOfHits() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getNumberOfRecords();
        } else {
            return 0;
        }

    }
}
