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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.process.TitleGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.ProcessService;
import org.omnifaces.util.Ajax;

public class ProcessDataTab {

    private static final Logger logger = LogManager.getLogger(ProcessDataTab.class);

    private List<SelectItem> allDocTypes;
    private final CreateProcessForm createProcessForm;
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
                Helper.setErrorMessage("docTypeNotFound", new Object[]{docType});
            }
        }
    }

    /**
     * Update process metadata of currently selected process.
     */
    public void updateProcessMetadata() {
        if (Objects.nonNull(docType) && Objects.nonNull(createProcessForm.getCurrentProcess())) {
            createProcessForm.getCurrentProcess().getWorkpiece().getLogicalStructure().setType(this.docType);
            if (this.docType.isEmpty()) {
                createProcessForm.getProcessMetadata().setProcessDetails(ProcessFieldedMetadata.EMPTY);
            } else {
                createProcessForm.getProcessMetadata()
                        .initializeProcessDetails(createProcessForm.getCurrentProcess().getWorkpiece()
                                .getLogicalStructure(), createProcessForm);
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
    public void generateProcessTitleAndTiffHeader() {
        List<ProcessDetail> processDetails = this.createProcessForm.getProcessMetadata().getProcessDetailsElements();
        Process process = this.createProcessForm.getCurrentProcess().getProcess();
        try {
            StructuralElementViewInterface docTypeView = createProcessForm.getRulesetManagement().getStructuralElementView(
                    docType, createProcessForm.getAcquisitionStage(), createProcessForm.getPriorityList());
            String processTitle = docTypeView.getProcessTitle().orElse("");
            if (processTitle.isEmpty()) {
                Helper.setErrorMessage("newProcess.titleGeneration.creationRuleNotFound",
                    new Object[] {getDocTypeLabel(docType), process.getRuleset().getTitle() });
            }

            String currentTitle = TitleGenerator.getValueOfMetadataID(TitleGenerator.TITLE_DOC_MAIN, processDetails);

            if (StringUtils.isBlank(currentTitle)) {
                Process parentProcess = createProcessForm.getTitleRecordLinkTab().getTitleRecordProcess();
                if (Objects.nonNull(parentProcess)) {
                    currentTitle = getTitleFromLogicalStructure(parentProcess);
                } else {
                    currentTitle = getTitleFromAncestors();
                }
            }

            String atstsl = ProcessService.generateProcessTitleAndGetAtstsl(processDetails, processTitle, process,
                currentTitle);

            // document name is generally equal to process title
            createProcessForm.getCurrentProcess().setTiffHeaderDocumentName(process.getTitle());
            createProcessForm.getCurrentProcess().setTiffHeaderImageDescription(ProcessService.generateTiffHeader(
                    processDetails, atstsl, ServiceManager.getImportService().getTiffDefinition(), this.docType));
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid",
                "editForm:processFromTemplateTabView:processMetadata",
                "editForm:processFromTemplateTabView:processAncestors");
    }

    private String getTitleFromLogicalStructure(Process process) {
        try {
            LegacyMetsModsDigitalDocumentHelper metsModsDigitalDocumentHelper = ServiceManager.getProcessService()
                    .readMetadataFile(process);
            return getTitleFromMetadata(metsModsDigitalDocumentHelper.getWorkpiece().getLogicalStructure().getMetadata());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    private String getTitleFromAncestors() {
        int processesSize = createProcessForm.getProcesses().size();

        if (processesSize <= 1) {
            return StringUtils.EMPTY;
        }

        List<TempProcess> ancestors = createProcessForm.getProcesses().subList(1, processesSize);

        // get title of ancestors where TitleDocMain exists when several processes were
        // imported
        for (TempProcess tempProcess : ancestors) {
            ProcessFieldedMetadata processFieldedMetadata = initializeTempProcessDetails(tempProcess);
            String title = getTitleFromMetadata(processFieldedMetadata.getChildMetadata());
            if (StringUtils.isNotBlank(title)) {
                return title;
            }
        }
        return StringUtils.EMPTY;
    }

    private String getTitleFromMetadata(Collection<Metadata> metadata) {
        Optional<Metadata> metadataOptional = metadata.parallelStream()
                .filter(metadataItem -> TitleGenerator.TITLE_DOC_MAIN.equals(metadataItem.getKey())).findFirst();
        if (metadataOptional.isPresent() && metadataOptional.get() instanceof MetadataEntry) {
            return ((MetadataEntry) metadataOptional.get()).getValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * initialize process details table.
     *
     * @param tempProcess
     *            whose metadata should be queried
     */
    private ProcessFieldedMetadata initializeTempProcessDetails(TempProcess tempProcess) {
        ProcessFieldedMetadata metadata = ImportService.initializeProcessDetails(tempProcess.getWorkpiece().getLogicalStructure(),
            createProcessForm.getRulesetManagement(), createProcessForm.getAcquisitionStage(),
            createProcessForm.getPriorityList());
        metadata.setMetadata(ImportService.importMetadata(tempProcess.getMetadataNodes(), MdSec.DMD_SEC));
        return metadata;
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
        } catch (IOException | DoctypeMissingException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }
}
