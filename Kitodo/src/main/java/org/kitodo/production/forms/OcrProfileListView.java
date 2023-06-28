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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.OcrProfile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("OcrProfileListView")
@SessionScoped
public class OcrProfileListView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(OcrProfileListView.class);
    private final String ocrProfileCreatePath = MessageFormat.format(REDIRECT_PATH, "ocrProfileEdit");


    /**
     * Get ocr profile.
     *
     * @return list of ocr profiles.
     */
    public List<OcrProfile> getOcrProfile() {
        try {
            return ServiceManager.getOcrProfileService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY,
                    new Object[] { ObjectType.OCR_PROFILE.getTranslationPlural() }, logger, e);
            return new ArrayList<>();
        }
    }


    public String newOcrProfile() {
        return ocrProfileCreatePath;
    }

    /**
     * Delete ocr profile identified by ID.
     *
     * @param id ID of ocr profile to delete
     */
    public void deleteById(int id) {
        try {
            ServiceManager.getOcrProfileService().removeFromDatabase(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.OCR_PROFILE.getTranslationSingular() }, logger, e);
        }
    }

}
