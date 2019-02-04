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

package org.kitodo.services.validation;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.services.ServiceManager;

public class MetadataValidationService {

    private List<LegacyDocStructHelperInterface> docStructsOhneSeiten;
    private Process process;
    private boolean autoSave = false;
    private static final Logger logger = LogManager.getLogger(MetadataValidationService.class);
    private static final String VALIDATE_METADATA = "validate.metadata";

    /**
     * Validate.
     *
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(Process process) {
        LegacyPrefsHelper prefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());
        LegacyMetsModsDigitalDocumentHelper gdzfile;
        try {
            gdzfile = ServiceManager.getProcessService().readMetadataFile(process);
        } catch (IOException | RuntimeException e) {
            Helper.setErrorMessage("metadataReadError", new Object[] {process.getTitle() }, logger, e);
            return false;
        }
        return validate(gdzfile, prefs, process);
    }

    /**
     * Validate.
     *
     * @param gdzfile
     *            Fileformat object
     * @param prefs
     *            Prefs object
     * @param process
     *            object
     * @return boolean
     */
    public boolean validate(LegacyMetsModsDigitalDocumentHelper gdzfile, LegacyPrefsHelper prefs, Process process) {
        // TODO: new validation needs to be implemented here
        return true;
    }

    /**
     * Check if automatic save is allowed.
     *
     * @return true or false
     */
    public boolean isAutoSave() {
        return this.autoSave;
    }

    /**
     * Set if automatic save is allowed.
     *
     * @param autoSave
     *            true or false
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }
}
