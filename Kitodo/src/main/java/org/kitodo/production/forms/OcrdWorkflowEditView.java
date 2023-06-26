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
import org.kitodo.data.database.beans.OcrdWorkflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("OcrdWorkflowEditView")
@SessionScoped
public class OcrdWorkflowEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(OcrdWorkflowEditView.class);
    private OcrdWorkflow ocrdWorkflow = new OcrdWorkflow();


    /**
     * Load import configuration by ID.
     *
     * @param id
     *            ID of import configuration to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                ocrdWorkflow = ServiceManager.getOcrdWorkflowService().getById(id);
            } else {
                ocrdWorkflow = new OcrdWorkflow();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.OCRD_WORKFLOW.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Save import configuration.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ocrdWorkflow.setClient(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
            ServiceManager.getOcrdWorkflowService().saveToDatabase(ocrdWorkflow);
            return projectsPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING,
                    new Object[] {ObjectType.OCRD_WORKFLOW.getTranslationSingular()}, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Get list of ocr workflow filenames.
     *
     * @return list of ocr workflow filenames
     */
    public List<Path> getOCRWorkflowFilenames() {
        try (Stream<Path> ocrWorkflowPaths = Files.walk(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_OCRD_WORKFLOWS)))) {
            return ocrWorkflowPaths.filter(f -> f.toString().endsWith(".sh"))
                    .map(Path::getFileName).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.OCRD_WORKFLOW.getTranslationPlural() },
                    logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get importConfiguration.
     *
     * @return value of importConfiguration
     */
    public OcrdWorkflow getOcrWorkflow() {
        return ocrdWorkflow;
    }

    /**
     * Set ocrWorkflow.
     *
     * @param ocrdWorkflow as org.kitodo.data.database.beans.OCRWorkflow
     */
    public void setOcrWorkflow(OcrdWorkflow ocrdWorkflow) {
        this.ocrdWorkflow = ocrdWorkflow;
    }


}
