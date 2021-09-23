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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.exceptions.DoctypeMissingException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.TempProcess;
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

            String currentTitle = TitleGenerator.getValueOfMetadataID(TitleGenerator.TITLE_DOC_MAIN, processDetails);

            if (StringUtils.isBlank(currentTitle)) {
                Process parentProcess = createProcessForm.getTitleRecordLinkTab().getTitleRecordProcess();

                if (Objects.nonNull(parentProcess)) {

                    // get title of workpiece property "Haupttitle" from chosen title record process
                    currentTitle = parentProcess.getWorkpieces().stream()
                            .filter(property -> "Haupttitel".equals(property.getTitle())).findFirst()
                            .map(Property::getValue).orElse(currentTitle);

                } else {
                    currentTitle = getTitleFromAncestors();
                }
            }

            String atstsl = ProcessService.generateProcessTitleAndGetAtstsl(processDetails, processTitle, process,
                currentTitle);

            // document name is generally equal to process title
            this.tiffHeaderDocumentName = process.getTitle();
            this.tiffHeaderImageDescription = ProcessService.generateTiffHeader(processDetails, atstsl,
                ServiceManager.getImportService().getTiffDefinition(), this.docType);
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid",
            "editForm:processFromTemplateTabView:processMetadata");
    }

    private String getTitleFromAncestors() {
        int processesSize = createProcessForm.getProcesses().size();

        if (processesSize <= 1) {
            return null;
        }

        List<TempProcess> ancestors = createProcessForm.getProcesses().subList(1, processesSize);
        AtomicReference<String> tempTitle = new AtomicReference<>();

        // get title of ancestors where TitleDocMain exists when several processes were
        // imported
        for (TempProcess tempProcess : ancestors) {
            ProcessFieldedMetadata processFieldedMetadata = initializeTempProcessDetails(tempProcess);
            Optional<Metadata> metadataOptional = processFieldedMetadata.getChildMetadata().parallelStream()
                    .filter(metadata -> TitleGenerator.TITLE_DOC_MAIN.equals(metadata.getKey())).findFirst();
            if (metadataOptional.isPresent() && metadataOptional.get() instanceof MetadataEntry) {
                return ((MetadataEntry) metadataOptional.get()).getValue();
            }
        }
        return tempTitle.get();
    }

    /**
     * initialize process details table.
     *
     * @param tempProcess
     *            whose metadata should be queried
     */
    private ProcessFieldedMetadata initializeTempProcessDetails(TempProcess tempProcess) {
        var metadata = ImportService.initializeProcessDetails(tempProcess.getWorkpiece().getLogicalStructure(),
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
