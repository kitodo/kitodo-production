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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.field.AdditionalField;

public abstract class Generator {

    private static final String INCOMPLETE_DATA = "errorDataIncomplete";

    protected String atstsl = "";
    protected List<AdditionalField> additionalFields;

    /**
     * Constructor for abstract Generator.
     *
     * @param atstsl
     *            field used for generation
     * @param additionalFields
     *            fields used for generation
     */
    public Generator(String atstsl, List<AdditionalField> additionalFields) {
        if (Objects.nonNull(atstsl)) {
            this.atstsl = atstsl;
        }
        this.additionalFields = additionalFields;
    }

    /**
     * Get atstsl.
     *
     * @return value of atstsl
     */
    public String getAtstsl() {
        return atstsl;
    }

    /**
     * Set atstsl.
     *
     * @param atstsl
     *            as java.lang.String
     */
    public void setAtstsl(String atstsl) {
        this.atstsl = atstsl;
    }

    /**
     * Get additional fields.
     *
     * @return value of additionalFields
     */
    public List<AdditionalField> getAdditionalFields() {
        return additionalFields;
    }

    /**
     * Set additional fields.
     *
     * @param additionalFields
     *            as List of AdditionalField objects
     */
    public void setAdditionalFields(List<AdditionalField> additionalFields) {
        this.additionalFields = additionalFields;
    }

    protected String calculateProcessTitleCheck(String fieldName, String fieldValue) throws ProcessGenerationException {
        String processTitleCheck = fieldValue;

        if ("Bandnummer".equals(fieldName) || "Volume number".equals(fieldName)) {
            try {
                int bandInt = Integer.parseInt(fieldValue);
                DecimalFormat df = new DecimalFormat("#0000");
                processTitleCheck = df.format(bandInt);
            } catch (NumberFormatException e) {
                throw new ProcessGenerationException(
                        Helper.getTranslation(INCOMPLETE_DATA) + Helper.getTranslation("errorVolume"), e);
            }

            if (processTitleCheck.length() < 4) {
                processTitleCheck = "0000".substring(processTitleCheck.length()) + processTitleCheck;
            }
        }

        return processTitleCheck;
    }
}
