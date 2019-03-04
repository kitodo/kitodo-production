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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.api.externaldatamanagement.Record;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.production.helper.AdditionalField;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Named("ImportForm")
@ViewScoped
public class ImportForm implements Serializable {
    private ProzesskopieForm prozesskopieForm;

    private String selectedCatalog;
    private String selectedField;
    private String searchTerm;
    private SearchResult searchResult;
    private static final String KITODO_NAMESPACE = "http://meta.kitodo.org/v1/";

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
     * Get prozesskopieForm.
     *
     * @return value of prozesskopieForm
     */
    public ProzesskopieForm getProzesskopieForm() {
        return prozesskopieForm;
    }

    /**
     * Set prozesskopieForm.
     *
     * @param prozesskopieForm
     *            as org.kitodo.forms.ProzesskopieForm
     */
    public void setProzesskopieForm(ProzesskopieForm prozesskopieForm) {
        this.prozesskopieForm = prozesskopieForm;
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
            Helper.setErrorMessage(e.getLocalizedMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Get list of search fields.
     *
     * @return list of search fields
     */
    public List<String> getSearchFields() {
        try {
            return ServiceManager.getImportService().getAvailableSearchFields(this.selectedCatalog);
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
            return new LinkedList<>();
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
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
    }

    /**
     * Get retrieved hits. Returns empty list if searchResult instance is null.
     *
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
     * Get total number of hits for performed query. Returns 0 if searchResult
     * instance is null.
     *
     * @return total number of hits
     */
    public int getNumberOfHits() {
        if (Objects.nonNull(this.searchResult)) {
            return this.searchResult.getNumberOfRecords();
        } else {
            return 0;
        }

    }

    /**
     * Get the full record with the given ID from the catalog.
     */
    public void getSelectedRecord() {
        String recordId = Helper.getRequestParameter("ID");
        Document record = ServiceManager.getImportService().getSelectedRecord(this.selectedCatalog, recordId);

        List<AdditionalField> actualFields = this.prozesskopieForm.getAdditionalFields();
        Element root = record.getDocumentElement();
        NodeList kitodoNodes = root.getElementsByTagNameNS(KITODO_NAMESPACE, "kitodo");

        // TODO: iterating over multiple kitodo nodes will overwrite existing
        // 'additionalField' values from the last kitodo node!
        for (int i = 0; i < kitodoNodes.getLength(); i++) {
            Node kitodoNode = kitodoNodes.item(i);
            actualFields = insertFieldValues(actualFields, kitodoNode.getChildNodes());
        }

        this.prozesskopieForm.setAdditionalFields(actualFields);

        Ajax.update("editForm");
        this.prozesskopieForm.setActiveTabId(2);
    }

    private List<AdditionalField> insertFieldValues(List<AdditionalField> additionalFields, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getLocalName().equals("metadataGroup")) {
                additionalFields = insertFieldValues(additionalFields, node.getChildNodes());
            } else if (node.getLocalName().equals("metadata")) {
                Element element = (Element) node;
                for (AdditionalField additionalField : additionalFields) {
                    if (Objects.nonNull(additionalField.getMetadata())
                            && additionalField.getMetadata().equals(element.getAttribute("name"))) {
                        // Append author to list of existing authors
                        if (additionalField.getMetadata().equals("ListOfCreators")) {
                            additionalField
                                    .setValue(Objects.isNull(additionalField.getValue()) ? element.getTextContent()
                                            : additionalField.getValue() + ", " + element.getTextContent());
                        } else {
                            additionalField.setValue(element.getTextContent());
                        }
                    }
                }
            }
        }
        return additionalFields;
    }
}
