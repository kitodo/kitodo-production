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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ExportFileException;
import org.kitodo.export.ExportDms;
import org.kitodo.export.TiffHeader;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.thread.TaskImageGeneratorThread;
import org.kitodo.production.workflow.Problem;
import org.kitodo.production.workflow.Solution;

@Named("CurrentTaskForm")
@SessionScoped
public class CurrentTaskForm extends BaseForm {
    private static final long serialVersionUID = 5841566727939692509L;
    private static final Logger logger = LogManager.getLogger(CurrentTaskForm.class);
    private Process myProcess = new Process();
    private Task currentTask = new Task();
    private transient Problem problem = new Problem();
    private transient Solution solution = new Solution();
    private transient List<TaskDTO> selectedTasks;
    private final WebDav myDav = new WebDav();
    private int gesamtAnzahlImages = 0;
    private boolean onlyOpenTasks = false;
    private boolean onlyOwnTasks = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;
    private Map<String, Boolean> anzeigeAnpassen;
    private String scriptPath;
    private String addToWikiField = "";
    private String doneDirectoryName;
    private transient BatchTaskHelper batchHelper;
    private List<Property> properties;
    private Property property;
    private String taskListPath = MessageFormat.format(REDIRECT_PATH, "tasks");
    private String taskEditPath = MessageFormat.format(REDIRECT_PATH, "currentTasksEdit");
    private String taskBatchEditPath = MessageFormat.format(REDIRECT_PATH, "taskBatchEdit");

    /**
     * Constructor.
     */
    public CurrentTaskForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getTaskService()));
        this.anzeigeAnpassen = new HashMap<>();
        this.anzeigeAnpassen.put("lockings", false);
        this.anzeigeAnpassen.put("selectionBoxes", false);
        this.anzeigeAnpassen.put("processId", false);
        this.anzeigeAnpassen.put("modules", false);
        this.anzeigeAnpassen.put("batchId", false);
        /*
         * Vorgangsdatum generell anzeigen?
         */
        User user = getUser();
        if (user != null) {
            this.anzeigeAnpassen.put("processDate", user.isConfigProductionDateShow());
        } else {
            this.anzeigeAnpassen.put("processDate", false);
        }
        doneDirectoryName = ConfigCore.getParameterOrDefaultValue(ParameterCore.DONE_DIRECTORY_NAME);
    }

    /**
     * Take over task by user which calls this method.
     *
     * @return page
     */
    public String takeOverTask() {
        if (this.currentTask.getProcessingStatusEnum() != TaskStatus.OPEN) {
            Helper.setErrorMessage("stepInWorkError");
            return this.stayOnCurrentPage;
        } else {
            ServiceManager.getWorkflowControllerService().assignTaskToUser(this.currentTask);
            try {
                ServiceManager.getTaskService().save(this.currentTask);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger,
                    e);
            }
        }
        return taskEditPath + "&id=" + getTaskIdForPath();
    }

    /**
     * Edit task.
     *
     * @return page
     */
    public String editTask() {
        ServiceManager.getTaskService().refresh(this.currentTask);
        return taskEditPath + "&id=" + getTaskIdForPath();
    }

    /**
     * Take over batch of tasks - all tasks assigned to the same batch with the same
     * title.
     *
     * @return page for edit one task, page for edit many or stay on the same page
     */
    public String takeOverBatchTasks() {
        String taskTitle = this.currentTask.getTitle();
        List<Batch> batches = ServiceManager.getProcessService().getBatchesByType(this.currentTask.getProcess(),
            Type.LOGISTIC);

        if (batches.isEmpty()) {
            return takeOverTask();
        } else if (batches.size() == 1) {
            Integer batchId = batches.get(0).getId();
            List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(taskTitle, batchId);
            if (currentTasksOfBatch.isEmpty()) {
                return this.stayOnCurrentPage;
            } else if (currentTasksOfBatch.size() == 1) {
                return takeOverTask();
            } else {
                for (Task task : currentTasksOfBatch) {
                    processTask(task);
                }

                this.setBatchHelper(new BatchTaskHelper(currentTasksOfBatch));
                return taskBatchEditPath;
            }
        } else {
            Helper.setErrorMessage("multipleBatchesAssigned");
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Update task which are available to take.
     *
     * @param task
     *            which is part of the batch
     */
    private void processTask(Task task) {
        if (task.getProcessingStatusEnum().equals(TaskStatus.OPEN)) {
            task.setProcessingStatusEnum(TaskStatus.INWORK);
            task.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
            task.setProcessingTime(new Date());
            User user = getUser();
            ServiceManager.getTaskService().replaceProcessingUser(task, user);
            if (task.getProcessingBegin() == null) {
                task.setProcessingBegin(new Date());
            }

            if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                try {
                    URI imagesOrigDirectory = ServiceManager.getProcessService().getImagesOriginDirectory(false,
                        task.getProcess());
                    if (!ServiceManager.getFileService().fileExist(imagesOrigDirectory)) {
                        Helper.setErrorMessage("Directory doesn't exists!", new Object[] {imagesOrigDirectory });
                    }
                } catch (Exception e) {
                    Helper.setErrorMessage("Error retrieving image directory: ", logger, e);
                }
                task.setProcessingTime(new Date());
                this.myDav.downloadToHome(task.getProcess(), !task.isTypeImagesWrite());
            }
        }

        try {
            ServiceManager.getTaskService().save(task);
        } catch (DataException e) {
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
        String taskTitle = this.currentTask.getTitle();
        List<Batch> batches = ServiceManager.getProcessService().getBatchesByType(this.currentTask.getProcess(),
            Type.LOGISTIC);

        if (batches.isEmpty()) {
            return taskEditPath + "&id=" + getTaskIdForPath();
        } else if (batches.size() == 1) {
            Integer batchId = batches.get(0).getId();
            List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(taskTitle, batchId);
            if (currentTasksOfBatch.isEmpty()) {
                return this.stayOnCurrentPage;
            } else if (currentTasksOfBatch.size() == 1) {
                return taskEditPath + "&id=" + getTaskIdForPath();
            } else {
                this.setBatchHelper(new BatchTaskHelper(currentTasksOfBatch));
                return taskBatchEditPath;
            }
        } else {
            Helper.setErrorMessage("multipleBatchesAssigned");
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Release task - set up task status to open and make available for other users
     * to take over.
     *
     * @return page
     */
    public String releaseTask() {
        try {
            ServiceManager.getWorkflowControllerService().unassignTaskFromUser(this.currentTask);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
        return taskListPath;
    }

    /**
     * Close method task called by user action.
     *
     * @return page
     */
    public String closeTaskByUser() {
        try {
            ServiceManager.getWorkflowControllerService().closeTaskByUser(this.currentTask);
        } catch (DataException | IOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
        return taskListPath;
    }

    /**
     * Unlock the current task's process.
     * 
     * @return stay on the current page
     */
    public String releaseLock() {
        MetadataLock.unlockProcess(this.currentTask.getProcess().getId());
        this.currentTask.getProcess().setBlockedUser(null);
        this.currentTask.getProcess().setBlockedMinutes(0);
        this.currentTask.getProcess().setBlockedSeconds(0);
        return this.stayOnCurrentPage;
    }

    /**
     * Korrekturmeldung an vorherige Schritte.
     */
    public List<Task> getPreviousStepsForProblemReporting() {
        return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(this.currentTask.getOrdering(),
            this.currentTask.getProcess().getId());
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    /**
     * Report the problem.
     *
     * @return problem as String
     */
    public String reportProblem() {
        ServiceManager.getWorkflowControllerService().setProblem(getProblem());
        try {
            ServiceManager.getWorkflowControllerService().reportProblem(this.currentTask);
        } catch (DAOException | DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
        setProblem(ServiceManager.getWorkflowControllerService().getProblem());
        return taskListPath;
    }

    /**
     * Problem-behoben-Meldung an nachfolgende Schritte.
     */
    public List<Task> getNextStepsForProblemSolution() {
        return ServiceManager.getTaskService().getNextTasksForProblemSolution(this.currentTask.getOrdering(),
            this.currentTask.getProcess().getId());
    }

    public int getSizeOfNextStepsForProblemSolution() {
        return getNextStepsForProblemSolution().size();
    }

    /**
     * Solve problem.
     *
     * @return String
     */
    public String solveProblem() {
        ServiceManager.getWorkflowControllerService().setSolution(getSolution());
        try {
            ServiceManager.getWorkflowControllerService().solveProblem(this.currentTask);
        } catch (DAOException | DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.TASK.getTranslationSingular() }, logger, e);
        }
        setSolution(ServiceManager.getWorkflowControllerService().getSolution());
        return taskListPath;
    }

    /**
     * Upload from home.
     *
     * @return String
     */
    @SuppressWarnings("unchecked")
    public String uploadFromHomeAlle() {
        List<URI> readyList = this.myDav.uploadAllFromHome(doneDirectoryName);
        List<URI> checkedList = new ArrayList<>();

        // go through the uploaded process IDs and set to complete
        if (!readyList.isEmpty() && this.onlyOpenTasks) {
            this.onlyOpenTasks = false;
            return taskListPath;
        }
        for (URI element : readyList) {
            String id = element.toString()
                    .substring(element.toString().indexOf('[') + 1, element.toString().indexOf(']')).trim();

            for (Task task : (List<Task>) lazyDTOModel.getEntities()) {
                // only when the task is already in edit mode, complete it
                if (task.getProcess().getId() == Integer.parseInt(id)
                        && task.getProcessingStatusEnum() == TaskStatus.INWORK) {
                    this.currentTask = task;
                    if (Objects.nonNull(closeTaskByUser())) {
                        checkedList.add(element);
                    }
                    this.currentTask.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                }
            }
        }

        this.myDav.removeAllFromHome(checkedList, URI.create(doneDirectoryName));
        Helper.setMessage("removed " + checkedList.size() + " directories from user home:", doneDirectoryName);
        return this.stayOnCurrentPage;
    }

    /**
     * Download to home page.
     *
     * @return String
     */
    public String downloadToHomePage() {
        download();
        // calcHomeImages();
        Helper.setMessage("Created directories in user home");
        return this.stayOnCurrentPage;
    }

    /**
     * Download to home.
     *
     * @return String
     */
    public String downloadToHomeHits() {
        download();
        // calcHomeImages();
        Helper.setMessage("Created directories in user home");
        return this.stayOnCurrentPage;
    }

    @SuppressWarnings("unchecked")
    private void download() {
        for (TaskDTO taskDTO : (List<TaskDTO>) lazyDTOModel.getEntities()) {
            Task task = new Task();
            try {
                task = ServiceManager.getTaskService().getById(taskDTO.getId());
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.TASK.getTranslationSingular(), taskDTO.getId() }, logger, e);
            }
            if (task.getProcessingStatusEnum() == TaskStatus.OPEN) {
                task.setProcessingStatusEnum(TaskStatus.INWORK);
                task.setEditTypeEnum(TaskEditType.MANUAL_MULTI);
                task.setProcessingTime(new Date());
                User user = getUser();
                ServiceManager.getTaskService().replaceProcessingUser(task, user);
                task.setProcessingBegin(new Date());
                Process process = task.getProcess();
                try {
                    ServiceManager.getProcessService().save(process);
                } catch (DataException e) {
                    Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                        logger, e);
                }
                this.myDav.downloadToHome(process, false);
            }
        }
    }

    /**
     * Generate all images.
     */
    public void generateAllImages() {
        generateImages(GenerationMode.ALL, "regenerateAllImagesStarted");
    }

    /**
     * Generate missing and damaged images.
     */
    public void generateMissingAndDamagedImages() {
        generateImages(GenerationMode.MISSING_OR_DAMAGED, "regenerateMissingAndDamagedImagesStarted");
    }

    /**
     * Generate missing images.
     */
    public void generateMissingImages() {
        generateImages(GenerationMode.MISSING, "regenerateMissingImagesStarted");
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
        try {
            Subfolder sourceFolder = new Subfolder(myProcess, myProcess.getProject().getGeneratorSource());
            List<Subfolder> outputs = SubfolderFactoryService.createAll(myProcess, currentTask.getContentFolders());
            ImageGenerator imageGenerator = new ImageGenerator(sourceFolder, mode, outputs);
            TaskManager.addTask(new TaskImageGeneratorThread(myProcess.getTitle(), imageGenerator));
            Helper.setMessage(messageKey);
        } catch (RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    public String getScriptPath() {
        return this.scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    /**
     * Execute script.
     */
    public void executeScript() throws DAOException, DataException {
        Task task = ServiceManager.getTaskService().getById(this.currentTask.getId());
        ServiceManager.getTaskService().executeScript(task, this.scriptPath, false);
    }

    public int getAllImages() {
        return this.gesamtAnzahlImages;
    }

    /**
     * Calc home images.
     */
    @SuppressWarnings("unchecked")
    public void calcHomeImages() {
        this.gesamtAnzahlImages = 0;
        User user = getUser();
        if (user != null && user.isWithMassDownload()) {
            for (TaskDTO taskDTO : (List<TaskDTO>) lazyDTOModel.getEntities()) {
                try {
                    Task task = ServiceManager.getTaskService().getById(taskDTO.getId());
                    if (task.getProcessingStatusEnum() == TaskStatus.OPEN) {
                        this.gesamtAnzahlImages += ServiceManager.getFileService()
                                .getSubUris(
                                    ServiceManager.getProcessService().getImagesOriginDirectory(false, task.getProcess()))
                                .size();
                    }
                } catch (DAOException | IOException e) {
                    Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                }
            }
        }
    }

    /**
     * Get current task.
     *
     * @return task
     */
    public Task getCurrentTask() {
        return this.currentTask;
    }

    /**
     * Set current task with edit mode set to empty String.
     *
     * @param task
     *            Object
     */
    public void setCurrentTask(Task task) {
        this.currentTask = task;
        this.currentTask.setLocalizedTitle(ServiceManager.getTaskService().getLocalizedTitle(task.getTitle()));
        this.myProcess = this.currentTask.getProcess();
        loadProcessProperties();
        setAttributesForProcess();
    }

    /**
     * Set task for given id.
     *
     * @param id
     *            passed as int
     */
    public void setTaskById(int id) {
        loadTaskById(id);
    }

    /**
     * Get problem.
     *
     * @return Problem object
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Set problem.
     *
     * @param problem
     *            object
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    /**
     * Get solution.
     *
     * @return Solution object
     */
    public Solution getSolution() {
        return solution;
    }

    /**
     * Set solution.
     *
     * @param solution
     *            object
     */
    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    private void setAttributesForProcess() {
        Process process = this.currentTask.getProcess();
        process.setBlockedUser(ServiceManager.getProcessService().getBlockedUser(process));
        process.setBlockedMinutes(ServiceManager.getProcessService().getBlockedMinutes(process));
        process.setBlockedSeconds(ServiceManager.getProcessService().getBlockedSeconds(process));
    }

    /**
     * Get list of selected Tasks.
     *
     * @return List of selected Tasks
     */
    public List<TaskDTO> getSelectedTasks() {
        return this.selectedTasks;
    }

    /**
     * Set selected tasks: Set tasks in old list to false and set new list to true.
     *
     * @param selectedTasks
     *            provided by data table
     */
    public void setSelectedTasks(List<TaskDTO> selectedTasks) {
        if (this.selectedTasks != null && !this.selectedTasks.isEmpty()) {
            for (TaskDTO task : this.selectedTasks) {
                task.setSelected(false);
            }
        }
        for (TaskDTO task : selectedTasks) {
            task.setSelected(true);
        }
        this.selectedTasks = selectedTasks;
    }

    /**
     * Downloads.
     */
    public void downloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.currentTask.getProcess());
        tiff.exportStart();
    }

    /**
     * Export DMS.
     */
    public void exportDMS() {
        ExportDms export = new ExportDms();
        try {
            export.startExport(this.currentTask.getProcess());
        } catch (ReadException | PreferencesException | WriteException | MetadataTypeNotAllowedException | IOException
                | ExportFileException | RuntimeException | JAXBException e) {
            Helper.setErrorMessage("errorExport", new Object[] {this.currentTask.getProcess().getTitle() }, logger, e);
        }
    }

    /**
     * Check if it should show only open tasks.
     * 
     * @return boolean
     */
    public boolean isOnlyOpenTasks() {
        return this.onlyOpenTasks;
    }

    /**
     * Set shown only open tasks.
     *
     * @param onlyOpenTasks
     *            as boolean
     */
    public void setOnlyOpenTasks(boolean onlyOpenTasks) {
        this.onlyOpenTasks = onlyOpenTasks;
        ServiceManager.getTaskService().setOnlyOpenTasks(this.onlyOpenTasks);
    }

    /**
     * Check if it should show only own tasks.
     * 
     * @return boolean
     */
    public boolean isOnlyOwnTasks() {
        return this.onlyOwnTasks;
    }

    /**
     * Set shown only tasks owned by currently logged user.
     *
     * @param onlyOwnTasks
     *            as boolean
     */
    public void setOnlyOwnTasks(boolean onlyOwnTasks) {
        this.onlyOwnTasks = onlyOwnTasks;
        ServiceManager.getTaskService().setOnlyOwnTasks(this.onlyOwnTasks);
    }

    /**
     * Check if it should show also automatic tasks.
     * 
     * @return boolean
     */
    public boolean isShowAutomaticTasks() {
        return this.showAutomaticTasks;
    }

    /**
     * Using this helper variable, JSF can check if there is content to generate in
     * the current task. In this case, corresponding action links are rendered,
     * otherwise not.
     * 
     * @return whether action links should be displayed
     */
    public boolean isShowingGenerationActions() {
        return TaskService.generatableFoldersFromProjects(Arrays.asList(currentTask.getProcess().getProject()).stream())
                .findAny().isPresent();
    }

    /**
     * Set show automatic tasks.
     *
     * @param showAutomaticTasks
     *            as boolean
     */
    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        this.showAutomaticTasks = showAutomaticTasks;
        ServiceManager.getTaskService().setShowAutomaticTasks(showAutomaticTasks);
    }

    /**
     * Check if it should hide correction tasks.
     * 
     * @return boolean
     */
    public boolean isHideCorrectionTasks() {
        return hideCorrectionTasks;
    }

    /**
     * Set hide correction tasks.
     *
     * @param hideCorrectionTasks
     *            as boolean
     */
    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        this.hideCorrectionTasks = hideCorrectionTasks;
        ServiceManager.getTaskService().setHideCorrectionTasks(this.hideCorrectionTasks);
    }

    public Map<String, Boolean> getAnzeigeAnpassen() {
        return this.anzeigeAnpassen;
    }

    public void setAnzeigeAnpassen(Map<String, Boolean> anzeigeAnpassen) {
        this.anzeigeAnpassen = anzeigeAnpassen;
    }

    /**
     * Get Wiki field.
     *
     * @return values for wiki field
     */
    public String getWikiField() {
        if (Objects.nonNull(this.currentTask) && Objects.nonNull(this.currentTask.getProcess())) {
            return this.currentTask.getProcess().getWikiField();
        }
        return "";
    }

    /**
     * Sets new value for wiki field.
     *
     * @param inString
     *            input String
     */
    public void setWikiField(String inString) {
        this.currentTask.getProcess().setWikiField(inString);
    }

    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    /**
     * Add to wiki field.
     */
    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            this.currentTask.setProcess(
                ServiceManager.getProcessService().addToWikiField(this.addToWikiField, this.currentTask.getProcess()));
            this.addToWikiField = "";
            try {
                ServiceManager.getProcessService().save(this.currentTask.getProcess());
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * Get list of process properties.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        return this.properties;
    }

    /**
     * Set list of process properties.
     *
     * @param properties
     *            for process as Property objects
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Get size of properties' list.
     *
     * @return size of properties' list
     */
    public int getPropertiesSize() {
        return this.properties.size();
    }

    private void loadProcessProperties() {
        ServiceManager.getProcessService().refresh(this.myProcess);
        setProperties(this.myProcess.getProperties());
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        try {
            ServiceManager.getPropertyService().save(this.property);
            if (!this.myProcess.getProperties().contains(this.property)) {
                this.myProcess.getProperties().add(this.property);
            }
            ServiceManager.getProcessService().save(this.myProcess);
            Helper.setMessage("propertiesSaved");
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger, e);
        }
        loadProcessProperties();
    }

    /**
     * Duplicate property.
     */
    public void duplicateProperty() {
        Property newProperty = ServiceManager.getPropertyService().transfer(this.property);
        try {
            newProperty.getProcesses().add(this.myProcess);
            this.myProcess.getProperties().add(newProperty);
            ServiceManager.getPropertyService().save(newProperty);
            Helper.setMessage("propertySaved");
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.PROPERTY.getTranslationPlural() }, logger, e);
        }
        loadProcessProperties();
    }

    /**
     * Get batch helper.
     *
     * @return batch helper as BatchHelper object
     */
    public BatchTaskHelper getBatchHelper() {
        return this.batchHelper;
    }

    /**
     * Set batch helper.
     *
     * @param batchHelper
     *            as BatchHelper object
     */
    public void setBatchHelper(BatchTaskHelper batchHelper) {
        this.batchHelper = batchHelper;
    }

    /**
     * Method being used as viewAction for CurrentTaskForm.
     *
     * @param id
     *            ID of the task to load
     */
    public void loadTaskById(int id) {
        try {
            setCurrentTask(ServiceManager.getTaskService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Retrieve and return the list of tasks that are assigned to the user that are
     * currently in progress.
     *
     * @return list of tasks that are currently assigned to the user that are
     *         currently in progress.
     */
    public List<Task> getTasksInProgress() {
        return ServiceManager.getUserService().getTasksInProgress(this.user);
    }

    /**
     * Get taskListPath.
     *
     * @return value of taskListPath
     */
    public String getTaskListPath() {
        return taskListPath;
    }

    private int getTaskIdForPath() {
        return Objects.isNull(this.currentTask.getId()) ? 0 : this.currentTask.getId();
    }
}
