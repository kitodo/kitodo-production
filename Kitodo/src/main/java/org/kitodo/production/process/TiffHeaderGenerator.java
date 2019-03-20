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

package org.kitodo.production.process;

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.helper.AdditionalField;
import org.kitodo.production.helper.Helper;

public class TiffHeaderGenerator extends Generator {

    private static final Logger logger = LogManager.getLogger(TiffHeaderGenerator.class);

    private static final String ERROR_READ = "errorReading";
    private static final String OPAC_CONFIG = "configurationOPAC";

    private String tiffHeader = "";

    /**
     * Constructor for TiffHeaderGenerator.
     *
     * @param atstsl
     *            field used for tiff header generation
     * @param additionalFields
     *            fields used for tiff header generation
     */
    public TiffHeaderGenerator(String atstsl, List<AdditionalField> additionalFields) {
        super(atstsl, additionalFields);
    }

    /**
     * Generate tiff header for new process.
     *
     * @param tiffDefinition
     *            definition for tiff header to generation
     * @param docType
     *            document type for tiff header to generation
     * @return generated tiff header
     */
    public String generateTiffHeader(String tiffDefinition, String docType) {
        StringBuilder tiffHeaderImageDescriptionBuilder = new StringBuilder();
        // image description
        StringTokenizer tokenizer = new StringTokenizer(
                tiffDefinition.replaceAll("\\[\\[", "<").replaceAll("\\]\\]", ">"), "+");
        // parse the Tiff header
        this.tiffHeader = "";
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // if the string begins with ' and ends with ' then take over the content
            if (token.startsWith("'") && token.endsWith("'") && token.length() > 2) {
                tiffHeaderImageDescriptionBuilder.append(token, 1, token.length() - 1);
            } else if ("$Doctype".equals(token)) {
                tiffHeaderImageDescriptionBuilder.append(getTifHeaderType(docType));
            } else {
                // otherwise, evaluate the token as a field name
                tiffHeaderImageDescriptionBuilder.append(evaluateAdditionalFields(token));

            }
        }
        return reduceLengthOfTifHeaderImageDescription(tiffHeaderImageDescriptionBuilder.toString());
    }

    private String evaluateAdditionalFields(String token) {
        StringBuilder newTiffHeader = new StringBuilder();

        for (AdditionalField additionalField : this.additionalFields) {
            String title = additionalField.getTitle();
            String value = additionalField.getValue();
            boolean showDependingOnDoctype = additionalField.getShowDependingOnDoctype();

            if ("Titel".equals(title) || "Title".equals(title) && !StringUtils.isEmpty(value)) {
                this.tiffHeader = value;
            }
            /*
             * if it is the ATS or TSL field, then use the calculated atstsl if it does not
             * already exist
             */
            if (("ATS".equals(title) || "TSL".equals(title)) && showDependingOnDoctype && StringUtils.isEmpty(value)) {
                additionalField.setValue(this.atstsl);
            }

            // add the content to the tiff header
            if (title.equals(token) && showDependingOnDoctype && Objects.nonNull(value)) {
                newTiffHeader.append(calculateProcessTitleCheck(title, value));
            }
        }

        return newTiffHeader.toString();
    }

    /**
     * Get tiff header type from config Opac if the doctype should be specified.
     *
     * @param docType
     *            for tiff header
     * @return tif header type
     */
    private String getTifHeaderType(String docType) {
        try {
            ConfigOpacDoctype configOpacDoctype = ConfigOpac.getDoctypeByName(docType);
            if (Objects.nonNull(configOpacDoctype)) {
                return configOpacDoctype.getTifHeaderType();
            }
        } catch (FileNotFoundException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[] {Helper.getTranslation(OPAC_CONFIG) }, logger, e);
        }
        return "";
    }

    /**
     * Reduce to 255 characters.
     */
    private String reduceLengthOfTifHeaderImageDescription(String tifHeaderImageDescription) {
        int length = tifHeaderImageDescription.length();
        if (length > 255) {
            try {
                int toCut = length - 255;
                String newTiffHeader = this.tiffHeader.substring(0, this.tiffHeader.length() - toCut);
                return tifHeaderImageDescription.replace(this.tiffHeader, newTiffHeader);
            } catch (IndexOutOfBoundsException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
        return tifHeaderImageDescription;
    }
}
