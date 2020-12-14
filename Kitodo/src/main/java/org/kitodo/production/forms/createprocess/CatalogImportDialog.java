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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.api.schemaconverter.ExemplarRecord;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.CatalogException;
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
import org.primefaces.model.SortOrder;
import org.xml.sax.SAXException;

public class CatalogImportDialog  extends MetadataImportDialog implements Serializable {
    private static final Logger logger = LogManager.getLogger(CatalogImportDialog.class);
    private final LazyHitModel hitModel = new LazyHitModel();

    private static final String ID_PARAMETER_NAME = "ID";
    private static final String HITSTABLE_NAME = "hitlistDialogForm:hitlistDialogTable";
    private static final int NUMBER_OF_CHILDREN_WARNING_THRESHOLD = 5;

    private String currentRecordId = "";
    private int importDepth = 1;
    private boolean importChildren = false;
    private int numberOfChildren = 0;
    private String opacErrorMessage = "";

    /**
     * Standard constructor.
     *
     * @param createProcessForm CreateProcessForm instance to which this ImportTab is assigned.
     */
    CatalogImportDialog(CreateProcessForm createProcessForm) {
        super(createProcessForm);
    }

    /**
     * Get the full record with the given ID from the catalog.
     */
    public void getSelectedRecord() {
        this.createProcessForm.setChildProcesses(new LinkedList<>());
        this.createProcessForm.setProcesses(new LinkedList<>());
        getRecordById(Helper.getRequestParameter(ID_PARAMETER_NAME));
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
        List<?> hits;
        try {
            hits = hitModel.load(0, 10, null, SortOrder.ASCENDING, Collections.EMPTY_MAP);
        } catch (CatalogException e) {
            this.opacErrorMessage = e.getMessage();
            PrimeFaces.current().ajax().update("opacErrorDialog");
            PrimeFaces.current().executeScript("PF('opacErrorDialog').show();");
            return;
        }
        if (hits.size() == 1) {
            getRecordById(((SingleHit) hits.get(0)).getIdentifier());
        } else {
            try {
                ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(HITSTABLE_NAME)).reset();
                PrimeFaces.current().executeScript("PF('hitlistDialog').show()");
            } catch (IllegalArgumentException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
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

    @Override
    void showRecord() {
        super.showRecord();
        // if more than one exemplar record was found, display a selection dialog to the user
        LinkedList<ExemplarRecord> exemplarRecords = ServiceManager.getImportService().getExemplarRecords();
        if (exemplarRecords.size() == 1) {
            this.setSelectedExemplarRecord(exemplarRecords.get(0));
        } else if (exemplarRecords.size() > 1) {
            PrimeFaces.current().executeScript("PF('exemplarRecordsDialog').show();");
        }
    }

    /**
     * Retrieve complete record hierarchy of record from currently selected catalog, including
     * potential child records and ancestor records.
     */
    public void getRecordHierarchy() {
        if (StringUtils.isBlank(this.currentRecordId)) {
            Helper.setErrorMessage("No record selected!");
        } else {
            try {
                int projectId = this.createProcessForm.getProject().getId();
                int templateId = this.createProcessForm.getTemplate().getId();
                String opac = this.hitModel.getSelectedCatalog();
                // import children
                if (this.importChildren) {
                    this.createProcessForm.setChildProcesses(ServiceManager.getImportService().getChildProcesses(
                            opac, this.currentRecordId, projectId, templateId, numberOfChildren));
                }
                // import ancestors
                LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(
                        this.currentRecordId, opac, projectId, templateId, this.importDepth,
                        this.createProcessForm.getRulesetManagement().getFunctionalKeys(
                                FunctionalMetadata.HIGHERLEVEL_IDENTIFIER));
                this.createProcessForm.setProcesses(processes);

                // Fill metadata fields in metadata tab with metadata values of first process on successful import
                if (!processes.isEmpty() && processes.getFirst().getMetadataNodes().getLength() > 0) {
                    TempProcess firstProcess = processes.getFirst();
                    this.createProcessForm.getProcessDataTab().setDocType(
                            firstProcess.getWorkpiece().getRootElement().getType());
                    Collection<Metadata> metadata = ImportService.importMetadata(firstProcess.getMetadataNodes(),
                            MdSec.DMD_SEC);
                    createProcessForm.getProcessMetadataTab().getProcessDetails().setMetadata(metadata);
                }

                String summary = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulSummary");
                String detail = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulDetail",
                        Arrays.asList(String.valueOf(processes.size()), opac));
                showGrowlMessage(summary, detail);

                if (this.importChildren) {
                    summary = Helper.getTranslation("newProcess.catalogueSearch.loadingChilrenSuccessfulSummary");
                    detail = Helper.getTranslation("newProcess.catalogueSearch.loadingChilrenSuccessfulDetail",
                            Collections.singletonList(String.valueOf(this.createProcessForm.getChildProcesses().size())));
                    showGrowlMessage(summary, detail);
                }

                showRecord();
            } catch (IOException | ProcessGenerationException | XPathExpressionException | URISyntaxException
                    | ParserConfigurationException | UnsupportedFormatException | SAXException | NoRecordFoundException
                    | DAOException | ConfigException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
    }

    private void getRecordById(String recordId) {
        this.currentRecordId = recordId;
        try {
            if (this.importChildren) {
                this.numberOfChildren = ServiceManager.getImportService().getNumberOfChildren(
                        this.hitModel.getSelectedCatalog(), this.currentRecordId);
            }
            if (this.importChildren && this.numberOfChildren > NUMBER_OF_CHILDREN_WARNING_THRESHOLD) {
                Ajax.update("manyChildrenWarningDialog");
                PrimeFaces.current().executeScript("PF('manyChildrenWarningDialog').show();");
            } else {
                getRecordHierarchy();
            }
        } catch (ConfigException e) {
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
     * Get import children.
     *
     * @return import children
     */
    public boolean isImportChildren() {
        return importChildren;
    }

    /**
     * Set import children.
     *
     * @param childImport import children
     */
    public void setImportChildren(boolean childImport) {
        this.importChildren = childImport;
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

    /**
     * Check and return whether the "parentElement" is configured in kitodo_opac.xml for the currently selected OPAC.
     *
     * @return whether "parentElement" is configured for current OPAC
     */
    public boolean isParentElementConfigured() {
        try {
            return ServiceManager.getImportService().isParentElementConfigured(this.hitModel.getSelectedCatalog());
        } catch (ConfigException e) {
            return false;
        }
    }

    /**
     * Get OPAC error message.
     *
     * @return OPAC error message
     */
    public String getOpacErrorMessage() {
        return this.opacErrorMessage;
    }

    /**
     * Return String containing warning about high number of child processes that are going to be imported.
     *
     * @return warning about high number of child processes to be imported
     */
    public String getNumberOfChildProcessesWarning() {
        return Helper.getTranslation("newProcess.catalogueSearch.manyChildrenWarning",
                Collections.singletonList(String.valueOf(this.numberOfChildren)));
    }
}
