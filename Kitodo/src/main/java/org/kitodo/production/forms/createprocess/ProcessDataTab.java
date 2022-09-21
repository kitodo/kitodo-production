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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.ProcessHelper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.process.TitleGenerator;
import org.kitodo.production.services.ServiceManager;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;

public class ProcessDataTab {

    private static final Logger logger = LogManager.getLogger(ProcessDataTab.class);

    private List<SelectItem> allDocTypes;
    protected final CreateProcessForm createProcessForm;
    private String docType;

    ProcessDataTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Set docType.
     *
     * @param docType as java.lang.String
     */
    public void setDocType(String docType) {
        if (Objects.isNull(this.docType) || this.docType.isEmpty() || !this.docType.equals(docType)) {
            if (Objects.isNull(allDocTypes) || allDocTypes.isEmpty()) {
                this.docType = "";
                Helper.setErrorMessage("errorLoadingDocTypes");
            } else if (docTypeExistsInRuleset(docType)) {
                this.docType = docType;
            } else {
                this.docType = (String) allDocTypes.get(0).getValue();
                Helper.setErrorMessage("docTypeNotFound", new Object[] {docType });
            }
        }
    }

    /**
     * Update process metadata of currently selected process.
     */
    public void updateProcessMetadata() {
        if (Objects.nonNull(docType) && Objects.nonNull(createProcessForm.getCurrentProcess()) && Objects.nonNull(
                createProcessForm.getCurrentProcess().getWorkpiece())) {
            createProcessForm.getCurrentProcess().getWorkpiece().getLogicalStructure().setType(this.docType);
            if (this.docType.isEmpty()) {
                createProcessForm.getProcessMetadata().setProcessDetails(ProcessFieldedMetadata.EMPTY);
            } else {
                createProcessForm.getProcessMetadata().initializeProcessDetails(
                        createProcessForm.getCurrentProcess().getWorkpiece().getLogicalStructure(), createProcessForm);
                overwriteProcessMetadata();
            }
        }
    }

    private void overwriteProcessMetadata() {
        TempProcess currentProcess = createProcessForm.getCurrentProcess();
        if (StringUtils.isNotBlank(currentProcess.getAtstsl())) {
            for (ProcessDetail processDetail : currentProcess.getProcessMetadata().getProcessDetailsElements()) {
                if (TitleGenerator.TSL_ATS.equals(processDetail.getMetadataID())
                        && processDetail instanceof ProcessTextMetadata) {
                    ProcessTextMetadata processTextMetadata = (ProcessTextMetadata) processDetail;
                    if (StringUtils.isBlank(processTextMetadata.getValue())) {
                        processTextMetadata.setValue(currentProcess.getAtstsl());
                    }
                }
            }
        }
    }

    private boolean docTypeExistsInRuleset(String docType) {
        for (SelectItem division : allDocTypes) {
            if (docType.equals(division.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get docType.
     *
     * @return value of docType
     */
    public String getDocType() {
        return docType;
    }

    /**
     * Get useTemplate.
     *
     * @return value of useTemplate
     */
    public boolean isUsingTemplates() {
        return ServiceManager.getImportService().isUsingTemplates();
    }

    /**
     * Set useTemplate.
     *
     * @param usingTemplates as boolean
     */
    public void setUsingTemplates(boolean usingTemplates) {
        ServiceManager.getImportService().setUsingTemplates(usingTemplates);
    }

    /**
     * Get all document types.
     *
     * @return list of all ruleset divisions
     */
    public List<SelectItem> getAllDoctypes() {
        return allDocTypes;
    }

    /**
     * Set allDocTypes.
     *
     * @param allDocTypes as java.util.List
     */
    void setAllDocTypes(List<SelectItem> allDocTypes) {
        this.allDocTypes = allDocTypes;
        if (allDocTypes.isEmpty()) {
            setDocType("");
        } else {
            setDocType((String) allDocTypes.get(0).getValue());
        }
    }

    /**
     * Generate process titles and other details.
     */
    public void generateAtstslFields() {
        List<ProcessDetail> processDetails = this.createProcessForm.getProcessMetadata().getProcessDetailsElements();
        TempProcess currentProcess = createProcessForm.getCurrentProcess();
        Process process = currentProcess.getProcess();
        try {
            String processTitleOfDocTypeView = ProcessHelper.getTitleDefinition(
                    createProcessForm.getRulesetManagement(), docType, createProcessForm.getAcquisitionStage(),
                    createProcessForm.getPriorityList());
            if (processTitleOfDocTypeView.isEmpty()) {
                Helper.setErrorMessage("newProcess.titleGeneration.creationRuleNotFound",
                        new Object[] {getDocTypeLabel(docType), process.getRuleset().getTitle()});
            }

            LinkedList<TempProcess> parents = new LinkedList<>();
            int processesSize = createProcessForm.getProcesses().size();
            if (processesSize > 1) {
                int indexCurrent = createProcessForm.getProcesses().indexOf(currentProcess);
                parents.addAll(createProcessForm.getProcesses().subList(indexCurrent + 1, processesSize));
            }
            
            ProcessHelper.generateAtstslFields(currentProcess, processDetails, parents, docType,
                    createProcessForm.getRulesetManagement(), createProcessForm.getAcquisitionStage(),
                    createProcessForm.getPriorityList(),
                    createProcessForm.getTitleRecordLinkTab().getTitleRecordProcess(), true);

            updateProcessMetadata();
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        if (Objects.nonNull(Faces.getContext())) {
            Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid",
                "editForm:processFromTemplateTabView:processMetadata",
                "editForm:processFromTemplateTabView:processAncestors");
        }
    }

    private String getDocTypeLabel(String docType) {
        for (int i = 0; i < this.allDocTypes.size(); i++) {
            SelectItem docTypeItem = this.allDocTypes.get(0);
            if (docTypeItem.getValue().equals(docType)) {
                return docTypeItem.getLabel();
            }
        }
        return docType;
    }

    /**
     * Read project configs for display in GUI.
     */
    public void prepare() {
        try {
            ServiceManager.getImportService().prepare(createProcessForm.getProject().getTitle());
            updateProcessMetadata();
        } catch (IOException | DoctypeMissingException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }
}
