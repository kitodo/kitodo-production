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
import org.kitodo.production.forms.createprocess.AdditionalDetailsTableRow;
import org.kitodo.production.helper.Helper;

public abstract class Generator {

    private static final String INCOMPLETE_DATA = "errorDataIncomplete";

    protected String atstsl = "";
    protected List<AdditionalDetailsTableRow> additionalDetailsTableRows;

    /**
     * Constructor for abstract Generator.
     *
     * @param atstsl
     *            field used for generation
     * @param additionalDetailsTableRows
     *            fields used for generation
     */
    public Generator(String atstsl, List<AdditionalDetailsTableRow> additionalDetailsTableRows) {
        if (Objects.nonNull(atstsl)) {
            this.atstsl = atstsl;
        }
        this.additionalDetailsTableRows = additionalDetailsTableRows;
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
    public List<AdditionalDetailsTableRow> getAdditionalFields() {
        return additionalDetailsTableRows;
    }

    /**
     * Set additional fields.
     *
     * @param additionalDetailsTableRows
     *            as List of AdditionalField objects
     */
    public void setAdditionalFields(List<AdditionalDetailsTableRow> additionalDetailsTableRows) {
        this.additionalDetailsTableRows = additionalDetailsTableRows;
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
