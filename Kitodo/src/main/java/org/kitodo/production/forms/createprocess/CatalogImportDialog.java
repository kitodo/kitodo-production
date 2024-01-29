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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.api.schemaconverter.ExemplarRecord;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.CatalogException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
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
    private boolean importChildren = false;
    private int numberOfChildren = 0;
    private String opacErrorMessage = "";
    private boolean additionalImport = false;

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
        getRecordById(Helper.getRequestParameter(ID_PARAMETER_NAME));
    }

    /**
     * Get list of search fields.
     *
     * @return list of search fields
     */
    public List<String> getSearchFields() {
        if (Objects.isNull(hitModel.getImportConfiguration())) {
            return new LinkedList<>();
        } else {
            try {
                return ServiceManager.getImportService().getAvailableSearchFields(hitModel.getImportConfiguration());
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
            if (skipHitList(hitModel.getImportConfiguration(), hitModel.getSelectedField())) {
                getRecordById(hitModel.getSearchTerm());
            } else {
                List<?> hits = hitModel.load(0, 10, null, SortOrder.ASCENDING, Collections.EMPTY_MAP);
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
        } catch (CatalogException e) {
            this.opacErrorMessage = e.getMessage();
            PrimeFaces.current().ajax().update("opacErrorDialog");
            PrimeFaces.current().executeScript("PF('opacErrorDialog').show();");
        }
    }

    private boolean skipHitList(ImportConfiguration importConfiguration, String searchField) {
        return ImportService.skipHitlist(importConfiguration, searchField);
    }

    /**
     * Get retrieved hits. Returns empty list if LazyHitModel instance is null.
     *
     * @return hits
     */
    public List<SingleHit> getHits() {
        return this.hitModel.getHits();
    }

    void showExemplarRecord() {
        Ajax.update(FORM_CLIENTID);
        // if more than one exemplar record was found, display a selection dialog to the user
        LinkedList<ExemplarRecord> exemplarRecords = ServiceManager.getImportService().getExemplarRecords();
        if (exemplarRecords.size() == 1) {
            this.setSelectedExemplarRecord(exemplarRecords.get(0));
        } else if (exemplarRecords.size() > 1) {
            PrimeFaces.current().executeScript("PF('exemplarRecordsDialog').show();");
        }
    }

    @Override
    public List<ImportConfiguration> getImportConfigurations() {
        if (Objects.isNull(importConfigurations)) {
            try {
                importConfigurations = ServiceManager.getImportConfigurationService().getAllOpacSearchConfigurations();
            } catch (IllegalArgumentException | DAOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                importConfigurations = new LinkedList<>();
            }
        }
        return importConfigurations;
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
                createProcessForm.setChildProcesses(new LinkedList<>());
                int projectId = this.createProcessForm.getProject().getId();
                int templateId = this.createProcessForm.getTemplate().getId();
                ImportConfiguration importConfiguration = this.hitModel.getImportConfiguration();

                // import current and ancestors
                LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(
                        currentRecordId, importConfiguration, projectId, templateId, hitModel.getImportDepth(),
                        createProcessForm.getRulesetManagement().getFunctionalKeys(
                                FunctionalMetadata.HIGHERLEVEL_IDENTIFIER));
                // import children
                if (this.importChildren) {
                    importChildren(projectId, templateId, importConfiguration, processes);
                }

                if (!createProcessForm.getProcesses().isEmpty() && additionalImport) {
                    extendsMetadataTableOfMetadataTab(processes);
                } else {
                    createProcessForm.setProcesses(processes);
                    TempProcess currentTempProcess = processes.getFirst();
                    attachToExistingParentAndGenerateAtstslIfNotExist(currentTempProcess);
                    createProcessForm.fillCreateProcessForm(currentTempProcess);
                    showMessageAndRecord(importConfiguration, processes);
                }
            } catch (IOException | ProcessGenerationException | XPathExpressionException | URISyntaxException
                    | ParserConfigurationException | UnsupportedFormatException | SAXException | DAOException
                    | ConfigException | TransformerException | NoRecordFoundException | InvalidMetadataValueException
                    | NoSuchMetadataFieldException e) {
                throw new CatalogException(e.getLocalizedMessage());
            }
        }
    }

    private void showMessageAndRecord(ImportConfiguration importConfiguration, LinkedList<TempProcess> processes) {
        String summary = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulSummary");
        String detail = Helper.getTranslation("newProcess.catalogueSearch.importSuccessfulDetail",
                String.valueOf(processes.size()), importConfiguration.getTitle());
        showGrowlMessage(summary, detail);

        if (this.importChildren) {
            summary = Helper.getTranslation("newProcess.catalogueSearch.loadingChildrenSuccessfulSummary");
            detail = Helper.getTranslation("newProcess.catalogueSearch.loadingChildrenSuccessfulDetail",
                    String.valueOf(this.createProcessForm.getChildProcesses().size()));
            showGrowlMessage(summary, detail);
        }
        showExemplarRecord();
    }

    private void importChildren(int projectId, int templateId, ImportConfiguration importConfiguration, List<TempProcess> parentProcesses)
            throws SAXException, UnsupportedFormatException, URISyntaxException, ParserConfigurationException,
            IOException, ProcessGenerationException, TransformerException, InvalidMetadataValueException,
            NoSuchMetadataFieldException {
        try {
            this.createProcessForm.setChildProcesses(ServiceManager.getImportService().getChildProcesses(
                    importConfiguration, this.currentRecordId, projectId, templateId, numberOfChildren, parentProcesses));
        } catch (NoRecordFoundException e) {
            this.createProcessForm.setChildProcesses(new LinkedList<>());
            showGrowlMessage("Import error", e.getLocalizedMessage());
        }
    }

    private void getRecordById(String recordId) {
        this.currentRecordId = recordId;
        try {
            if (this.importChildren) {
                this.numberOfChildren = ServiceManager.getImportService().getNumberOfChildren(
                        this.hitModel.getImportConfiguration(), this.currentRecordId);
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
            ImportService.setSelectedExemplarRecord(selectedExemplarRecord, this.hitModel.getImportConfiguration(),
                    this.createProcessForm.getProcessMetadata().getProcessDetailsElements());
            String summary = Helper.getTranslation("newProcess.catalogueSearch.exemplarRecordSelectedSummary");
            String detail = Helper.getTranslation("newProcess.catalogueSearch.exemplarRecordSelectedDetail",
                selectedExemplarRecord.getOwner(), selectedExemplarRecord.getSignature());
            showGrowlMessage(summary, detail);
            Ajax.update(FORM_CLIENTID);
        } catch (ParameterNotFoundException e) {
            Helper.setErrorMessage("newProcess.catalogueSearch.exemplarRecordParameterNotFoundError",
                    new Object[] {e.getMessage(), this.hitModel.getImportConfiguration().getTitle() });
        }
    }

    /**
     * Check and return whether the "parentIdSearchField" is configured in the current ImportConfiguration.
     *
     * @return whether "parentIdSearchField" is configured for current ImportConfiguration
     */
    public boolean isParentIdSearchFieldConfigured() {
        try {
            return Objects.nonNull(this.hitModel.getImportConfiguration()) && ServiceManager.getImportService()
                    .isParentIdSearchFieldConfigured(this.hitModel.getImportConfiguration());
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
            String.valueOf(this.numberOfChildren));
    }


    /**
     * Checks the additional import.
     *
     * @return true if is additional import
     */
    public boolean isAdditionalImport() {
        return additionalImport;
    }

    /**
     * Set additional import.
     *
     * @param additionalImport
     *            the value if is additional import
     */
    public void setAdditionalImport(boolean additionalImport) {
        this.additionalImport = additionalImport;
    }

}
