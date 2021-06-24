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
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.omnifaces.util.Ajax;

public class ProcessDataTab {

    private static final Logger logger = LogManager.getLogger(ProcessDataTab.class);

    private List<SelectItem> allDocTypes;
    private final CreateProcessForm createProcessForm;
    private String docType;
    private String atstsl = "";
    private String tiffHeaderImageDescription = "";
    private String tiffHeaderDocumentName = "";
    private int guessedImages = 0;

    ProcessDataTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Set docType.
     *
     * @param docType as java.lang.String
     */
    public void setDocType(String docType) {
        if (Objects.isNull(allDocTypes) || allDocTypes.isEmpty()) {
            this.docType = "";
            Helper.setErrorMessage("errorLoadingDocTypes");
        } else if (docTypeExistsInRuleset(docType)) {
            this.docType = docType;
        } else {
            this.docType = (String) allDocTypes.get(0).getValue();
            Helper.setErrorMessage("docTypeNotFound", new Object[] {docType});
        }
        if (!this.createProcessForm.getProcesses().isEmpty()) {
            this.createProcessForm.getProcesses().get(0).getWorkpiece().getLogicalStructure().setType(this.docType);
            if (this.docType.isEmpty()) {
                this.createProcessForm.getProcessMetadataTab().setProcessDetails(ProcessFieldedMetadata.EMPTY);
            } else {
                ProcessFieldedMetadata metadata = this.createProcessForm.getProcessMetadataTab()
                        .initializeProcessDetails(this.createProcessForm.getProcesses().get(0).getWorkpiece().getLogicalStructure());
                this.createProcessForm.getProcessMetadataTab().setProcessDetails(metadata);
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
     * Get tiffHeaderImageDescription.
     *
     * @return value of tiffHeaderImageDescription
     */
    public String getTiffHeaderImageDescription() {
        return tiffHeaderImageDescription;
    }

    /**
     * Set tiffHeaderImageDescription.
     *
     * @param tiffHeaderImageDescription as java.lang.String
     */
    public void setTiffHeaderImageDescription(String tiffHeaderImageDescription) {
        this.tiffHeaderImageDescription = tiffHeaderImageDescription;
    }

    /**
     * Get tiffHeaderDocumentName.
     *
     * @return value of tiffHeaderDocumentName
     */
    public String getTiffHeaderDocumentName() {
        return tiffHeaderDocumentName;
    }

    /**
     * Set tiffHeaderDocumentName.
     *
     * @param tiffHeaderDocumentName as java.lang.String
     */
    public void setTiffHeaderDocumentName(String tiffHeaderDocumentName) {
        this.tiffHeaderDocumentName = tiffHeaderDocumentName;
    }

    /**
     * Get guessedImages.
     *
     * @return value of guessedImages
     */
    public int getGuessedImages() {
        return guessedImages;
    }

    /**
     * Set guessedImages.
     *
     * @param guessedImages as int
     */
    public void setGuessedImages(int guessedImages) {
        this.guessedImages = guessedImages;
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
        List<ProcessDetail> processDetails = this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements();
        Process process = this.createProcessForm.getMainProcess();
        try {
            StructuralElementViewInterface docTypeView = createProcessForm.getRulesetManagement().getStructuralElementView(
                docType, createProcessForm.getAcquisitionStage(), createProcessForm.getPriorityList());
            String processTitle = docTypeView.getProcessTitle().orElse("");
            if (processTitle.isEmpty()) {
                Helper.setErrorMessage("newProcess.titleGeneration.creationRuleNotFound",
                    new Object[] {getDocTypeLabel(docType), process.getRuleset().getTitle() });
            }
            this.atstsl = ProcessService.generateProcessTitle(this.atstsl, processDetails,
                processTitle, process);
            // document name is generally equal to process title
            this.tiffHeaderDocumentName = process.getTitle();
            this.tiffHeaderImageDescription = ProcessService.generateTiffHeader(
                    processDetails, this.atstsl, ServiceManager.getImportService().getTiffDefinition(), this.docType);
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid",
                "editForm:processFromTemplateTabView:processMetadata");
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
