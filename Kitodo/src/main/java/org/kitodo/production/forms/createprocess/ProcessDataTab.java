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

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.CreateProcessForm;
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
    protected transient List<String> digitalCollections;
    private transient List<String> availableDigitalCollections;
    private transient Map<String, Boolean> standardFields;
    private String tifHeaderImageDescription = "";
    private String tifHeaderDocumentName = "";
    private int guessedImages = 0;
    private String tifDefinition;

    /**
     * Set docType.
     *
     * @param docType as java.lang.String
     */
    public void setDocType(String docType) {
        if (Objects.isNull(this.docType) || !this.docType.equals(docType)) {
            this.docType = docType;
            this.createProcessForm.getWorkpiece().getRootElement().setType(getRulesetType());
            if (this.docType.isEmpty()) {
                this.createProcessForm.getAdditionalDetailsTab().resetAddtionalDetailsTable();
            } else {
                this.createProcessForm.getAdditionalDetailsTab().show(this.createProcessForm.getWorkpiece().getRootElement());
            }
        }
    }

    public ProcessDataTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
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
     * Get rulesetType of docType.
     *
     * @return value of rulesetType
     */
    public String getRulesetType() {
        if (Objects.nonNull(docType) && !docType.isEmpty()) {
            try {
                ConfigOpacDoctype configOpacDoctype = ConfigOpac.getDoctypeByName(docType);
                if (Objects.nonNull(configOpacDoctype)) {
                    return configOpacDoctype.getRulesetType();
                }
            } catch (FileNotFoundException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return "";
    }

    /**
     * Get digitalCollections.
     *
     * @return value of digitalCollections
     */
    public List<String> getDigitalCollections() {
        return digitalCollections;
    }

    /**
     * Set digitalCollections.
     *
     * @param digitalCollections as java.util.List of String
     */
    public void setDigitalCollections(List<String> digitalCollections) {
        this.digitalCollections = digitalCollections;
    }

    /**
     * Get availableDigitalCollections.
     *
     * @return value of availableDigitalCollections
     */
    public List<String> getAvailableDigitalCollections() {
        return availableDigitalCollections;
    }

    /**
     * Set availableDigitalCollections.
     *
     * @param availableDigitalCollections as java.util.List of String
     */
    public void setAvailableDigitalCollections(List<String> availableDigitalCollections) {
        this.availableDigitalCollections = availableDigitalCollections;
    }

    /**
     * Get tifHeaderImageDescription.
     *
     * @return value of tifHeaderImageDescription
     */
    public String getTifHeaderImageDescription() {
        return tifHeaderImageDescription;
    }

    /**
     * Set tifHeaderImageDescription.
     *
     * @param tifHeaderImageDescription as java.lang.String
     */
    public void setTifHeaderImageDescription(String tifHeaderImageDescription) {
        this.tifHeaderImageDescription = tifHeaderImageDescription;
    }

    /**
     * Get tifHeaderDocumentName.
     *
     * @return value of tifHeaderDocumentName
     */
    public String getTifHeaderDocumentName() {
        return tifHeaderDocumentName;
    }

    /**
     * Set tifHeaderDocumentName.
     *
     * @param tifHeaderDocumentName as java.lang.String
     */
    public void setTifHeaderDocumentName(String tifHeaderDocumentName) {
        this.tifHeaderDocumentName = tifHeaderDocumentName;
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
     * reset all process data.
     */
    public void resetProcessData() {
        this.standardFields = new HashMap<>();
        this.standardFields.put("collections", true);
        this.standardFields.put("doctype", true);
        this.standardFields.put("regelsatz", true);
        this.standardFields.put("images", true);
        this.tifHeaderDocumentName = "";
        this.tifHeaderImageDescription = "";
    }

    public Map<String, Boolean> getStandardFields() {
        return this.standardFields;
    }

    /**
     * Get all document types.
     *
     * @return list of ConfigOpacDoctype objects
     */
    public List<ConfigOpacDoctype> getAllDoctypes() {
        return ConfigOpac.getAllDoctypes();
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
     * Set tifDefinition.
     *
     * @param tifDefinition as java.lang.String
     */
    public void setTifDefinition(String tifDefinition) {
        this.tifDefinition = tifDefinition;
    }

    /**
     * Generate process titles and other details.
     */
    public void generateProcessTitleAndTiffHeader() {
        generateProcessTitle();
        generateTiffHeader();
        Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid",
                "editForm:processFromTemplateTabView:additionalFields");
    }

    private void generateProcessTitle() {
        TitleGenerator titleGenerator = new TitleGenerator(this.atstsl,
                this.createProcessForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows());
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
        this.tifHeaderDocumentName = this.createProcessForm.getMainProcess().getTitle();

        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator(this.atstsl,
                this.createProcessForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows());
        try {
            this.tifHeaderImageDescription = tiffHeaderGenerator.generateTiffHeader(this.tifDefinition, this.docType);
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Set standardFields.
     *
     * @param standardFields as java.util.Map
     */
    public void setStandardFields(Map<String, Boolean> standardFields) {
        this.standardFields = standardFields;
    }
}
