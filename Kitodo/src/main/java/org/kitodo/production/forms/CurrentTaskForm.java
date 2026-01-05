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
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.export.ExportDms;
import org.kitodo.export.TiffHeader;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.filters.FilterMenu;
import org.kitodo.production.helper.CustomListColumnInitializer;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.model.LazyTaskModel;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.kitodo.production.thread.TaskImageGeneratorThread;
import org.kitodo.utils.Stopwatch;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.xml.sax.SAXException;

@Named("CurrentTaskForm")
@SessionScoped
public class CurrentTaskForm extends ValidatableForm {
    private static final Logger logger = LogManager.getLogger(CurrentTaskForm.class);
    private Process myProcess = new Process();
    private Task currentTask = new Task();
    private List<Task> selectedTasks = new ArrayList<>();
    private final WebDav myDav = new WebDav();
    private String scriptPath;
    private transient BatchTaskHelper batchHelper;
    private final WorkflowControllerService workflowControllerService = new WorkflowControllerService();
    private List<Property> properties;
    private Property property;
    private final String tasksPath = MessageFormat.format(REDIRECT_PATH, "tasks");
    private final String taskEditPath = MessageFormat.format(REDIRECT_PATH, "currentTasksEdit");
    private final String taskBatchEditPath = MessageFormat.format(REDIRECT_PATH, "taskBatchEdit");

    private List<String> taskFilters;
    private List<String> selectedTaskFilters;
    private FilterMenu filterMenu = new FilterMenu(this);

    private List<TaskStatus> taskStatus;
    private List<TaskStatus> selectedTaskStatus;

    private static final String AUTOMATIC_TASKS_FILTER = "automaticTasks";
    private static final String CORRECTION_TASKS_FILTER = "correctionTasks";
    private static final String OTHER_USERS_TASKS_FILTER = "otherUsersTasks";

    @Inject
    private CustomListColumnInitializer initializer;

    /**
     * Constructor.
     */
    public CurrentTaskForm() {
        super();
        Stopwatch stopwatch = new Stopwatch(this, "CurrentTaskForm");
        super.setLazyBeanModel(new LazyTaskModel(ServiceManager.getTaskService()));
        stopwatch.stop();
    }

    /**
     * Initialize the list of displayed list columns.
     */
    @PostConstruct
    public void init() {
        final Stopwatch stopwatch = new Stopwatch(this, "init");
        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("task"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("task");

        taskFilters = new LinkedList<>();
        taskFilters.add(AUTOMATIC_TASKS_FILTER);
        taskFilters.add(CORRECTION_TASKS_FILTER);
        taskFilters.add(OTHER_USERS_TASKS_FILTER);

        selectedTaskFilters = new LinkedList<>();
        selectedTaskFilters.add(CORRECTION_TASKS_FILTER);
        selectedTaskFilters.add(OTHER_USERS_TASKS_FILTER);

        taskStatus = new LinkedList<>();
        taskStatus.add(TaskStatus.OPEN);
        taskStatus.add(TaskStatus.INWORK);

        selectedTaskStatus = new LinkedList<>();
        selectedTaskStatus.add(TaskStatus.OPEN);
        selectedTaskStatus.add(TaskStatus.INWORK);

        sortBy = SortMeta.builder().field("title.keyword").order(SortOrder.ASCENDING).build();
        stopwatch.stop();
    }

    /**
     * Take over task by user which calls this method.
     *
     * @return page
     */
    public String takeOverTask() {
        Stopwatch stopwatch = new Stopwatch(this, "takeOverTask");
        if (this.currentTask.getProcessingStatus() != TaskStatus.OPEN) {
            Helper.setErrorMessage("stepInWorkError");
            return stopwatch.stop(this.stayOnCurrentPage);
        } else {
            try {
                if (this.currentTask.isTypeAcceptClose()) {
                    this.workflowControllerService.close(this.currentTask);
                    return stopwatch.stop(tasksPath);
                } else {
                    this.workflowControllerService.assignTaskToUser(this.currentTask);
                    ServiceManager.getTaskService().save(this.currentTask);
                }
            } catch (DAOException | IOException | SAXException | FileStructureValidationException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger,
                    e);
            }
        }
        return stopwatch.stop(taskEditPath + "&id=" + getTaskIdForPath());
    }

    /**
     * Edit task.
     *
     * @return page
     */
    public String editTask() {
        Stopwatch stopwatch = new Stopwatch(this, "editTask");
        return stopwatch.stop(taskEditPath + "&id=" + getTaskIdForPath());
    }

    /**
     * Take over batch of tasks - all tasks assigned to the same batch with the same
     * title.
     *
     * @return page for edit one task, page for edit many or stay on the same page
     */
    public String takeOverBatchTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "takeOverBatchTasks");
        String taskTitle = this.currentTask.getTitle();
        List<Batch> batches = this.currentTask.getProcess().getBatches();

        if (batches.isEmpty()) {
            return stopwatch.stop(takeOverTask());
        } else if (batches.size() == 1) {
            Integer batchId = batches.getFirst().getId();
            List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(taskTitle, batchId);
            if (currentTasksOfBatch.isEmpty()) {
                return stopwatch.stop(this.stayOnCurrentPage);
            } else if (currentTasksOfBatch.size() == 1) {
                return stopwatch.stop(takeOverTask());
            } else {
                for (Task task : currentTasksOfBatch) {
                    processTask(task);
                }

                this.setBatchHelper(new BatchTaskHelper(currentTasksOfBatch));
                return stopwatch.stop(taskBatchEditPath);
            }
        } else {
            Helper.setErrorMessage("multipleBatchesAssigned");
            return stopwatch.stop(this.stayOnCurrentPage);
        }
    }

    /**
     * Update task which are available to take.
     *
     * @param task
     *            which is part of the batch
     */
    private void processTask(Task task) {
        if (task.getProcessingStatus().equals(TaskStatus.OPEN)) {
            task.setProcessingStatus(TaskStatus.INWORK);
            task.setEditType(TaskEditType.MANUAL_MULTI);
            task.setProcessingTime(new Date());
            User user = getUser();
            ServiceManager.getTaskService().replaceProcessingUser(task, user);
            if (Objects.isNull(task.getProcessingBegin())) {
                task.setProcessingBegin(new Date());
            }

            if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                try {
                    URI imagesOrigDirectory = ServiceManager.getProcessService().getImagesOriginDirectory(false,
                        task.getProcess());
                    if (!ServiceManager.getFileService().fileExist(imagesOrigDirectory)) {
                        Helper.setErrorMessage("errorDirectoryNotFound", new Object[] {imagesOrigDirectory });
                    }
                } catch (Exception e) {
                    Helper.setErrorMessage("errorDirectoryRetrieve", new Object[] {"image" }, logger, e);
                }
                task.setProcessingTime(new Date());
                this.myDav.downloadToHome(task.getProcess(), !task.isTypeImagesWrite());
            }
        }

        try {
            ServiceManager.getTaskService().save(task);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Edit batch of tasks - all tasks assigned to the same batch with the same
     * title.
     *
     * @return page for edit one task, page for edit many or stay on the same page
     */
    public String editBatchTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "editBatchTasks");
        String taskTitle = this.currentTask.getTitle();
        List<Batch> batches = this.currentTask.getProcess().getBatches();

        if (batches.isEmpty()) {
            return stopwatch.stop(taskEditPath + "&id=" + getTaskIdForPath());
        } else if (batches.size() == 1) {
            Integer batchId = batches.getFirst().getId();
            List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(taskTitle, batchId);
            if (currentTasksOfBatch.isEmpty()) {
                return stopwatch.stop(this.stayOnCurrentPage);
            } else if (currentTasksOfBatch.size() == 1) {
                return stopwatch.stop(taskEditPath + "&id=" + getTaskIdForPath());
            } else {
                this.setBatchHelper(new BatchTaskHelper(currentTasksOfBatch));
                return stopwatch.stop(taskBatchEditPath);
            }
        } else {
            Helper.setErrorMessage("multipleBatchesAssigned");
            return stopwatch.stop(this.stayOnCurrentPage);
        }
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
            this.workflowControllerService.unassignTaskFromUser(this.currentTask);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
            return stopwatch.stop(this.stayOnCurrentPage);
        }
        return stopwatch.stop(tasksPage);
    }

    /**
     * Close method task called by user action.
     *
     * @return page
     */
    public String closeTaskByUser() {
        Stopwatch stopwatch = new Stopwatch(this, "closeTaskByUser");
        try {
            this.workflowControllerService.closeTaskByUser(this.currentTask);
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

    /**
     * Unlock the current task's process.
     *
     * @return stay on the current page
     */
    public String releaseLock() {
        Stopwatch stopwatch = new Stopwatch(this, "releaseLock");
        MetadataLock.setFree(this.currentTask.getProcess().getId());
        return stopwatch.stop(this.stayOnCurrentPage);
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
        Folder generatorSource = myProcess.getProject().getGeneratorSource();
        List<Folder> contentFolders = currentTask.getContentFolders();
        if (Objects.isNull(generatorSource)) {
            Helper.setErrorMessage("noSourceFolderConfiguredInProject");
            return;
        }
        if (Objects.isNull(contentFolders)) {
            Helper.setErrorMessage("noImageFolderConfiguredInProject");
            return;
        }
        Subfolder sourceFolder = new Subfolder(myProcess, generatorSource);
        if (sourceFolder.listContents().isEmpty()) {
            Helper.setErrorMessage("emptySourceFolder");
        } else {
            List<Subfolder> outputs = SubfolderFactoryService.createAll(myProcess, contentFolders);
            ImageGenerator imageGenerator = new ImageGenerator(sourceFolder, mode, outputs);
            TaskManager.addTask(new TaskImageGeneratorThread(myProcess.getTitle(), imageGenerator));
            Helper.setMessage(messageKey);
        }
    }

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
        Task task = ServiceManager.getTaskService().getById(this.currentTask.getId());
        if (ServiceManager.getTaskService().executeScript(task, this.scriptPath, false)) {
            Helper.setMessageWithoutDescription(
                    Helper.getTranslation("scriptExecutionSuccessful", this.currentTask.getScriptName()));
        } else {
            Helper.setErrorMessagesWithoutDescription(
                    Helper.getTranslation("scriptExecutionError", this.currentTask.getScriptName()));
        }
        stopwatch.stop();
    }

    /**
     * Get current task.
     *
     * @return task
     */
    public Task getCurrentTask() {
        Stopwatch stopwatch = new Stopwatch(this, "getCurrentTask");
        return stopwatch.stop(this.currentTask);
    }

    /**
     * Set current task with edit mode set to empty String.
     *
     * @param task
     *            Object
     */
    public void setCurrentTask(Task task) {
        final Stopwatch stopwatch = new Stopwatch(this.getClass(), task, "setCurrentTask");
        this.currentTask = task;
        this.currentTask.setLocalizedTitle(ServiceManager.getTaskService().getLocalizedTitle(task.getTitle()));
        this.myProcess = this.currentTask.getProcess();
        loadProcessProperties();
        stopwatch.stop();
    }

    /**
     * Set task for given id.
     *
     * @param id
     *            passed as int
     */
    public void setTaskById(int id) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskById", "id", Integer.toString(id));
        loadTaskById(id);
        stopwatch.stop();
    }

    /**
     * Get list of selected Tasks.
     *
     * @return List of selected Tasks
     */
    public List<Task> getSelectedTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedTasks");
        return stopwatch.stop(this.selectedTasks);
    }

    /**
     * Set selected tasks: Set tasks in old list to false and set new list to true.
     *
     * @param selectedTasks
     *            provided by data table
     */
    public void setSelectedTasks(List<Task> selectedTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedTasks", "selectedTasks", Objects.toString(selectedTasks));
        this.selectedTasks = selectedTasks;
        stopwatch.stop();
    }

    /**
     * Downloads.
     */
    public void downloadTiffHeader() throws IOException {
        Stopwatch stopwatch = new Stopwatch(this, "downloadTiffHeader");
        TiffHeader tiff = new TiffHeader(this.currentTask.getProcess());
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
            export.startExport(this.currentTask.getProcess());
        } catch (DAOException e) {
            Helper.setErrorMessage("errorExport", new Object[] {this.currentTask.getProcess().getTitle() }, logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Sets the task status constraint.
     * 
     * @param taskStatus
     *            Status of the tasks to be displayed. If empty, all tasks are
     *            displayed; otherwise, only those in one of the given statuses
     *            are displayed. Must not be {@code null}.
     */
    public void setTaskStatusRestriction(List<TaskStatus> taskStatus) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatusRestriction", "taskStatus", Objects.toString(
            taskStatus));
        ((LazyTaskModel)this.lazyBeanModel).setTaskStatusRestriction(taskStatus);
        stopwatch.stop();
    }

    /**
     * Set shown only tasks owned by currently logged user.
     *
     * @param onlyOwnTasks
     *            as boolean
     */
    public void setOnlyOwnTasks(boolean onlyOwnTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setOnlyOwnTasks", "onlyOwnTasks", Boolean.toString(onlyOwnTasks));
        ((LazyTaskModel)this.lazyBeanModel).setOnlyOwnTasks(onlyOwnTasks);
        stopwatch.stop();
    }

    /**
     * Get task filters.
     *
     * @return task filters
     */
    public List<String> getTaskFilters() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskFilters");
        return stopwatch.stop(this.taskFilters);
    }

    /**
     * Set task filters.
     *
     * @param filters task filters
     */
    public void setTaskFilters(List<String> filters) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskFilters", "filters", Objects.toString(filters));
        this.taskFilters = filters;
        stopwatch.stop();
    }

    /**
     * Get selected task filters.
     *
     * @return selected task filters
     */
    public List<String> getSelectedTaskFilters() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedTaskFilters");
        return stopwatch.stop(this.selectedTaskFilters);
    }

    /**
     * Set selected task filters.
     *
     * @param selectedFilters selected task filters
     */
    public void setSelectedTaskFilters(List<String> selectedFilters) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedTaskFilters", "selectedFilters", Objects.toString(
            selectedFilters));
        this.selectedTaskFilters = selectedFilters;
        stopwatch.stop();
    }

    /**
     * Event listener for task filter changed event.
     */
    public void taskFiltersChanged() {
        final Stopwatch stopwatch = new Stopwatch(this, "taskFiltersChanged");
        this.setShowAutomaticTasks(this.selectedTaskFilters.contains(AUTOMATIC_TASKS_FILTER));
        this.setHideCorrectionTasks(!this.selectedTaskFilters.contains(CORRECTION_TASKS_FILTER));
        this.setOnlyOwnTasks(!this.selectedTaskFilters.contains(OTHER_USERS_TASKS_FILTER));
        stopwatch.stop();
    }

    /**
     * Get task status.
     *
     * @return task status
     */
    public List<TaskStatus> getTaskStatus() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskStatus");
        return stopwatch.stop(this.taskStatus);
    }

    /**
     * Set task status.
     *
     * @param status task status
     */
    public void setTaskStatus(List<TaskStatus> status) {
        Stopwatch stopwatch = new Stopwatch(this, "setTaskStatus", "status", Objects.toString(status));
        this.taskStatus = status;
        stopwatch.stop();
    }

    /**
     * Get selected task status.
     *
     * @return selected task status
     */
    public List<TaskStatus> getSelectedTaskStatus() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedTaskStatus");
        return stopwatch.stop(this.selectedTaskStatus);
    }

    /**
     * Set selected task status.
     *
     * @param selectedStatus selected task status
     */
    public void setSelectedTaskStatus(List<TaskStatus> selectedStatus) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedTaskStatus", "selectedStatus", Objects.toString(
            selectedStatus));
        this.selectedTaskStatus = selectedStatus;
        stopwatch.stop();
    }

    /**
     * Event listener for task status changed event.
     */
    public void taskStatusChanged() {
        Stopwatch stopwatch = new Stopwatch(this, "taskStatusChanged");
        this.setTaskStatusRestriction(this.selectedTaskStatus);
        stopwatch.stop();
    }

    /**
     * Checks if the task type is "generateImages" and thus the generate images links are shown.
     *
     * @return whether action links should be displayed
     */
    public boolean isShowingGenerationActions() {
        Stopwatch stopwatch = new Stopwatch(this, "isShowingGenerationActions");
        return stopwatch.stop(currentTask.isTypeGenerateImages());
    }

    /**
     * Checks if folders for generation are configured in the project.
     * @return whether the folders are configured.
     */
    public boolean isImageGenerationPossible() {
        Stopwatch stopwatch = new Stopwatch(this, "isImageGenerationPossible");
        return stopwatch.stop(TaskService.generatableFoldersFromProjects(Stream.of(currentTask.getProcess()
                .getProject())).findAny().isPresent());
    }

    /**
     * Checks if the task type is "validateImages" and thus the task action link is shown.
     *
     * @return whether action link for validating images should be displayed
     */
    public boolean isShowingImageValidationAction() {
        return currentTask.isTypeValidateImages();
    }

    /**
     * Checks if any folders are configured to contain images that need to be validated.
     * 
     * @return whether there are folders with images that are supposed to validated.
     */
    public boolean isImageValidationPossible() {
        return !currentTask.getValidationFolders().isEmpty();
    }


    /**
     * Set show automatic tasks.
     *
     * @param showAutomaticTasks
     *            as boolean
     */
    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowAutomaticTasks");
        ((LazyTaskModel)this.lazyBeanModel).setShowAutomaticTasks(showAutomaticTasks);
        stopwatch.stop();
    }

    /**
     * Check if it should hide correction tasks.
     *
     * @return boolean
     */
    public boolean isHideCorrectionTasks() {
        Stopwatch stopwatch = new Stopwatch(this, "isHideCorrectionTasks");
        return stopwatch.stop(((LazyTaskModel) this.lazyBeanModel).isHideCorrectionTasks());
    }

    /**
     * Set hide correction tasks.
     *
     * @param hideCorrectionTasks
     *            as boolean
     */
    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        Stopwatch stopwatch = new Stopwatch(this, "setHideCorrectionTasks", "hideCorrectionTasks", Boolean.toString(
            hideCorrectionTasks));
        ((LazyTaskModel)this.lazyBeanModel).setHideCorrectionTasks(hideCorrectionTasks);
        stopwatch.stop();
    }

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getProperty");
        return stopwatch.stop(this.property);
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        Stopwatch stopwatch = new Stopwatch(this, "setProperty", "property", Objects.toString(property));
        this.property = property;
        stopwatch.stop();
    }

    /**
     * Get list of process properties.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        Stopwatch stopwatch = new Stopwatch(this, "getProperties");
        return stopwatch.stop(this.properties);
    }

    /**
     * Set list of process properties.
     *
     * @param properties
     *            for process as Property objects
     */
    public void setProperties(List<Property> properties) {
        Stopwatch stopwatch = new Stopwatch(this, "setProperties", "properties", Objects.toString(properties));
        this.properties = properties;
        stopwatch.stop();
    }

    private void loadProcessProperties() {
        setProperties(this.myProcess.getProperties());
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveCurrentProperty");
        try {
            ServiceManager.getPropertyService().save(this.property);
            if (!this.myProcess.getProperties().contains(this.property)) {
                this.myProcess.getProperties().add(this.property);
            }
            ServiceManager.getProcessService().save(this.myProcess);
            Helper.setMessage("propertiesSaved");
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger, e);
        }
        loadProcessProperties();
        stopwatch.stop();
    }

    /**
     * Duplicate property.
     */
    public void duplicateProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "duplicateProperty");
        Property newProperty = ServiceManager.getPropertyService().transfer(this.property);
        try {
            newProperty.getProcesses().add(this.myProcess);
            this.myProcess.getProperties().add(newProperty);
            ServiceManager.getPropertyService().save(newProperty);
            Helper.setMessage("propertySaved");
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger, e);
        }
        loadProcessProperties();
        stopwatch.stop();
    }

    /**
     * Get batch helper.
     *
     * @return batch helper as BatchHelper object
     */
    public BatchTaskHelper getBatchHelper() {
        Stopwatch stopwatch = new Stopwatch(this, "getBatchHelper");
        return stopwatch.stop(this.batchHelper);
    }

    /**
     * Set batch helper.
     *
     * @param batchHelper
     *            as BatchHelper object
     */
    public void setBatchHelper(BatchTaskHelper batchHelper) {
        Stopwatch stopwatch = new Stopwatch(this, "setBatchHelper", "batchHelper", Objects.toString(batchHelper));
        this.batchHelper = batchHelper;
        stopwatch.stop();
    }

    /**
     * Method being used as viewAction for CurrentTaskForm.
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
     * Get taskListPath.
     *
     * @return value of taskListPath
     */
    public String getTaskListPath() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskListPath");
        return stopwatch.stop(tasksPage);
    }

    private int getTaskIdForPath() {
        return Objects.isNull(this.currentTask.getId()) ? 0 : this.currentTask.getId();
    }

    /**
     * Return array of task custom column names.
     * @return array of task custom column names
     */
    public String[] getTaskCustomColumnNames() {
        Stopwatch stopwatch = new Stopwatch(this, "getTaskCustomColumnNames");
        return stopwatch.stop(initializer.getTaskProcessProperties());
    }

    /**
     * Retrieve and return process property value of property with given name 'propertyName' from process of given
     * Task 'task'.
     * @param task the Task object from which the property value is retrieved
     * @param propertyName name of the property for the property value is retrieved
     * @return property value if process has property with name 'propertyName', empty String otherwise
     */
    public static String getTaskProcessPropertyValue(Task task, String propertyName) {
        Stopwatch stopwatch = new Stopwatch(CurrentTaskForm.class, task, "getTaskProcessPropertyValue", "propertyName",
                propertyName);
        return stopwatch.stop(ProcessService.getPropertyValue(task.getProcess(), propertyName));
    }

    /**
     * Get the id of the template task corresponding to the given task.
     * The corresponding template task was the blueprint when creating the given task.
     * @param task task to find the corresponding template task for
     * @return id of the template task or -1 if no matching task could be found
     */
    public static int getCorrespondingTemplateTaskId(Task task) {
        Stopwatch stopwatch = new Stopwatch(CurrentTaskForm.class, task, "getCorrespondingTemplateTaskId");
        return stopwatch.stop(TaskService.getCorrespondingTemplateTaskId(task));
    }

    /**
     * Calculate and return age of given tasks process as a String.
     *
     * @param task Task object whose process is used
     * @return process age of given tasks process
     */
    public String getProcessDuration(Task task) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), task, "getProcessDuration");
        return stopwatch.stop(ProcessService.getProcessDuration(task.getProcess()));
    }

    /**
     * Changes the filter of the CurrentTaskForm and reloads it.
     *
     * @param filter
     *            the filter to apply
     * @return path of the page
     */
    public String changeFilter(String filter) {
        Stopwatch stopwatch = new Stopwatch(this, "changeFilter", "filter", filter);
        filterMenu.parseFilters(filter);
        setFilter(filter);
        return stopwatch.stop(filterList());
    }

    private String filterList() {
        this.selectedTasks.clear();
        return tasksPage;
    }

    @Override
    public void setFilter(String filter) {
        Stopwatch stopwatch = new Stopwatch(this, "setFilter", "filter", filter);
        super.filter = filter;
        this.lazyBeanModel.setFilterString(filter);
        stopwatch.stop();
    }

    /**
     * Download to home for single process. First check if this volume is currently
     * being edited by another user and placed in his home directory, otherwise
     * download.
     */
    public void downloadToHome(int processId) {
        Stopwatch stopwatch = new Stopwatch(this, "downloadToHome", "processId", Integer.toString(processId));
        try {
            ProcessService.downloadToHome(new WebDav(), processId);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error downloading process " + processId + " to home directory!");
        }
        stopwatch.stop();
    }

    /**
     * Get filterMenu.
     *
     * @return value of filterMenu
     */
    public FilterMenu getFilterMenu() {
        Stopwatch stopwatch = new Stopwatch(this, "getFilterMenu");
        return stopwatch.stop(filterMenu);
    }

    /**
     * Determine whether the last comment should be displayed in the comments column.
     *
     * @return boolean
     */
    public boolean showLastComment() {
        Stopwatch stopwatch = new Stopwatch(this, "showLastComment");
        return stopwatch.stop(ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.SHOW_LAST_COMMENT));
    }

}
