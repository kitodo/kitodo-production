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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.services.data.ImportService;

public class TitleGenerator extends Generator {

    /**
     * Metadata identifier for title doc main.
     */
    public static final String TITLE_DOC_MAIN = "TitleDocMain";

    /**
     * Metadata identifier for tsl/ats.
     */
    public static final String TSL_ATS = "TSL_ATS";

    /**
     * Constructor for TitleGenerator.
     *
     * @param atstsl                     field used for title generation
     * @param processDetailsList fields used for title generation
     */
    public TitleGenerator(String atstsl, List<ProcessDetail> processDetailsList) {
        super(atstsl, processDetailsList);
    }

    /**
     * Generate title for process.
     *
     * @param titleDefinition definition for title to generation
     * @param genericFields   Map of Strings
     * @return String
     */
    public String generateTitle(String titleDefinition, Map<String, String> genericFields)
            throws ProcessGenerationException {
        return generateTitle(titleDefinition, genericFields, getValueOfMetadataID(TITLE_DOC_MAIN, processDetailsList));
    }

    /**
     * Generate title for process.
     *
     * @param titleDefinition
     *            definition for title to generation
     * @param genericFields
     *            Map of Strings
     * @return String
     */
    public String generateTitle(String titleDefinition, Map<String, String> genericFields, String title)
            throws ProcessGenerationException {
        String currentAuthors = ImportService.getListOfCreators(this.processDetailsList);
        StringBuilder newTitle = new StringBuilder();

        StringTokenizer tokenizer = new StringTokenizer(titleDefinition, "+");
        // parse the band title
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            // if the string begins with ' and ends with ' then take over the content
            if (token.startsWith("'") && token.endsWith("'")) {
                newTitle.append(token, 1, token.length() - 1);
            } else if (token.startsWith("#")) {
                // resolve strings beginning with # from generic fields
                if (Objects.nonNull(genericFields)) {
                    String genericValue = genericFields.get(token);
                    if (Objects.nonNull(genericValue)) {
                        newTitle.append(genericValue);
                    }
                }
            } else {
                newTitle.append(evaluateAdditionalDetailsRows(title, currentAuthors, token));
            }
        }

        if (newTitle.toString().endsWith("_")) {
            newTitle.setLength(newTitle.length() - 1);
        }
        // remove non-ascii characters for the sake of TIFF header limits
        return newTitle.toString().replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * Forms the author title key, or the title key (4/2/2/1) if no author is
     * given. The author title key is a librarian sort criteria, composed out of
     * the first four letters of the first author’s last name, followed by the
     * first four letters of the title of the works. The title key (4/2/2/1) a
     * librarian sort criteria composed of the title of the works, taking the
     * first four letters of the first word, each the first two letters of the
     * second and third word, and the first letter of the fourth word of the
     * title. Note that this implementation removes non-word characters (any
     * characters except A-Z, such as letters with diacritics).
     *
     * <p>
     * <u>Examples:</u><br>
     * {@code createAtstsl("Twenty Thousand Leagues Under the Sea", "Verne")}:
     * {@code VernTwen}<br>
     * {@code createAtstsl("Oxford English Dictionary", null)}: {@code OxfoEnDi}
     *
     * @param title
     *            the title of the work
     * @param author
     *            the last name of the (first) author, may be {@code null} or
     *            empty
     * @return the author title key, or the title key (4/2/2/1)
     */
    public static String createAtstsl(String title, String author) {
        StringBuilder result = new StringBuilder(8);
        if (Objects.nonNull(author) && !author.trim().isEmpty()) {
            result.append(getPartString(author, 4));
            result.append(getPartString(title, 4));
        } else {
            StringTokenizer titleWords = new StringTokenizer(title);
            int wordNo = 1;
            while (titleWords.hasMoreTokens() && wordNo < 5) {
                String word = titleWords.nextToken();
                switch (wordNo) {
                    case 1:
                        result.append(getPartString(word, 4));
                        break;
                    case 2:
                    case 3:
                        result.append(getPartString(word, 2));
                        break;
                    case 4:
                        result.append(getPartString(word, 1));
                        break;
                    default:
                        break;
                }
                wordNo++;
            }
        }
        return result.toString().replaceAll("[\\W]", ""); // delete umlauts etc.
    }

    /**
     * Get the value of metadata identifier from process details list.
     *
     * @param metadataID
     *            The metadata identifier
     * @param processDetailsList
     *            The process detail list that contains the potential value
     * @return The value of metadata identifier or null
     */
    public static String getValueOfMetadataID(String metadataID, List<ProcessDetail> processDetailsList) {
        for (ProcessDetail row : processDetailsList) {
            String metadata = row.getMetadataID();
            if (Objects.nonNull(metadata) && metadata.equals(metadataID)) {
                return ImportService.getProcessDetailValue(row);
            }
        }
        return StringUtils.EMPTY;
    }

    private String evaluateAdditionalDetailsRows(String currentTitle, String currentAuthors, String token)
            throws ProcessGenerationException {
        StringBuilder newTitle = new StringBuilder();
        for (ProcessDetail row : this.processDetailsList) {
            String rowMetadataID = row.getMetadataID();
            String rowValue = ImportService.getProcessDetailValue(row);
            // if it is the ATS or TSL field, then use the calculated atstsl if it does not already exist
            if (TSL_ATS.equals(rowMetadataID)) {
                if (StringUtils.isBlank(rowValue)) {
                    rowValue = createAtstsl(currentTitle, currentAuthors);
                }
                this.atstsl = rowValue;
            }
            // add the content to the title
            if (rowMetadataID.equals(token) && Objects.nonNull(rowValue)) {
                newTitle.append(calculateProcessTitleCheck(rowMetadataID, rowValue));
            }
        }
        return newTitle.toString();
    }

    private static String getPartString(String word, int length) {
        return word.length() > length ? word.substring(0, length) : word;
    }
}
