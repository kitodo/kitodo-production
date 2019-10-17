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

import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyHitModel;
import org.kitodo.production.services.ServiceManager;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.SortOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportTab implements Serializable {
    private static final Logger logger = LogManager.getLogger(ImportTab.class);
    private LazyHitModel hitModel = new LazyHitModel();

    private CreateProcessForm createProcessForm;
    private static final String KITODO_NAMESPACE = "http://meta.kitodo.org/v1/";
    private static final int ADDITIONAL_FIELDS_TAB_INDEX = 2;
    private static final String ID_PARAMETER_NAME = "ID";
    private static final String FORM_CLIENTID = "editForm";
    private static final String KITODO_STRING = "kitodo";
    private static final String HITSTABLE_NAME = "hitlistDialogForm:hitlistDialogTable";
    private DataTable hitsTable;

    /**
     * Standard constructor.
     *
     * @param createProcessForm CreateProcessForm instance to which this ImportTab is assigned.
     */
    ImportTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
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
        if (this.hitModel.getSelectedCatalog().isEmpty()) {
            return new LinkedList<>();
        } else {
            try {
                return ServiceManager.getImportService().getAvailableSearchFields(this.hitModel.getSelectedCatalog());
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
            int pageSize = ServiceManager.getUserService().getAuthenticatedUser().getTableSize();
            this.hitsTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(HITSTABLE_NAME);
            this.hitModel.load(
                    (this.hitsTable.getPage() + 1) * pageSize,
                    pageSize,
                    "",
                    SortOrder.ASCENDING,
                    null);
            PrimeFaces.current().executeScript("PF('hitlistDialog').show()");
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Get retrieved hits. Returns empty list if LazyHitModel instance is null.
     *
     * @return hits
     */
    public List<SingleHit> getHits() {
        if (Objects.nonNull(this.hitModel)) {
            return this.hitModel.getHits();
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
        if (Objects.nonNull(this.hitModel)) {
            return this.hitModel.getRowCount();
        } else {
            return 0;
        }
    }

    /**
     * Get the full record with the given ID from the catalog.
     */
    public void getSelectedRecord() {
        String recordId = Helper.getRequestParameter(ID_PARAMETER_NAME);
        getRecordById(this.hitModel.getSelectedCatalog(), recordId);
        Ajax.update(FORM_CLIENTID);
        this.createProcessForm.setEditActiveTabIndex(ADDITIONAL_FIELDS_TAB_INDEX);
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

        List<ProcessDetail> processDetailsList =
                this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements();
        Element root = record.getDocumentElement();
        NodeList kitodoNodes = root.getElementsByTagNameNS(KITODO_NAMESPACE, KITODO_STRING);
        for (int i = 0; i < kitodoNodes.getLength(); i++) {
            Node kitodoNode = kitodoNodes.item(i);
            this.createProcessForm.getProcessMetadataTab().fillProcessDetailsElements(processDetailsList,
                    kitodoNode.getChildNodes(), false);
        }
    }

    public LazyHitModel getHitModel() {
        return this.hitModel;
    }

    public void printPageNumber() {
        System.out.println("Current page number: " + this.hitsTable.getPage());
    }
}
