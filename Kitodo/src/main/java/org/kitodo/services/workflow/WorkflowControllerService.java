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

package org.kitodo.services.workflow;

import de.sub.goobi.metadaten.MetadataLock;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.helper.Helper;
import org.kitodo.helper.WebDav;
import org.kitodo.helper.metadata.ImagesHelper;
import org.kitodo.helper.tasks.TaskManager;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.TaskService;
import org.kitodo.workflow.Problem;
import org.kitodo.workflow.Solution;

public class WorkflowControllerService {

    private final MetadataLock metadataLock = new MetadataLock();
    private List<Task> automaticTasks;
    private List<Task> tasksToFinish;
    private Problem problem = new Problem();
    private Solution solution = new Solution();
    private Boolean flagWait = false;
    private final ReentrantLock flagWaitLock = new ReentrantLock();
    private final WebDav webDav = new WebDav();
    private static final Logger logger = LogManager.getLogger(WorkflowControllerService.class);
    private static WorkflowControllerService instance = null;
    private ServiceManager serviceManager = new ServiceManager();
    private TaskService taskService = serviceManager.getTaskService();

    /**
     * Return singleton variable of type TaskService.
     *
     * @return unique instance of TaskService
     */
    public static WorkflowControllerService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (WorkflowControllerService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new WorkflowControllerService();
                }
            }
        }
        return instance;
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

    /**
     * Set Task status up.
     *
     * @param task
     *            to change status up
     */
    public void setTaskStatusUp(Task task) throws DataException, IOException {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            setProcessingStatusUp(task);
            task.setEditTypeEnum(TaskEditType.ADMIN);
            if (task.getProcessingStatusEnum() == TaskStatus.DONE) {
                close(task);
            } else {
                task.setProcessingTime(new Date());
                taskService.replaceProcessingUser(task, getCurrentUser());
            }
        }
    }

    /**
     * Change Task status down.
     *
     * @param task
     *            to change status down
     */
    public void setTaskStatusDown(Task task) {
        task.setEditTypeEnum(TaskEditType.ADMIN);
        task.setProcessingTime(new Date());
        taskService.replaceProcessingUser(task, getCurrentUser());
        setProcessingStatusDown(task);
    }

    /**
     * Change Task status up for list of tasks assigned to given Process.
     *
     * @param process
     *            object
     */
    public void setTasksStatusUp(Process process) throws DataException, IOException {
        List<Task> tasks = new CopyOnWriteArrayList<>(process.getTasks());

        for (Task task : tasks) {
            setTaskStatusUp(task);
        }
    }

    /**
     * Change Task status down for list of tasks assigned to given Process.
     *
     * @param process
     *            object
     */
    public void setTasksStatusDown(Process process) throws DataException {
        List<Task> tasks = new CopyOnWriteArrayList<>(process.getTasks());
        Collections.reverse(tasks);

        for (Task task : tasks) {
            // TODO: check if this behaviour is correct
            if (process.getTasks().get(0) != task && task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
                setTaskStatusDown(task);
                taskService.save(task);
                break;
            }
        }
    }

    /**
     * Close method task called by user action.
     *
     * @param task
     *            object
     */
    public void closeTaskByUser(Task task) throws DataException, IOException {
        // if the result of the task is to be verified first, then if necessary,
        // cancel the completion
        if (task.isTypeCloseVerify()) {
            // metadata validation
            if (task.isTypeMetadata()
                    && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.USE_META_DATA_VALIDATION)) {
                serviceManager.getMetadataValidationService().setAutoSave(true);
                if (!serviceManager.getMetadataValidationService().validate(task.getProcess())) {
                    return;
                }
            }

            // image validation
            if (task.isTypeImagesWrite()) {
                ImagesHelper mih = new ImagesHelper(null, null);
                URI imageFolder = serviceManager.getProcessService().getImagesOrigDirectory(false, task.getProcess());
                if (!mih.checkIfImagesValid(task.getProcess().getTitle(), imageFolder)) {
                    Helper.setErrorMessage("Error on image validation!");
                    return;
                }
            }
        }

        // unlock the process
        metadataLock.setFree(task.getProcess().getId());

        // if the result of the verification is ok, then continue, otherwise it
        // is not reached
        this.webDav.uploadFromHome(task.getProcess());
        task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        close(task);
    }

    /**
     * Close task.
     *
     * @param task
     *            as Task object
     */
    public void close(Task task) throws DataException, IOException {
        task.setProcessingStatus(3);
        task.setProcessingTime(new Date());
        taskService.replaceProcessingUser(task, getCurrentUser());
        task.setProcessingEnd(new Date());

        taskService.save(task);

        automaticTasks = new ArrayList<>();
        tasksToFinish = new ArrayList<>();

        // check if there are tasks that take place in parallel but are not yet
        // completed
        List<Task> tasks = task.getProcess().getTasks();
        List<Task> concurrentTasksForOpen = getConcurrentTasksForOpen(tasks, task);

        if (concurrentTasksForOpen.isEmpty() && !isAnotherTaskInWorkWhichBlocksOtherTasks(tasks, task)) {
            if (!task.isLast()) {
                activateNextTasks(getAllHigherTasks(tasks, task));
            }
        } else {
            activateConcurrentTasks(concurrentTasksForOpen);
        }

        Process process = task.getProcess();
        URI imagesOrigDirectory = serviceManager.getProcessService().getImagesOrigDirectory(true, process);
        Integer numberOfFiles = serviceManager.getFileService().getNumberOfFiles(imagesOrigDirectory);
        if (!process.getSortHelperImages().equals(numberOfFiles)) {
            process.setSortHelperImages(numberOfFiles);
            serviceManager.getProcessService().save(process);
        }

        updateProcessSortHelperStatus(process);

        for (Task automaticTask : automaticTasks) {
            TaskScriptThread thread = new TaskScriptThread(automaticTask);
            TaskManager.addTask(thread);
        }
        for (Task finish : tasksToFinish) {
            close(finish);
        }
    }

    /**
     * Taken from CurrentTaskForm.
     *
     * @param task
     *            object
     */
    public void assignTaskToUser(Task task) {
        this.flagWaitLock.lock();
        try {
            if (!this.flagWait) {
                this.flagWait = true;

                task.setProcessingStatusEnum(TaskStatus.INWORK);
                task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
                task.setProcessingTime(new Date());
                taskService.replaceProcessingUser(task, getCurrentUser());
                if (task.getProcessingBegin() == null) {
                    task.setProcessingBegin(new Date());
                }

                Process process = task.getProcess();

                List<Task> concurrentTasks = getConcurrentTasksForOpen(process.getTasks(), task);

                if (!concurrentTasks.isEmpty()) {
                    for (Task concurrentTask : concurrentTasks) {
                        concurrentTask.setProcessingStatusEnum(TaskStatus.LOCKED);
                        serviceManager.getTaskService().save(concurrentTask);
                    }
                }

                updateProcessSortHelperStatus(process);

                // if it is an image task, then download the images into the
                // user home directory
                if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                    downloadToHome(task);
                }
            } else {
                Helper.setErrorMessage("stepInWorkError");
            }
            this.flagWait = false;
        } catch (DataException e) {
            Helper.setErrorMessage("stepSaveError", logger, e);
        } finally {
            this.flagWaitLock.unlock();
        }
    }

    /**
     * Unassing user from task.
     *
     * @param task
     *            object
     */
    public void unassignTaskFromUser(Task task) throws DataException {
        this.webDav.uploadFromHome(task.getProcess());
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        taskService.replaceProcessingUser(task, null);
        // if we have a correction task here then never remove startdate
        if (isCorrectionTask(task)) {
            task.setProcessingBegin(null);
        }
        task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        task.setProcessingTime(new Date());

        taskService.save(task);

        // unlock the process
        metadataLock.setFree(task.getProcess().getId());

        updateProcessSortHelperStatus(task.getProcess());
    }

    /**
     * Priority equal 10 means correction task.
     *
     * @param task
     *            Task object
     * @return true or false
     */
    public boolean isCorrectionTask(Task task) {
        return (task.getPriority() == 10);
    }

    /**
     * Set Priority equal 10 means correction task.
     *
     * @param task
     *            Task object
     */
    public void setCorrectionTask(Task task) {
        task.setPriority(10);
    }

    /**
     * Unified method for report problem with task.
     *
     * @param currentTask
     *            as Task object
     */
    public void reportProblem(Task currentTask) throws DAOException, DataException {
        this.webDav.uploadFromHome(getCurrentUser(), currentTask.getProcess());
        Date date = new Date();
        currentTask.setProcessingStatusEnum(TaskStatus.LOCKED);
        currentTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentTask.setProcessingTime(date);
        taskService.replaceProcessingUser(currentTask, getCurrentUser());
        currentTask.setProcessingBegin(null);
        taskService.save(currentTask);

        Task correctionTask = taskService.getById(getProblem().getId());
        correctionTask.setProcessingStatusEnum(TaskStatus.OPEN);
        setCorrectionTask(correctionTask);
        correctionTask.setProcessingEnd(null);

        Property processProperty = prepareProblemMessageProperty(date, currentTask, correctionTask);
        processProperty.getProcesses().add(currentTask.getProcess());
        currentTask.getProcess().getProperties().add(processProperty);

        currentTask.getProcess().setWikiField(prepareProblemWikiField(currentTask.getProcess(), correctionTask));

        taskService.save(correctionTask);

        closeTasksBetweenCurrentAndCorrectionTask(currentTask, correctionTask);

        updateProcessSortHelperStatus(currentTask.getProcess());
    }

    /**
     * Unified method for solve problem with task.
     *
     * @param currentTask
     *            task which was send to correction and now was fixed as Task object
     */
    public void solveProblem(Task currentTask) throws DAOException, DataException {
        Date date = new Date();
        this.webDav.uploadFromHome(currentTask.getProcess());
        currentTask.setProcessingStatusEnum(TaskStatus.DONE);
        currentTask.setProcessingEnd(date);
        currentTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentTask.setProcessingTime(date);
        taskService.replaceProcessingUser(currentTask, getCurrentUser());
        taskService.save(currentTask);

        // TODO: find more suitable name for this task
        // tasks which was executed at the moment of correction reporting
        List<Property> properties = currentTask.getProcess().getProperties();
        for (Property property : properties) {
            if ((property.getTitle().equals(Helper.getTranslation("correctionNecessary"))
                    && (property.getValue().contains(" CorrectionTask: " + currentTask.getId().toString())))) {
                int id = Integer
                        .parseInt(property.getValue().substring(property.getValue().indexOf("(CurrentTask: ") + 14,
                            property.getValue().indexOf(" CorrectionTask: ")));
                Task correctionTask = taskService.getById(id);
                closeTasksBetweenCurrentAndCorrectionTask(currentTask, correctionTask, date);
                openTaskForProcessing(correctionTask);
                Property processProperty = prepareSolveMessageProperty(property, currentTask);
                serviceManager.getPropertyService().save(processProperty);
                updateProcessSortHelperStatus(
                    serviceManager.getProcessService().getById(currentTask.getProcess().getId()));
                currentTask = correctionTask;
            }
        }
    }

    /**
     * Set processing status up. This method adds double check of task status.
     *
     * @param task
     *            object
     */
    private void setProcessingStatusUp(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task.setProcessingStatus(task.getProcessingStatus() + 1);
        }
    }

    /**
     * Set processing status down. This method adds double check of task status.
     *
     * @param task
     *            object
     */
    private void setProcessingStatusDown(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
            task.setProcessingStatus(task.getProcessingStatus() - 1);
        }
    }

    private void closeTasksBetweenCurrentAndCorrectionTask(Task currentTask, Task correctionTask) throws DataException {
        List<Task> allTasksInBetween = taskService.getAllTasksInBetween(correctionTask.getOrdering(),
            currentTask.getOrdering(), currentTask.getProcess().getId());
        for (Task taskInBetween : allTasksInBetween) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.LOCKED);
            setCorrectionTask(taskInBetween);
            taskInBetween.setProcessingEnd(null);
            taskService.save(taskInBetween);
        }
    }

    private void closeTasksBetweenCurrentAndCorrectionTask(Task currentTask, Task correctionTask, Date date)
            throws DataException {
        List<Task> allTasksInBetween = taskService.getAllTasksInBetween(currentTask.getOrdering(),
            correctionTask.getOrdering(), currentTask.getProcess().getId());
        for (Task taskInBetween : allTasksInBetween) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.DONE);
            taskInBetween.setProcessingEnd(date);
            taskInBetween.setPriority(0);
            taskService.save(taskInBetween);
        }
    }

    private void openTaskForProcessing(Task correctionTask) throws DataException {
        correctionTask.setProcessingStatusEnum(TaskStatus.OPEN);
        setCorrectionTask(correctionTask);
        correctionTask.setProcessingEnd(null);
        correctionTask.setProcessingTime(new Date());
        taskService.save(correctionTask);
    }

    private Property prepareProblemMessageProperty(Date date, Task currentTask, Task correctionTask) {
        Property processProperty = new Property();
        processProperty.setTitle(Helper.getTranslation("correctionNecessary"));
        processProperty.setValue("[" + Helper.getDateAsFormattedString(date) + ", "
                + serviceManager.getUserService().getFullName(getCurrentUser()) + "] " + "(CurrentTask: "
                + currentTask.getId().toString() + " CorrectionTask: " + correctionTask.getId().toString() + ") "
                + this.problem.getMessage());
        processProperty.setType(PropertyType.MESSAGE_ERROR);
        return processProperty;
    }

    private Property prepareSolveMessageProperty(Property property, Task correctionTask) {
        property.setTitle(Helper.getTranslation("correctionPerformed"));
        property.setValue("[" + Helper.getDateAsFormattedString(new Date()) + ", "
                + serviceManager.getUserService().getFullName(getCurrentUser()) + "] "
                + Helper.getTranslation("correctionSolutionFor") + " " + correctionTask.getTitle());
        property.setType(PropertyType.MESSAGE_IMPORTANT);
        return property;
    }

    private String prepareProblemWikiField(Process process, Task correctionTask) {
        String message = "Red K " + serviceManager.getUserService().getFullName(getCurrentUser()) + " "
                + Helper.getTranslation("correctionFor") + " " + correctionTask.getTitle() + ": "
                + this.problem.getMessage();

        serviceManager.getProcessService().addToWikiField(message, process);
        return process.getWikiField();
    }

    private List<Task> getAllHigherTasks(List<Task> tasks, Task task) {
        List<Task> allHigherTasks = new ArrayList<>();
        for (Task tempTask : tasks) {
            if (tempTask.getOrdering() > task.getOrdering()) {
                allHigherTasks.add(tempTask);
            }
        }
        return allHigherTasks;
    }

    private List<Task> getConcurrentTasksForOpen(List<Task> tasks, Task task) {
        boolean blocksOtherTasks = isAnotherTaskInWorkWhichBlocksOtherTasks(tasks, task);

        List<Task> allConcurrentTasks = new ArrayList<>();
        for (Task tempTask : tasks) {
            if (tempTask.getOrdering().equals(task.getOrdering()) && tempTask.getProcessingStatus() < 2
                    && !tempTask.getId().equals(task.getId())) {
                if (blocksOtherTasks) {
                    if (tempTask.isConcurrent()) {
                        allConcurrentTasks.add(tempTask);
                    }
                } else {
                    allConcurrentTasks.add(tempTask);
                }
            }
        }
        return allConcurrentTasks;
    }

    private boolean isAnotherTaskInWorkWhichBlocksOtherTasks(List<Task> tasks, Task task) {
        for (Task tempTask : tasks) {
            if (tempTask.getOrdering().equals(task.getOrdering()) && tempTask.getProcessingStatus() == 2
                    && !tempTask.getId().equals(task.getId()) && !tempTask.isConcurrent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Activate the concurrent tasks.
     */
    private void activateConcurrentTasks(List<Task> concurrentTasks) throws DataException {
        for (Task concurrentTask : concurrentTasks) {
            activateTask(concurrentTask);
        }
    }

    /**
     * If no open parallel tasks are available, activate the next tasks.
     */
    private void activateNextTasks(List<Task> allHigherTasks) throws DataException {
        int ordering = 0;
        boolean matched = false;
        for (Task higherTask : allHigherTasks) {
            if (ordering < higherTask.getOrdering() && !matched) {
                ordering = higherTask.getOrdering();
            }

            if (ordering == higherTask.getOrdering() && higherTask.getProcessingStatus() < 2) {
                activateTask(higherTask);
                matched = true;
            }
        }
    }

    /**
     * If no open parallel tasks are available, activate the next tasks.
     */
    private void activateTask(Task task) throws DataException {
        // activate the task if it is not fully automatic
        task.setProcessingStatus(1);
        task.setProcessingTime(new Date());
        task.setEditType(4);

        verifyTask(task);

        taskService.save(task);
    }

    private void verifyTask(Task task) {
        // if it is an automatic task with script
        if (task.isTypeAutomatic()) {
            automaticTasks.add(task);
        } else if (task.isTypeAcceptClose()) {
            tasksToFinish.add(task);
        }
    }

    /**
     * Update process sort helper status.
     *
     * @param process
     *            object
     */
    private void updateProcessSortHelperStatus(Process process) throws DataException {
        String value = serviceManager.getProcessService().getProgress(process.getTasks(), null);
        process.setSortHelperStatus(value);
        serviceManager.getProcessService().save(process);
    }

    /**
     * Download to user home directory.
     *
     * @param task
     *            object
     */
    private void downloadToHome(Task task) {
        task.setProcessingTime(new Date());
        if (serviceManager.getSecurityAccessService().isAuthenticated()) {
            taskService.replaceProcessingUser(task, getCurrentUser());
            this.webDav.downloadToHome(task.getProcess(), !task.isTypeImagesWrite());
        }
    }

    private User getCurrentUser() {
        return serviceManager.getUserService().getAuthenticatedUser();
    }
}
