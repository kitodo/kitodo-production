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
import java.text.MessageFormat;
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
import org.kitodo.data.database.beans.OCRWorkflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("OCRWorkflowListView")
@SessionScoped
public class OCRWorkflowListView extends BaseForm {
    private OCRWorkflow ocrWorkflow;
    private static final Logger logger = LogManager.getLogger(OCRWorkflowListView.class);
    private final String ocrWorkflowEditPath = MessageFormat.format(REDIRECT_PATH, "ocrWorkflowEdit");


    /**
     * Get import configurations.
     *
     * @return import configurations
     */
    public List<OCRWorkflow> getOcrWorkflows() {
        try {
            return ServiceManager.getOCRWorkflowService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY,
                    new Object[] { ObjectType.OCR_WORKFLOW.getTranslationPlural() }, logger, e);
            return new ArrayList<>();
        }
    }


    public String newOCRWorkflow() {
        return ocrWorkflowEditPath;
    }

    /**
     * Delete ocr workflow identified by ID.
     *
     * @param id ID of ocr workflow to delete
     */
    public void deleteById(int id) {
        try {
            ServiceManager.getOCRWorkflowService().removeFromDatabase(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.OCR_WORKFLOW.getTranslationSingular() }, logger, e);
        }
    }

}
