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

package org.kitodo.production.exporter.dms;

import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.ExportFileException;
import org.kitodo.production.helper.BeanHelper;
import org.kitodo.production.services.ServiceManager;

public class ExportDmsCorrectRusdml {
    private final PrefsInterface prefs;
    private final Process process;
    private final DigitalDocumentInterface digitalDocument;

    /**
     * Constructor.
     *
     * @param process
     *            object
     * @param prefs
     *            Prefs object
     * @param gdzFile
     *            Fileformat object
     */
    public ExportDmsCorrectRusdml(Process process, PrefsInterface prefs, FileformatInterface gdzFile)
            throws PreferencesException {
        this.prefs = prefs;
        this.digitalDocument = gdzFile.getDigitalDocument();
        this.process = process;
    }

    /**
     * Start correction.
     *
     * @return String
     */
    public String correctionStart() throws ExportFileException, MetadataTypeNotAllowedException {
        String atsPpnBand = generateAtsPpnBand();
        ServiceManager.getSchemaService().tempConvertRusdml(digitalDocument, prefs, process, atsPpnBand);
        return atsPpnBand;
    }

    private String generateAtsPpnBand() {
        String atsPpnBand;
        /*
         * Determine process properties
         */
        atsPpnBand = BeanHelper.determineWorkpieceProperty(process, "ATS")
                + BeanHelper.determineWorkpieceProperty(process, "TSL") + "_";
        String ppn = BeanHelper.determineWorkpieceProperty(process, "PPN digital");
        if (!ppn.startsWith("PPN")) {
            ppn = "PPN" + ppn;
        }
        atsPpnBand += ppn;
        String bandNumber = BeanHelper.determineWorkpieceProperty(process, "Band");
        if (bandNumber != null && bandNumber.length() > 0) {
            atsPpnBand += "_" + BeanHelper.determineWorkpieceProperty(process, "Band");
        }

        return atsPpnBand;
    }
}
