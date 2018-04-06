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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.tasks.TaskManager;
import de.sub.goobi.metadaten.MetadatenImagesHelper;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.PropertyType;
import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.thread.TaskScriptThread;
import org.kitodo.services.ServiceManager;
import org.kitodo.workflow.Problem;
import org.kitodo.workflow.Solution;

public class WorkflowService {

    private int openTasksWithTheSameOrdering;
    private List<Task> automaticTasks;
    private List<Task> tasksToFinish;
    private Problem problem = new Problem();
    private Solution solution = new Solution();
    private User user = Helper.getCurrentUser();
    private Boolean flagWait = false;
    private final ReentrantLock flagWaitLock = new ReentrantLock();
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final WebDav webDav = new WebDav();
    private static final Logger logger = LogManager.getLogger(WorkflowService.class);
    private static WorkflowService instance = null;
    private transient ServiceManager serviceManager = new ServiceManager();

    /**
     * Return singleton variable of type TaskService.
     *
     * @return unique instance of TaskService
     */
    public static WorkflowService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (WorkflowService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new WorkflowService();
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
     * Set user for test purpose.
     *
     * @param user
     *            as User object
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Set Task status up.
     *
     * @param task
     *            to change status up
     * @return updated task
     */
    public Task setTaskStatusUp(Task task) throws DataException, IOException {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task = setProcessingStatusUp(task);
            task.setEditTypeEnum(TaskEditType.ADMIN);
            if (task.getProcessingStatusEnum() == TaskStatus.DONE) {
                close(task);
            } else {
                task.setProcessingTime(new Date());
                serviceManager.getTaskService().replaceProcessingUser(task, this.user);
            }
        }
        return task;
    }

    /**
     * Change Task status down.
     *
     * @param task
     *            to change status down
     * @return updated task
     */
    public Task setTaskStatusDown(Task task) {
        task.setEditTypeEnum(TaskEditType.ADMIN);
        task.setProcessingTime(new Date());
        serviceManager.getTaskService().replaceProcessingUser(task, this.user);
        return setProcessingStatusDown(task);
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
                serviceManager.getTaskService().save(setTaskStatusDown(task));
                break;
            }
        }
    }

    /**
     * Not sure.
     *
     * @param task
     *            object
     * @return closed Task
     */
    public Task closeTaskByUser(Task task) throws DataException, IOException {
        // if the result of the task is to be verified first, then if necessary, cancel
        // the completion
        if (task.isTypeCloseVerify()) {
            // metadata validation
            if (task.isTypeMetadata() && ConfigCore.getBooleanParameter("useMetadatenvalidierung")) {
                serviceManager.getMetadataValidationService().setAutoSave(true);
                if (!serviceManager.getMetadataValidationService().validate(task.getProcess())) {
                    return null;
                }
            }

            // image validation
            if (task.isTypeImagesWrite()) {
                MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
                URI imageFolder = serviceManager.getProcessService().getImagesOrigDirectory(false, task.getProcess());
                if (!mih.checkIfImagesValid(task.getProcess().getTitle(), imageFolder)) {
                    Helper.setFehlerMeldung("Error on image validation!");
                    return null;
                }
            }
        }
        // if the result of the verification is ok, then continue, otherwise it is not
        // reached
        this.webDav.uploadFromHome(task.getProcess());
        task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        close(task);
        return task;
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
        serviceManager.getTaskService().replaceProcessingUser(task, this.user);
        task.setProcessingEnd(new Date());

        serviceManager.getTaskService().save(task);
        automaticTasks = new ArrayList<>();
        tasksToFinish = new ArrayList<>();

        // check if there are tasks that take place in parallel but are not yet
        // completed
        List<Task> tasks = task.getProcess().getTasks();
        List<Task> allHigherTasks = getAllHigherTasks(tasks, task);

        activateNextTask(allHigherTasks);

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
     * Taken from AktuelleSchritteForm.
     *
     * @param task
     *            object
     * @return Task object
     */
    public Task assignTaskToUser(Task task) {
        this.flagWaitLock.lock();
        try {
            if (!this.flagWait) {
                this.flagWait = true;

                task.setProcessingStatusEnum(TaskStatus.INWORK);
                task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
                task.setProcessingTime(new Date());
                serviceManager.getTaskService().replaceProcessingUser(task, this.user);
                if (task.getProcessingBegin() == null) {
                    task.setProcessingBegin(new Date());
                }

                updateProcessSortHelperStatus(task.getProcess());

                // if it is an image task, then download the images into the user home directory
                if (task.isTypeImagesRead() || task.isTypeImagesWrite()) {
                    downloadToHome(task);
                }
            } else {
                Helper.setFehlerMeldung("stepInWorkError");
            }
            this.flagWait = false;
        } catch (DataException e) {
            Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
            logger.error("Task couldn't get saved", e);
        } finally {
            this.flagWaitLock.unlock();
        }
        return task;
    }

    /**
     * Unassing user from task.
     * 
     * @param task
     *            object
     * @return Task object
     */
    public Task unassignTaskFromUser(Task task) throws DataException {
        this.webDav.uploadFromHome(task.getProcess());
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        serviceManager.getTaskService().replaceProcessingUser(task, null);
        // if we have a correction task here then never remove startdate
        if (isCorrectionTask(task)) {
            task.setProcessingBegin(null);
        }
        task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        task.setProcessingTime(new Date());

        updateProcessSortHelperStatus(task.getProcess());

        return task;
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
     * @param task
     *            Task object
     * @return correction Task
     */
    public Task setCorrectionTask(Task task) {
        task.setPriority(10);
        return task;
    }

    /**
     * Unified method for report problem with task.
     *
     * @param currentTask
     *            as Task object
     * @return Task
     */
    public Task reportProblem(Task currentTask) throws DAOException, DataException {
        this.webDav.uploadFromHome(this.user, currentTask.getProcess());
        Date date = new Date();
        currentTask.setProcessingStatusEnum(TaskStatus.LOCKED);
        currentTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentTask.setProcessingTime(date);
        serviceManager.getTaskService().replaceProcessingUser(currentTask, this.user);
        currentTask.setProcessingBegin(null);

        Task correctionTask = serviceManager.getTaskService().getById(this.problem.getId());
        correctionTask.setProcessingStatusEnum(TaskStatus.OPEN);
        correctionTask = setCorrectionTask(correctionTask);
        correctionTask.setProcessingEnd(null);

        Property processProperty = prepareProblemMessageProperty(date);
        processProperty.getProcesses().add(currentTask.getProcess());
        currentTask.getProcess().getProperties().add(processProperty);

        currentTask.getProcess().setWikiField(prepareProblemWikiField(currentTask.getProcess(), correctionTask));

        serviceManager.getTaskService().save(correctionTask);

        closeTasksBetweenCurrentAndCorrectionTask(currentTask, correctionTask);

        updateProcessSortHelperStatus(currentTask.getProcess());

        this.problem.setMessage("");
        this.problem.setId(0);
        return currentTask;
    }

    /**
     * Unified method for solve problem with task.
     *
     * @param currentTask
     *            as Task object
     * @return Task
     */
    public Task solveProblem(Task currentTask) throws DAOException, DataException {
        Date date = new Date();
        this.webDav.uploadFromHome(currentTask.getProcess());
        currentTask.setProcessingStatusEnum(TaskStatus.DONE);
        currentTask.setProcessingEnd(date);
        currentTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentTask.setProcessingTime(date);
        serviceManager.getTaskService().replaceProcessingUser(currentTask, this.user);

        Task correctionTask = serviceManager.getTaskService().getById(this.solution.getId());

        closeTasksBetweenCurrentAndCorrectionTask(currentTask, correctionTask, date);

        currentTask.getProcess().setWikiField(prepareSolutionWikiField(currentTask.getProcess(), correctionTask));

        Property processProperty = prepareSolveMessageProperty(correctionTask);
        processProperty.getProcesses().add(currentTask.getProcess());
        currentTask.getProcess().getProperties().add(processProperty);

        updateProcessSortHelperStatus(currentTask.getProcess());

        this.solution.setMessage("");
        this.solution.setId(0);
        return currentTask;
    }

    /**
     * Set processing status up. This method adds double check of task status.
     *
     * @param task
     *            object
     * @return task object
     */
    private Task setProcessingStatusUp(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task.setProcessingStatus(task.getProcessingStatus() + 1);
        }
        return task;
    }

    /**
     * Set processing status down. This method adds double check of task status.
     *
     * @param task
     *            object
     * @return task object
     */
    private Task setProcessingStatusDown(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
            task.setProcessingStatus(task.getProcessingStatus() - 1);
        }
        return task;
    }

    // TODO: find out if method should save or not task
    private void closeTasksBetweenCurrentAndCorrectionTask(Task currentTask, Task correctionTask) throws DataException {
        List<Task> allTasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(
            correctionTask.getOrdering(), currentTask.getOrdering(), currentTask.getProcess().getId());
        for (Task taskInBetween : allTasksInBetween) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.LOCKED);
            taskInBetween = setCorrectionTask(taskInBetween);
            taskInBetween.setProcessingEnd(null);
            serviceManager.getTaskService().save(taskInBetween);
        }
    }

    private void closeTasksBetweenCurrentAndCorrectionTask(Task currentTask, Task correctionTask, Date date)
            throws DataException {
        List<Task> allTasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(
            correctionTask.getOrdering(), currentTask.getOrdering(), currentTask.getProcess().getId());
        for (Task taskInBetween : allTasksInBetween) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.DONE);
            taskInBetween.setProcessingEnd(date);
            taskInBetween.setPriority(0);

            // TODO: check if this two lines are needed
            // this two lines differs both methods
            currentTask.setProcessingTime(date);
            serviceManager.getTaskService().replaceProcessingUser(taskInBetween, this.user);
            // this two lines differs both methods

            serviceManager.getTaskService().save(prepareTaskForClose(currentTask, taskInBetween, date));
        }
    }

    private Task prepareTaskForClose(Task currentTask, Task taskInBetween, Date date) {
        if (taskInBetween.getId().intValue() == currentTask.getId().intValue()) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.OPEN);
            taskInBetween = setCorrectionTask(taskInBetween);
            taskInBetween.setProcessingEnd(null);
            taskInBetween.setProcessingTime(date);
        }
        return taskInBetween;
    }

    private Property prepareProblemMessageProperty(Date date) {
        Property processProperty = new Property();
        processProperty.setTitle(Helper.getTranslation("Korrektur notwendig"));
        processProperty.setValue("[" + this.formatter.format(date) + ", "
                + serviceManager.getUserService().getFullName(this.user) + "] " + this.problem.getMessage());
        processProperty.setType(PropertyType.MESSAGE_ERROR);
        return processProperty;
    }

    private Property prepareSolveMessageProperty(Task correctionTask) {
        Property processProperty = new Property();
        processProperty.setTitle(Helper.getTranslation("Korrektur durchgefuehrt"));
        processProperty.setValue(
            "[" + this.formatter.format(new Date()) + ", " + serviceManager.getUserService().getFullName(this.user)
                    + "] " + Helper.getTranslation("KorrekturloesungFuer") + " " + correctionTask.getTitle() + ": "
                    + this.solution.getMessage());
        processProperty.setType(PropertyType.MESSAGE_IMPORTANT);
        return processProperty;
    }

    private String prepareProblemWikiField(Process process, Task correctionTask) {
        String message = Helper.getTranslation("KorrekturFuer") + " " + correctionTask.getTitle() + ": "
                + this.problem.getMessage() + " (" + serviceManager.getUserService().getFullName(this.user) + ")";
        return WikiFieldHelper.getWikiMessage(process, process.getWikiField(), "error", message);
    }

    private String prepareSolutionWikiField(Process process, Task correctionTask) {
        String message = Helper.getTranslation("KorrekturloesungFuer") + " " + correctionTask.getTitle() + ": "
                + this.solution.getMessage() + " (" + serviceManager.getUserService().getFullName(this.user) + ")";
        return WikiFieldHelper.getWikiMessage(process, process.getWikiField(), "info", message);
    }

    private List<Task> getAllHigherTasks(List<Task> tasks, Task task) {
        List<Task> allHigherTasks = new ArrayList<>();
        this.openTasksWithTheSameOrdering = 0;
        for (Task tempTask : tasks) {
            if (tempTask.getOrdering().equals(task.getOrdering()) && tempTask.getProcessingStatus() != 3
                    && !tempTask.getId().equals(task.getId())) {
                openTasksWithTheSameOrdering++;
            } else if (tempTask.getOrdering() > task.getOrdering()) {
                allHigherTasks.add(tempTask);
            }
        }
        return allHigherTasks;
    }

    /**
     * If no open parallel tasks are available, activate the next tasks.
     */
    private void activateNextTask(List<Task> allHigherTasks) throws DataException {
        if (openTasksWithTheSameOrdering == 0) {
            int ordering = 0;
            boolean matched = false;
            for (Task task : allHigherTasks) {
                if (ordering < task.getOrdering() && !matched) {
                    ordering = task.getOrdering();
                }

                if (ordering == task.getOrdering() && task.getProcessingStatus() != 3
                        && task.getProcessingStatus() != 2) {
                    // activate the task if it is not fully automatic
                    task.setProcessingStatus(1);
                    task.setProcessingTime(new Date());
                    task.setEditType(4);

                    verifyTask(task);

                    serviceManager.getTaskService().save(task);
                    matched = true;
                } else {
                    if (matched) {
                        break;
                    }
                }
            }
        }
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
        if (this.user != null) {
            serviceManager.getTaskService().replaceProcessingUser(task, this.user);
            this.webDav.downloadToHome(task.getProcess(), !task.isTypeImagesWrite());
        }
    }
}
