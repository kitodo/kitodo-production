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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.api.schemaconverter.ExemplarRecord;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.ParameterNotFoundException;
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
    private static final int TITLE_RECORD_LINK_TAB_INDEX = 4;
    private static final String ID_PARAMETER_NAME = "ID";
    private static final String FORM_CLIENTID = "editForm";
    private static final String HITSTABLE_NAME = "hitlistDialogForm:hitlistDialogTable";
    private static final String INSERTION_TREE = "editForm:processFromTemplateTabView:insertionTree";
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

        // if fewer processes are imported than configured in the frontend, it can mean that
        // - the OPAC does not have as many processes in the hierarchy or
        // - one process of the hierarchy was already in the DB
        int numberOfProcesses = this.createProcessForm.getProcesses().size();

        if (numberOfProcesses < 1) {
            Helper.setErrorMessage("Error: list of processes is empty!");
            return;
        }

        if (numberOfProcesses < this.importDepth) {
            // check, if parent of last process in list is in DB
            TempProcess parentTempProcess = ServiceManager.getImportService().getParentTempProcess();
            if (Objects.nonNull(parentTempProcess)) {
                Process parentProcess = parentTempProcess.getProcess();
                // case 1: only one process was imported => load parent into "TitleRecordLinkTab"
                if (numberOfProcesses == 1) {
                    this.createProcessForm.setEditActiveTabIndex(TITLE_RECORD_LINK_TAB_INDEX);
                    ArrayList<SelectItem> parentCandidates = new ArrayList<>();
                    parentCandidates.add(new SelectItem(parentProcess.getId().toString(), parentProcess.getTitle()));
                    this.createProcessForm.getTitleRecordLinkTab().setPossibleParentProcesses(parentCandidates);
                    this.createProcessForm.getTitleRecordLinkTab().setChosenParentProcess((String)parentCandidates.get(0).getValue());
                    this.createProcessForm.getTitleRecordLinkTab().chooseParentProcess();
                    Ajax.update(INSERTION_TREE);
                }
                // case 2: more than one process was imported => add parent to list
                else {
                    this.createProcessForm.getProcesses().add(parentTempProcess);
                    this.createProcessForm.setEditActiveTabIndex(ADDITIONAL_FIELDS_TAB_INDEX);
                }
            } else {
                this.createProcessForm.setEditActiveTabIndex(ADDITIONAL_FIELDS_TAB_INDEX);
            }
        } else {
            this.createProcessForm.setEditActiveTabIndex(ADDITIONAL_FIELDS_TAB_INDEX);
        }

        // if more than one exemplar record was found, display a selection dialog to the user
        if (ServiceManager.getImportService().getExemplarRecords().size() > 0) {
            PrimeFaces.current().executeScript("PF('exemplarRecordsDialog').show();");
        }
    }

    private void getRecordById(String recordId) {
        try {
            LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(recordId,
                    this.hitModel.getSelectedCatalog(), this.createProcessForm.getProject().getId(),
                    this.createProcessForm.getTemplate().getId(), this.importDepth,
                    this.createProcessForm.getRuleset().getFunctionalKeys(FunctionalMetadata.HIGHERLEVEL_IDENTIFIER));
            this.createProcessForm.setProcesses(processes);

            // Fill metadata fields in metadata tab on successful import
            if (!processes.isEmpty() && processes.getFirst().getMetadataNodes().getLength() > 0) {
                TempProcess firstProcess = processes.getFirst();
                this.createProcessForm.getProcessDataTab().setDocType(firstProcess.getWorkpiece().getRootElement().getType());
                Collection<Metadata> metadata = ImportService.importMetadata(firstProcess.getMetadataNodes(),
                    MdSec.DMD_SEC);
                createProcessForm.getProcessMetadataTab().getProcessDetails().setMetadata(metadata);
            }
            String summary = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulSummary");
            String detail = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulDetail",
                    Arrays.asList(String.valueOf(processes.size()), this.hitModel.getSelectedCatalog()));
            showGrowlMessage(summary, detail);
        } catch (IOException | ProcessGenerationException | XPathExpressionException | URISyntaxException
                | ParserConfigurationException | UnsupportedFormatException | SAXException | NoRecordFoundException
                | DAOException | ConfigException e) {
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

    public void showGrowlMessage(String summary, String detail) {
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

    /**
     * Get exemplarRecords.
     *
     * @return value of exemplarRecords
     */
    public LinkedList<ExemplarRecord> getExemplarRecords() {
        return ServiceManager.getImportService().getExemplarRecords();
    }

    /**
     * Set selectedExemplarRecord.
     *
     * @param selectedExemplarRecord as org.kitodo.api.schemaconverter.ExemplarRecord
     */
    public void setSelectedExemplarRecord(ExemplarRecord selectedExemplarRecord) {
        try {
            ImportService.setSelectedExemplarRecord(selectedExemplarRecord, this.hitModel.getSelectedCatalog(),
                    this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements());
            String summary = Helper.getTranslation("newProcess.catalogueSearch.exemplarRecordSelectedSummary");
            String detail = Helper.getTranslation("newProcess.catalogueSearch.exemplarRecordSelectedDetail",
                    Arrays.asList(selectedExemplarRecord.getOwner(), selectedExemplarRecord.getSignature()));
            showGrowlMessage(summary, detail);
            Ajax.update(FORM_CLIENTID);
        } catch (ParameterNotFoundException e) {
            Helper.setErrorMessage("newProcess.catalogueSearch.exemplarRecordParameterNotFoundError",
                    new Object[] {e.getMessage(), this.hitModel.getSelectedCatalog() });
        }
    }
}
