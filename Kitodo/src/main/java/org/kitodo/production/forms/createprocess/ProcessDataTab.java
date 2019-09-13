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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.forms.CreateProcessForm;
import org.kitodo.production.helper.Helper;

public class ProcessDataTab {

    private static Logger logger = LogManager.getLogger(ProcessDataTab.class);

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

    private static final String OPAC_CONFIG = "configurationOPAC";

    /**
     * Set docType.
     *
     * @param docType as java.lang.String
     */
    public void setDocType(String docType) {
        if (Objects.isNull(this.docType) || !this.docType.equals(docType)) {
            this.docType = docType;
            try {
                this.createProcessForm.getWorkpiece().getRootElement()
                        .setType(ConfigOpac.getDoctypeByName(docType).getRulesetType());
            } catch (FileNotFoundException e) {
                logger.error(e.getLocalizedMessage());
            }
            this.createProcessForm.getAdditionalDetailsTab().show(this.createProcessForm.getWorkpiece().getRootElement());
        }
    }

    private static final String ERROR_READING = "errorReading";

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
     * @param digitalCollections as java.util.List<java.lang.String>
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
     * @param availableDigitalCollections as java.util.List<java.lang.String>
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
        try {
            return ConfigOpac.getAllDoctypes();
        } catch (RuntimeException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
            return new ArrayList<>();
        }
    }

    // TODO: improve upon process title generation!
    public String generateProcessTitle() {
        byte[] processTitle = new byte[7];
        new Random().nextBytes(processTitle);
        return new String(processTitle, StandardCharsets.UTF_8);
    }


    /**
     * Generate process titles and other details.
     */
    /*
    public void calculateProcessTitle() {
        TitleGenerator titleGenerator = new TitleGenerator(this.atstsl,
                this.createProcessForm.getAdditionalDetailsTab().getAdditionalDetailsTableRows());
        try {
            String newTitle = titleGenerator.generateTitle(this.titleDefinition, null);
            this.createProcessForm.getMainProcess().setTitle(newTitle);
            // atstsl is created in title generator and next used in tiff header generator
            this.atstsl = titleGenerator.getAtstsl();
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return;
        }

        calculateTiffHeader();

        Ajax.update("editForm:processFromTemplateTabView:processDataEditGrid");
    }
     */

    /**
     * Calculate tiff header.
     */
    /*
    private void calculateTiffHeader() {
        // document name is generally equal to process title
        this.tifHeaderDocumentName = this.createProcessForm.getMainProcess().getTitle();

        TiffHeaderGenerator tiffHeaderGenerator = new TiffHeaderGenerator(this.atstsl, this.additionalFields);
        try {
            this.tifHeaderImageDescription = tiffHeaderGenerator.generateTiffHeader(this.tifDefinition, this.docType);
        } catch (ProcessGenerationException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }
     */
}
