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

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.OCRWorkflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named
@SessionScoped
public class OCRWorkflowEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(OCRWorkflowEditView.class);
    private OCRWorkflow ocrWorkflow = new OCRWorkflow();

    private final String ocrWorkflowEditPath = MessageFormat.format(REDIRECT_PATH, "ocrWorkflowEdit");


    /**
     * Load import configuration by ID.
     *
     * @param id
     *            ID of import configuration to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                ocrWorkflow = ServiceManager.getOCRWorkflowService().getById(id);
            } else {
                ocrWorkflow = new OCRWorkflow();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] { ObjectType.OCR_WORKFLOW.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Save import configuration.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ServiceManager.getOCRWorkflowService().saveToDatabase(ocrWorkflow);
            return projectsPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING,
                    new Object[] {ObjectType.OCR_WORKFLOW.getTranslationSingular()}, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Get importConfiguration.
     *
     * @return value of importConfiguration
     */
    public OCRWorkflow getOcrWorkflow() {
        return ocrWorkflow;
    }

    /**
     * Set ocrWorkflow.
     *
     * @param ocrWorkflow as org.kitodo.data.database.beans.OCRWorkflow
     */
    public void setOcrWorkflow(OCRWorkflow ocrWorkflow) {
        this.ocrWorkflow = ocrWorkflow;
    }

}
