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

package de.sub.goobi.export.dms;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.exceptions.ExportFileException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

public class ExportDms_CorrectRusdml {
    private final PrefsInterface prefsInterface;
    private final Process process;
    private final DigitalDocumentInterface digitalDocumentInterface;
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(ExportDms_CorrectRusdml.class);

    /**
     * Constructor.
     *
     * @param process object
     * @param prefsInterface   Prefs object
     * @param gdzFile Fileformat object
     */
    public ExportDms_CorrectRusdml(Process process, PrefsInterface prefsInterface, FileformatInterface gdzFile) throws PreferencesException {
        this.prefsInterface = prefsInterface;
        this.digitalDocumentInterface = gdzFile.getDigitalDocument();
        this.process = process;
    }

    /**
     * Start correction.
     *
     * @return String
     */
    public String correctionStart() throws ExportFileException, MetadataTypeNotAllowedException {
        String atsPpnBand = generateAtsPpnBand();

        serviceManager.getSchemaService().tempConvertRusdml(digitalDocumentInterface, prefsInterface, process, atsPpnBand);

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
