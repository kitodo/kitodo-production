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

package org.kitodo.production.forms.createprocess;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.forms.CreateProcessForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportTab implements Serializable {
    private static final Logger logger = LogManager.getLogger(ImportTab.class);

    private CreateProcessForm createProcessForm;
    private String selectedCatalog;
    private String selectedField;
    private String searchTerm;
    private SearchResult searchResult;
    private static final String KITODO_NAMESPACE = "http://meta.kitodo.org/v1/";

    /**
     * Standard constructor.
     *
     * @param createProcessForm CreateProcessForm instance to which this ImportTab is assigned.
     */
    public ImportTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
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
     * @param catalog
     *            as java.lang.String
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
     * @param searchTerm
     *            as java.lang.String
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
     * @param field
     *            as java.lang.String
     */
    public void setSelectedField(String field) {
        this.selectedField = field;
    }


    /**
     * Get list of catalogs.
     *
     * @return list of catalogs
     */
    public List<String> getCatalogs() {
        try {
            return ServiceManager.getImportService().getAvailableCatalogs();
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Get list of search fields.
     *
     * @return list of search fields
     */
    public List<String> getSearchFields() {
        if (this.selectedCatalog.isEmpty()) {
            return new LinkedList<>();
        } else {
            try {
                return ServiceManager.getImportService().getAvailableSearchFields(this.selectedCatalog);
            } catch (IllegalArgumentException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                return new LinkedList<>();
            }
        }
    }

    /**
     * Call search method of ImportService.
     */
    public void search() {
        try {
            this.searchResult = ServiceManager.getImportService().performSearch(this.selectedField, this.searchTerm,
                this.selectedCatalog);
            PrimeFaces.current().executeScript("PF('hitlist').show()");
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Get retrieved hits. Returns empty list if searchResult instance is null.
     *
     * @return hits
     */
    public List<SingleHit> getHits() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getHits();
        } else {
            return new LinkedList<>();
        }

    }

    /**
     * Get total number of hits for performed query. Returns 0 if searchResult
     * instance is null.
     *
     * @return total number of hits
     */
    public int getNumberOfHits() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getNumberOfHits();
        } else {
            return 0;
        }

    }

    /**
     * Get the full record with the given ID from the catalog.
     */
    public void getSelectedRecord() {
        String recordId = Helper.getRequestParameter("ID");
        getRecordById(this.selectedCatalog, recordId);
        Ajax.update("editForm");
        this.createProcessForm.setEditActiveTabIndex(2);
    }

    private void getRecordById(String catalog, String recordId) {
        Document record;
        try {
            record = ServiceManager.getImportService().getSelectedRecord(catalog, recordId);
        } catch (IOException | SAXException | ParserConfigurationException | URISyntaxException
                | NoRecordFoundException | UnsupportedFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        List<AdditionalDetailsTableRow> additionalDetailsTableRows =
                this.createProcessForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows();
        Element root = record.getDocumentElement();
        NodeList kitodoNodes = root.getElementsByTagNameNS(KITODO_NAMESPACE, "kitodo");
        for (int i = 0; i < kitodoNodes.getLength(); i++) {
            Node kitodoNode = kitodoNodes.item(i);
            this.createProcessForm.getAdditionalDetailsTab().setAdditionalDetailsTable(additionalDetailsTableRows,
                    kitodoNode.getChildNodes(), false);
        }
    }
}
