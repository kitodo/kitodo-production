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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.model.LazyHitModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.xml.sax.SAXException;

public class ImportTab implements Serializable {
    private static final Logger logger = LogManager.getLogger(ImportTab.class);
    private LazyHitModel hitModel = new LazyHitModel();

    private CreateProcessForm createProcessForm;
    private static final int ADDITIONAL_FIELDS_TAB_INDEX = 2;
    private static final String ID_PARAMETER_NAME = "ID";
    private static final String FORM_CLIENTID = "editForm";
    private static final String HITSTABLE_NAME = "hitlistDialogForm:hitlistDialogTable";
    private static final String GROWL_MESSAGE =
            "PF('notifications').renderMessage({'summary':'SUMMARY','detail':'DETAIL','severity':'SEVERITY'});";

    private int importDepth = 2;

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
            DataTable hits = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(HITSTABLE_NAME);
            hits.reset();
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
     * Get the full record with the given ID from the catalog.
     */
    public void getSelectedRecord() {
        getRecordById(Helper.getRequestParameter(ID_PARAMETER_NAME));
        Ajax.update(FORM_CLIENTID);
        this.createProcessForm.setEditActiveTabIndex(ADDITIONAL_FIELDS_TAB_INDEX);
    }

    private void getRecordById(String recordId) {
        try {
            LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(recordId,
                    this.createProcessForm, this.importDepth);
            this.createProcessForm.setProcesses(processes);

            // Fill metadata fields in metadata tab on successful import
            if (!processes.isEmpty() && processes.getFirst().getMetadataNodes().getLength() > 0) {
                TempProcess firstProcess = processes.getFirst();
                this.createProcessForm.getProcessDataTab().setDocType(firstProcess.getWorkpiece().getRootElement().getType());
                ProcessFieldedMetadata processDetails =
                        createProcessForm.getProcessMetadataTab().getProcessDetails();
                ImportService.fillProcessDetails(processDetails,
                        firstProcess.getMetadataNodes(), this.createProcessForm.getRuleset(),
                        this.createProcessForm.getProcessDataTab().getDocType(),
                        this.createProcessForm.getAcquisitionStage(),
                        this.createProcessForm.getPriorityList());
            }

            showGrowlMessage(processes, this.hitModel.getSelectedCatalog());
        } catch (IOException | ProcessGenerationException | XPathExpressionException | URISyntaxException
                | ParserConfigurationException | UnsupportedFormatException | SAXException | NoRecordFoundException e) {
            Helper.setErrorMessage(e);
        }
    }

    /**
     * Get LazyHitModel.
     *
     * @return LazyHitModel of this ImportTab
     */
    public LazyHitModel getHitModel() {
        return this.hitModel;
    }

    private void showGrowlMessage(LinkedList<TempProcess> processes, String opac) {
        String summary = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulSummary");
        String detail = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulDetail",
                Arrays.asList(String.valueOf(processes.size()), opac));
        String script = GROWL_MESSAGE.replace("SUMMARY", summary).replace("DETAIL", detail)
                .replace("SEVERITY", "info");
        PrimeFaces.current().executeScript(script);
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
