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

package org.kitodo.production.forms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.OcrProfile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("OcrProfileEditView")
@SessionScoped
public class OcrProfileEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(OcrProfileEditView.class);
    private OcrProfile ocrProfile = new OcrProfile();

    /**
     * Load OCR profile by ID.
     *
     * @param id
     *         ID of OCR profile to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                ocrProfile = ServiceManager.getOcrProfileService().getById(id);
            } else {
                ocrProfile = new OcrProfile();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.OCR_PROFILE.getTranslationSingular(), id}, logger, e);
        }
    }

    /**
     * Save OCR profile
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ocrProfile.setClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
            ServiceManager.getOcrProfileService().saveToDatabase(ocrProfile);
            return projectsPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.OCR_PROFILE.getTranslationSingular()}, logger,
                    e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Get list of OCR profile filenames.
     *
     * @return list of OCR profile filenames
     */
    public List<Path> getFilenames() {
        try (Stream<Path> ocrProfilePaths = Files.walk(
                Paths.get(ConfigCore.getParameter(ParameterCore.DIR_OCR_PROFILES)))) {
            return ocrProfilePaths.map(Path::getFileName).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.OCR_PROFILE.getTranslationPlural()},
                    logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get OCR profile.
     *
     * @return value of OCR profile
     */
    public OcrProfile getOcrProfile() {
        return ocrProfile;
    }

    /**
     * Set OCR profile.
     *
     * @param ocrProfile as org.kitodo.data.database.beans.OcrProfile
     */
    public void setOcrProfile(OcrProfile ocrProfile) {
        this.ocrProfile = ocrProfile;
    }

}
