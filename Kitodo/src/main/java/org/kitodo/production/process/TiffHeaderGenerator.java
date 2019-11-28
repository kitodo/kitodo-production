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
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.data.ImportService;

public class TiffHeaderGenerator extends Generator {

    private static final String ERROR_READ = "errorReading";
    private static final String OPAC_CONFIG = "configurationOPAC";

    private String tiffHeader = "";

    /**
     * Constructor for TiffHeaderGenerator.
     *
     * @param atstsl
     *            field used for tiff header generation
     * @param processDetailsList
     *            fields used for tiff header generation
     */
    public TiffHeaderGenerator(String atstsl, List<ProcessDetail> processDetailsList) {
        super(atstsl, processDetailsList);
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
    public String generateTiffHeader(String tiffDefinition, String docType) throws ProcessGenerationException {
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
                tiffHeaderImageDescriptionBuilder.append(getProcessDetailValueByMetadataId(token));
            }
        }
        return reduceLengthOfTifHeaderImageDescription(tiffHeaderImageDescriptionBuilder.toString());
    }

    private String getProcessDetailValueByMetadataId(String metadataId) throws ProcessGenerationException {
        StringBuilder newTiffHeader = new StringBuilder();
        if ("Autoren".equals(metadataId)) {
            newTiffHeader.append(calculateProcessTitleCheck("Autoren",
                    ImportService.getListOfCreators(this.processDetailsList)));
        } else {
            for (ProcessDetail processDetail : this.processDetailsList) {
                String detailMetadataID = processDetail.getMetadataID();

                String detailValue = ImportService.getProcessDetailValue(processDetail);
                if (Objects.nonNull(detailValue)) {
                    if ("TitleDocMain".equals(detailMetadataID) && !detailValue.isEmpty()) {
                        this.tiffHeader = detailValue;
                    }
                    //if it is the ATS or TSL field, then use the calculated atstsl if it does not already exist
                    if ("TSL_ATS".equals(detailMetadataID) && detailValue.isEmpty() && !this.atstsl.isEmpty()) {
                        ImportService.setProcessDetailValue(processDetail, this.atstsl);
                    }
                    // add the content to the tiff header
                    if (detailMetadataID.equals(metadataId) && !detailValue.isEmpty()) {
                        newTiffHeader.append(calculateProcessTitleCheck(detailMetadataID, detailValue));
                    }
                }
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
    private String getTifHeaderType(String docType) throws ProcessGenerationException {
        try {
            ConfigOpacDoctype configOpacDoctype = ConfigOpac.getDoctypeByName(docType);
            if (Objects.nonNull(configOpacDoctype)) {
                return configOpacDoctype.getTifHeaderType();
            }
        } catch (FileNotFoundException e) {
            throw new ProcessGenerationException(
                    MessageFormat.format(Helper.getTranslation(ERROR_READ), Helper.getTranslation(OPAC_CONFIG)), e);
        }
        return "";
    }

    /**
     * Reduce to 255 characters.
     */
    private String reduceLengthOfTifHeaderImageDescription(String tifHeaderImageDescription)
            throws ProcessGenerationException {
        int length = tifHeaderImageDescription.length();
        if (length > 255) {
            try {
                int toCut = length - 255;
                String newTiffHeader = this.tiffHeader.substring(0, this.tiffHeader.length() - toCut);
                return tifHeaderImageDescription.replace(this.tiffHeader, newTiffHeader);
            } catch (IndexOutOfBoundsException e) {
                throw new ProcessGenerationException(e.getLocalizedMessage(), e);
            }
        }
        return tifHeaderImageDescription;
    }
}
