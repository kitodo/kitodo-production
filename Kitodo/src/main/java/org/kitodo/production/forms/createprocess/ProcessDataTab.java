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
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigProject;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.TiffHeaderGenerator;
import org.kitodo.production.process.TitleGenerator;
import org.omnifaces.util.Ajax;

public class ProcessDataTab {

    private static final Logger logger = LogManager.getLogger(ProcessDataTab.class);

    private CreateProcessForm createProcessForm;
    private String docType;
    private String atstsl = "";
    private String titleDefinition;
    private String tiffHeaderImageDescription = "";
    private String tiffHeaderDocumentName = "";
    private String tiffDefinition;
    private boolean usingTemplates;
    private int guessedImages = 0;

    public ProcessDataTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * Set docType.
     *
     * @param docType as java.lang.String
     */
    public void setDocType(String docType) {
        if (Objects.isNull(this.docType) || !this.docType.equals(docType)) {
            this.docType = docType;
            this.createProcessForm.getWorkpiece().getRootElement().setType(this.docType);
            if (this.docType.isEmpty()) {
                this.createProcessForm.getProcessMetadataTab().setProcessDetails(new ProcessFieldedMetadata());
            } else {
                this.createProcessForm.getProcessMetadataTab()
                        .initializeProcessDetails(this.createProcessForm.getWorkpiece().getRootElement());
            }
        }
    }

    /**
     * Get docType.
     *
     * @return value of docType
     */
    public String getDocType() {
        if (Objects.isNull(docType)) {
            String monograph = (String) getAllDoctypes()
                    .stream()
                    .filter(typ -> typ.getValue().equals("Monograph"))
                    .findFirst()
                    .get().getValue();
            setDocType(Objects.isNull(monograph) || monograph.isEmpty() ? (String) getAllDoctypes().get(0).getValue() : monograph);
        }
        return docType;
    }

    /**
     * Get useTemplate.
     *
     * @return value of useTemplate
     */
    public boolean isUsingTemplates() {
        return usingTemplates;
    }

    /**
     * Set useTemplate.
     *
     * @param usingTemplates as boolean
     */
    public void setUsingTemplates(boolean usingTemplates) {
        this.usingTemplates = usingTemplates;
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
        return createProcessForm.getRuleset()
                .getStructuralElements(createProcessForm.getPriorityList()).entrySet()
                .stream().map(entry -> new SelectItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Set titleDefinition.
     *
     * @param titleDefinition as java.lang.String
     */
    public void setTitleDefinition(String titleDefinition) {
        this.titleDefinition = titleDefinition;
    }

    /**
     * Get titleDefinition.
     *
     * @return value of titleDefinition
     */
    public String getTitleDefinition() {
        return titleDefinition;
    }

    /**
     * Set tiffDefinition.
     *
     * @param tiffDefinition as java.lang.String
     */
    public void setTiffDefinition(String tiffDefinition) {
        this.tiffDefinition = tiffDefinition;
    }

    /**
     * Get tiffDefinition.
     *
     * @return value of tifDefinition
     */
    public String getTiffDefinition() {
        return tiffDefinition;
    }

    /**
     * Generate process titles and other details.
     */
    public void generateProcessTitleAndTiffHeader() {
        generateProcessTitle();
        generateTiffHeader();
        Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid",
                "editForm:processFromTemplateTabView:processMetadata");
    }

    private void generateProcessTitle() {
        TitleGenerator titleGenerator = new TitleGenerator(this.atstsl,
                this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements());
        try {
            String newTitle = titleGenerator.generateTitle(this.titleDefinition, null);
            this.createProcessForm.getMainProcess().setTitle(newTitle);
            // atstsl is created in title generator and next used in tiff header generator
            this.atstsl = titleGenerator.getAtstsl();
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Calculate tiff header.
     */
    private void generateTiffHeader() {
        // document name is generally equal to process title
        this.tiffHeaderDocumentName = this.createProcessForm.getMainProcess().getTitle();

        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator(this.atstsl,
                this.createProcessForm.getProcessMetadataTab().getProcessDetailsElements());
        try {
            this.tiffHeaderImageDescription = tiffHeaderGenerator.generateTiffHeader(this.tiffDefinition, this.docType);
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Read project configs for display in GUI.
     */
    public void prepare() {
        ConfigProject configProject;
        try {
            configProject = new ConfigProject(createProcessForm.getProject().getTitle());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }
        usingTemplates = configProject.isUseTemplates();
        tiffDefinition = configProject.getTifDefinition();
        titleDefinition = configProject.getTitleDefinition();
    }
}
