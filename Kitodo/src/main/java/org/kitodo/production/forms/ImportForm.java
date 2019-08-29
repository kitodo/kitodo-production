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

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.forms.copyprocess.AdditionalDetailsTableRow;
import org.kitodo.production.forms.copyprocess.BooleanMetadataTableRow;
import org.kitodo.production.forms.copyprocess.FieldedAdditionalDetailsTableRow;
import org.kitodo.production.forms.copyprocess.ProzesskopieForm;
import org.kitodo.production.forms.copyprocess.SelectMetadataTableRow;
import org.kitodo.production.forms.copyprocess.TextMetadataTableRow;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Named("ImportForm")
@ViewScoped
public class ImportForm implements Serializable {
    private static final Logger logger = LogManager.getLogger(ImportForm.class);

    private ProzesskopieForm prozesskopieForm;
    private String selectedCatalog;
    private String selectedField;
    private String searchTerm;
    private SearchResult searchResult;
    private List<String> filledMetadataGroups = new ArrayList<>();
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
        this.prozesskopieForm.setEditActiveTabIndex(2);
    }

    private void getRecordById(String catalog, String recordId) {
        Document record;
        try {
            record = ServiceManager.getImportService().getSelectedRecord(this.selectedCatalog, recordId);
        } catch (IOException | SAXException | ParserConfigurationException | URISyntaxException
                | NoRecordFoundException | UnsupportedFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        List<AdditionalDetailsTableRow> additionalDetailsTableRows =
                this.prozesskopieForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows();
        Element root = record.getDocumentElement();
        NodeList kitodoNodes = root.getElementsByTagNameNS(KITODO_NAMESPACE, "kitodo");

        // TODO: iterating over multiple kitodo nodes will overwrite existing
        for (int i = 0; i < kitodoNodes.getLength(); i++) {
            Node kitodoNode = kitodoNodes.item(i);
            setAdditionalDetailsTable(additionalDetailsTableRows, kitodoNode.getChildNodes());
        }
        this.prozesskopieForm.setOpacKatalog(catalog);
    }

    private void setAdditionalDetailsTable(List<AdditionalDetailsTableRow> rows, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Element element = (Element) node;
            String nodeName = element.getAttribute("name");
            for (AdditionalDetailsTableRow tableRow : rows) {
                if (Objects.nonNull(tableRow.getMetadataID()) && tableRow.getMetadataID().equals(nodeName)) {
                    if (node.getLocalName().equals("metadataGroup")
                            && tableRow instanceof FieldedAdditionalDetailsTableRow) {
                        FieldedAdditionalDetailsTableRow fieldedRow;
                        if (filledMetadataGroups.contains(nodeName)) {
                            try {
                                fieldedRow = this.prozesskopieForm.getAdditionalDetailsTab().addMetadataGroupRow(nodeName);
                                this.prozesskopieForm.getAdditionalDetailsTab().getAdditionalDetailsTable().getRows().add(fieldedRow);
                                setAdditionalDetailsTable(fieldedRow.getRows(), element.getChildNodes());
                            } catch (NoSuchMetadataFieldException e) {
                                logger.error(e.getLocalizedMessage());
                            }
                        } else {
                            fieldedRow = (FieldedAdditionalDetailsTableRow) tableRow;
                            filledMetadataGroups.add(nodeName);
                            setAdditionalDetailsTable(fieldedRow.getRows(), element.getChildNodes());
                        }
                    } else if (node.getLocalName().equals("metadata")) {
                        setAdditionalDetailsRow(tableRow, element);
                    }
                    break;
                }
            }
        }
    }

    private void setAdditionalDetailsRow(AdditionalDetailsTableRow row, Element element) {
        if (row instanceof TextMetadataTableRow) {
            ((TextMetadataTableRow) row).setValue(element.getTextContent());

        } else if (row instanceof BooleanMetadataTableRow) {
            ((BooleanMetadataTableRow) row).setActive(Boolean.valueOf(element.getTextContent()));

        } else if (row instanceof SelectMetadataTableRow) {
            ((SelectMetadataTableRow) row).setSelectedItem(element.getTextContent());
        }
    }
}
