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

package org.kitodo.production.helper.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.helper.metadata.ImageHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.xml.sax.SAXException;

public class BatchTaskHelper extends BatchHelper {
    private List<Task> steps;
    private static final Logger logger = LogManager.getLogger(BatchTaskHelper.class);
    private Task currentStep;
    private String processName = "";
    private String script;
    private final WebDav myDav = new WebDav();
    private List<String> processNameList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param steps
     *            list of tasks
     */
    public BatchTaskHelper(List<Task> steps) {
        this.steps = steps;
        for (Task s : steps) {
            this.processNameList.add(s.getProcess().getTitle());
        }
        if (!steps.isEmpty()) {
            this.currentStep = steps.getFirst();
            this.processName = this.currentStep.getProcess().getTitle();
            loadProcessProperties(this.currentStep);
        }
    }

    public List<Task> getSteps() {
        return this.steps;
    }

    public void setSteps(List<Task> steps) {
        this.steps = steps;
    }

    public Task getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(Task currentStep) {
        this.currentStep = currentStep;
    }

    public List<String> getProcessNameList() {
        return this.processNameList;
    }

    public void setProcessNameList(List<String> processNameList) {
        this.processNameList = processNameList;
    }

    public String getProcessName() {
        return this.processName;
    }

    /**
     * Set process' name.
     *
     * @param processName
     *            String
     */
    public void setProcessName(String processName) {
        this.processName = processName;
        for (Task s : this.steps) {
            if (s.getProcess().getTitle().equals(processName)) {
                this.currentStep = s;
                loadProcessProperties(this.currentStep);
                break;
            }
        }
    }

    private void loadProcessProperties(Task task) {
        Process process = task.getProcess();
        this.properties = process.getProperties();
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Execute script.
     */
    public void executeScript() throws DAOException {
        for (Task task : this.steps) {
            if (task.getScriptName().equals(this.script)) {
                String scriptPath = task.getScriptPath();
                ServiceManager.getTaskService().executeScript(task, scriptPath, false);
            }
        }
    }

    /**
     * Export DMS.
     */
    public void exportDMS() {
        for (Task step : this.steps) {
            ExportDms export = new ExportDms();
            try {
                export.startExport(step.getProcess());
            } catch (DAOException e) {
                Helper.setErrorMessage("errorExporting",
                    new Object[] {Helper.getTranslation("task"), step.getId() }, logger, e);
            }
        }
    }

    /**
     * Not sure.
     *
     * @return String
     */
    public String openBatchTasksByUser() {
        for (Task task : this.steps) {
            if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                this.myDav.uploadFromHome(task.getProcess());
            }
            task.setProcessingStatus(TaskStatus.OPEN);
            if (task.isCorrection()) {
                task.setProcessingBegin(null);
            }
            task.setEditType(TaskEditType.MANUAL_MULTI);
            task.setProcessingTime(new Date());
            User user = ServiceManager.getUserService().getAuthenticatedUser();
            ServiceManager.getTaskService().replaceProcessingUser(task, user);

            try {
                ServiceManager.getTaskService().save(task);
            } catch (DAOException e) {
                Helper.setErrorMessage("errorSaving",  new Object[] {ObjectType.TASK.getTranslationSingular()}, logger, e);
            }
        }
        return "";
    }

    /**
     * Not sure.
     *
     * @return String
     */
    public String closeBatchTasksByUser() {
        for (Task task : this.steps) {

            try {
                boolean valid = isTaskValid(task);

                if (valid) {
                    if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                        this.myDav.uploadFromHome(task.getProcess());
                    }
                    task.setEditType(TaskEditType.MANUAL_MULTI);
                    new WorkflowControllerService().close(task);
                }
            } catch (DAOException | IOException | SAXException | FileStructureValidationException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }

        return "";
    }

    private boolean isTaskValid(Task task) {
        boolean valid = true;

        if (task.isTypeCloseVerify()) {
            if (invalidMetadataExists(task)) {
                valid = false;
            }
            if (invalidImageExists(task)) {
                valid = false;
            }

            loadProcessProperties(task);
            if (invalidPropertyExists(task)) {
                valid = false;
            }
        }

        return valid;
    }

    private boolean invalidMetadataExists(Task task) {
        if (task.isTypeMetadata()
                && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.USE_META_DATA_VALIDATION)) {
            return !ServiceManager.getMetadataValidationService().validate(task.getProcess());
        }
        return false;
    }

    private boolean invalidImageExists(Task task) {
        if (task.isTypeImagesWrite()) {
            ImageHelper mih = new ImageHelper();
            if (!mih.checkIfImagesValid(task.getProcess().getTitle(),
                ServiceManager.getProcessService().getImagesOriginDirectory(false, task.getProcess()))) {
                Helper.setErrorMessage("errorImageValidation");
                return true;
            }
        }
        return false;
    }

    private boolean invalidPropertyExists(Task task) {
        for (Property prop : this.properties) {
            if (isPropertyInvalid(prop, task)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPropertyInvalid(Property property, Task task) {
        if (Objects.isNull(property.getValue()) || property.getValue().isEmpty()) {
            Helper.setErrorMessage("batchPropertyEmpty",
                new Object[] {property.getTitle(), task.getProcess().getTitle() });
            return true;
        }
        return false;
    }
}
