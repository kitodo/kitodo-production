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
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.helper.Helper;

public abstract class Generator {

    private static final String INCOMPLETE_DATA = "errorDataIncomplete";

    protected String atstsl = "";
    protected List<ProcessDetail> processDetailsList;

    /**
     * Constructor for abstract Generator.
     *
     * @param atstsl
     *            field used for generation
     * @param processDetailsList
     *            fields used for generation
     */
    public Generator(String atstsl, List<ProcessDetail> processDetailsList) {
        if (Objects.nonNull(atstsl)) {
            this.atstsl = atstsl;
        }
        this.processDetailsList = processDetailsList;
    }

    /**
     * Get atstsl.
     *
     * @return value of atstsl
     */
    public String getAtstsl() {
        return atstsl;
    }

    protected String calculateProcessTitleCheck(String detailMetadataId, String detailValue) throws ProcessGenerationException {
        String processTitleCheck = detailValue;

        if ("CurrentNo".equals(detailMetadataId)) {
            try {
                int bandInt = Integer.parseInt(detailValue);
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
