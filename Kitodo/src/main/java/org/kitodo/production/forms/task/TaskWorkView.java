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

package org.kitodo.production.forms.task;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.export.ExportDms;
import org.kitodo.export.TiffHeader;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.ValidatableForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.production.thread.TaskImageGeneratorThread;
import org.kitodo.utils.Stopwatch;
import org.xml.sax.SAXException;

@Named("TaskWorkView")
@ViewScoped
public class TaskWorkView extends ValidatableForm {

    private static final Logger logger = LogManager.getLogger(TaskWorkView.class);

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "currentTasksEdit");
    private final WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    private Task task = new Task();

    private String scriptPath;

    public static String getViewPath(Task task) {
        return VIEW_PATH + "&id=" + task.getId();
    }

    /**
     * Return task that is currently being worked on.
     */
    public Task getTask() {
        return this.task;
    }
 
    /**
     * Method being used as viewAction when rendering template.
     *
     * @param id
     *            ID of the task to load
     */
    public void loadTaskById(int id) {
        Stopwatch stopwatch = new Stopwatch(this, "loadTaskById", "id", Integer.toString(id));
        try {
            setCurrentTask(ServiceManager.getTaskService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Set current task with edit mode set to empty String.
     *
     * @param task
     *            Object
     */
    public void setCurrentTask(Task task) {
        final Stopwatch stopwatch = new Stopwatch(this.getClass(), task, "setCurrentTask");
        this.task = task;
        this.task.setLocalizedTitle(ServiceManager.getTaskService().getLocalizedTitle(task.getTitle()));
        stopwatch.stop();
    }

    /**
     * Unlock the current task's process.
     *
     * @return stay on the current page
     */
    public String releaseLock() {
        Stopwatch stopwatch = new Stopwatch(this, "releaseLock");
        MetadataLock.setFree(this.task.getProcess().getId());
        return stopwatch.stop(this.stayOnCurrentPage);
    }

    /**
     * Release task - set up task status to open and make available for other users
     * to take over.
     *
     * @return page
     */
    public String releaseTask() {
        Stopwatch stopwatch = new Stopwatch(this, "releaseTask");
        try {
            this.workflowControllerService.unassignTaskFromUser(this.task);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
            return stopwatch.stop(this.stayOnCurrentPage);
        }
        return stopwatch.stop(tasksPage);
    }

    /**
     * Return script path.
     * 
     * @return the script path
     */
    public String getScriptPath() {
        Stopwatch stopwatch = new Stopwatch(this, "getScriptPath");
        return stopwatch.stop(this.scriptPath);
    }

    /**
     * Sets the script path.
     *
     * @param scriptPath
     *            script path to set
     */
    public void setScriptPath(String scriptPath) {
        Stopwatch stopwatch = new Stopwatch(this, "setScriptPath", "scriptPath", scriptPath);
        this.scriptPath = scriptPath;
        stopwatch.stop();
    }

    /**
     * Execute script.
     */
    public void executeScript() throws DAOException {
        Stopwatch stopwatch = new Stopwatch(this, "executeScript");
        Task task = ServiceManager.getTaskService().getById(this.task.getId());
        if (ServiceManager.getTaskService().executeScript(task, this.scriptPath, false)) {
            Helper.setMessageWithoutDescription(
                    Helper.getTranslation("scriptExecutionSuccessful", this.task.getScriptName()));
        } else {
            Helper.setErrorMessagesWithoutDescription(
                    Helper.getTranslation("scriptExecutionError", this.task.getScriptName()));
        }
        stopwatch.stop();
    }

    /**
     * Downloads.
     */
    public void downloadTiffHeader() throws IOException {
        Stopwatch stopwatch = new Stopwatch(this, "downloadTiffHeader");
        TiffHeader tiff = new TiffHeader(this.task.getProcess());
        tiff.exportStart();
        stopwatch.stop();
    }


    /**
     * Export DMS.
     */
    public void exportDMS() {
        Stopwatch stopwatch = new Stopwatch(this, "exportDMS");
        ExportDms export = new ExportDms();
        try {
            export.startExport(this.task.getProcess());
        } catch (DAOException e) {
            Helper.setErrorMessage("errorExport", new Object[] {this.task.getProcess().getTitle() }, logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Get the id of the template task corresponding to the given task.
     * The corresponding template task was the blueprint when creating the given task.
     * @param task task to find the corresponding template task for
     * @return id of the template task or -1 if no matching task could be found
     */
    public static int getCorrespondingTemplateTaskId(Task task) {
        Stopwatch stopwatch = new Stopwatch(TaskListView.class, task, "getCorrespondingTemplateTaskId");
        return stopwatch.stop(TaskService.getCorrespondingTemplateTaskId(task));
    }

    /**
     * Checks if the task type is "generateImages" and thus the generate images links are shown.
     *
     * @return whether action links should be displayed
     */
    public boolean isShowingGenerationActions() {
        Stopwatch stopwatch = new Stopwatch(this, "isShowingGenerationActions");
        return stopwatch.stop(task.isTypeGenerateImages());
    }

    /**
     * Checks if folders for generation are configured in the project.
     * @return whether the folders are configured.
     */
    public boolean isImageGenerationPossible() {
        Stopwatch stopwatch = new Stopwatch(this, "isImageGenerationPossible");
        return stopwatch.stop(TaskService.generatableFoldersFromProjects(Stream.of(task.getProcess()
                .getProject())).findAny().isPresent());
    }

    /**
     * Generate all images.
     */
    public void generateAllImages() {
        Stopwatch stopwatch = new Stopwatch(this, "generateAllImages");
        generateImages(GenerationMode.ALL, "regenerateAllImagesStarted");
        stopwatch.stop();
    }


    /**
     * Generate missing and damaged images.
     */
    public void generateMissingAndDamagedImages() {
        Stopwatch stopwatch = new Stopwatch(this, "generateMissingAndDamagedImages");
        generateImages(GenerationMode.MISSING_OR_DAMAGED, "regenerateMissingAndDamagedImagesStarted");
        stopwatch.stop();
    }

    /**
     * Generate missing images.
     */
    public void generateMissingImages() {
        Stopwatch stopwatch = new Stopwatch(this, "generateMissingImages");
        generateImages(GenerationMode.MISSING, "regenerateMissingImagesStarted");
        stopwatch.stop();
    }

    /**
     * Action that creates images.
     *
     * @param mode
     *            which function should be executed
     * @param messageKey
     *            message displayed to the user (key for resourcebundle)
     */
    private void generateImages(GenerationMode mode, String messageKey) {
        Folder generatorSource = task.getProcess().getProject().getGeneratorSource();
        List<Folder> contentFolders = task.getContentFolders();
        if (Objects.isNull(generatorSource)) {
            Helper.setErrorMessage("noSourceFolderConfiguredInProject");
            return;
        }
        if (Objects.isNull(contentFolders)) {
            Helper.setErrorMessage("noImageFolderConfiguredInProject");
            return;
        }
        Subfolder sourceFolder = new Subfolder(task.getProcess(), generatorSource);
        if (sourceFolder.listContents().isEmpty()) {
            Helper.setErrorMessage("emptySourceFolder");
        } else {
            List<Subfolder> outputs = SubfolderFactoryService.createAll(task.getProcess(), contentFolders);
            ImageGenerator imageGenerator = new ImageGenerator(sourceFolder, mode, outputs);
            TaskManager.addTask(new TaskImageGeneratorThread(task.getProcess().getTitle(), imageGenerator));
            Helper.setMessage(messageKey);
        }
    }

    /**
     * Checks if the task type is "validateImages" and thus the task action link is shown.
     *
     * @return whether action link for validating images should be displayed
     */
    public boolean isShowingImageValidationAction() {
        return task.isTypeValidateImages();
    }

    /**
     * Checks if any folders are configured to contain images that need to be validated.
     * 
     * @return whether there are folders with images that are supposed to validated.
     */
    public boolean isImageValidationPossible() {
        return !task.getValidationFolders().isEmpty();
    }

    /**
     * Close method task called by user action.
     *
     * @return page
     */
    public String closeTaskByUser() {
        Stopwatch stopwatch = new Stopwatch(this, "closeTaskByUser");
        try {
            this.workflowControllerService.closeTaskByUser(this.task);
        } catch (DAOException | IOException | SAXException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
            return stopwatch.stop(this.stayOnCurrentPage);
        } catch (FileStructureValidationException e) {
            setValidationErrorTitle(Helper.getTranslation("validation.invalidMetadataFile"));
            showValidationExceptionDialog(e, null);
            return stopwatch.stop(this.stayOnCurrentPage);
        }
        return stopwatch.stop(tasksPage);
    }

}
