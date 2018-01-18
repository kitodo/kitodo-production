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

import javax.naming.AuthenticationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
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
    private User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static WorkflowService instance = null;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(WorkflowService.class);

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
     * Set Task status up.
     *
     * @param task
     *            to change status up
     * @return updated task
     */
    public Task setTaskStatusUp(Task task) throws DataException, IOException {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task = serviceManager.getTaskService().setProcessingStatusUp(task);
            task.setEditTypeEnum(TaskEditType.ADMIN);
            if (task.getProcessingStatusEnum() == TaskStatus.DONE) {
                close(task);
            } else {
                task.setProcessingTime(new Date());
                if (this.user != null) {
                    task.setProcessingUser(this.user);
                }
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
        if (this.user != null) {
            task.setProcessingUser(this.user);
        }
        task = serviceManager.getTaskService().setProcessingStatusDown(task);
        return task;
    }

    /**
     * .
     * 
     * @param process
     *            object
     */
    public void setTasksStatusUp(Process process) throws DataException, IOException {
        List<Task> tasks = process.getTasks();

        for (Task task : tasks) {
            if (!task.getProcessingStatus().equals(TaskStatus.DONE.getValue())) {
                task.setProcessingStatus(task.getProcessingStatus() + 1);
                task.setEditType(TaskEditType.ADMIN.getValue());
                if (task.getProcessingStatus().equals(TaskStatus.DONE.getValue())) {
                    close(task);
                } else {
                    if (this.user != null) {
                        task.setProcessingUser(this.user);
                        serviceManager.getTaskService().save(task);
                    }
                }
                break;
            }
        }
    }

    /**
     * .
     * 
     * @param process
     *            object
     */
    public void setTasksStatusDown(Process process) throws DataException {
        List<Task> tasks = new ArrayList<>(process.getTasks());
        Collections.reverse(tasks);

        for (Task task : tasks) {
            if (process.getTasks().get(0) != task && task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
                task.setEditTypeEnum(TaskEditType.ADMIN);
                task.setProcessingTime(new Date());
                if (this.user != null) {
                    task.setProcessingUser(this.user);
                }
                task = serviceManager.getTaskService().setProcessingStatusDown(task);
                serviceManager.getTaskService().save(task);
                break;
            }
        }
    }

    /**
     * Not sure.
     *
     * @return closed Task
     */
    public Task closeTaskByUser(Task task, WebDav webDav) throws DataException, IOException {

        // if step allows writing of images, then count all images here
        if (task.isTypeImagesWrite()) {
            try {
                // this.mySchritt.getProzess().setSortHelperImages(
                // FileUtils.getNumberOfFiles(new
                // File(this.mySchritt.getProzess().getImagesOrigDirectory())));
                HistoryAnalyserJob.updateHistory(task.getProcess());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while calculation of storage and images", e);
            }
        }

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
                try {
                    if (!mih.checkIfImagesValid(task.getProcess().getTitle(),
                        serviceManager.getProcessService().getImagesOrigDirectory(false, task.getProcess()))) {
                        return null;
                    }
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Error on image validation: ", e);
                }
            }
        }
        // if the result of the verification is ok, then continue, otherwise it is not
        // reached
        webDav.uploadFromHome(task.getProcess());
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
        if (this.user != null) {
            task.setProcessingUser(this.user);
        }
        task.setProcessingEnd(new Date());

        serviceManager.getTaskService().save(task);
        automaticTasks = new ArrayList<>();
        tasksToFinish = new ArrayList<>();

        History history = new History(new Date(), task.getOrdering(), task.getTitle(), HistoryTypeEnum.taskDone,
                task.getProcess());
        serviceManager.getHistoryService().save(history);

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

        updateProcessStatus(process);

        for (Task automaticTask : automaticTasks) {
            TaskScriptThread thread = new TaskScriptThread(automaticTask);
            TaskManager.addTask(thread);
        }
        for (Task finish : tasksToFinish) {
            close(finish);
        }
    }

    /**
     * Report the problem.
     *
     * @return Task
     */
    public Task reportProblem(Task task, WebDav webDav) {
        if (this.user == null) {
            Helper.setFehlerMeldung("userNotFound");
            return null;
        }

        webDav.uploadFromHome(task.getProcess());
        Date date = new Date();
        task.setProcessingStatusEnum(TaskStatus.LOCKED);
        task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        task.setProcessingTime(date);
        task.setProcessingUser(this.user);
        task.setProcessingBegin(null);

        try {
            Task temp = serviceManager.getTaskService().getById(this.problem.getId());
            temp.setProcessingStatusEnum(TaskStatus.OPEN);
            temp = serviceManager.getTaskService().setCorrectionStep(temp);
            temp.setProcessingEnd(null);

            Property processProperty = prepareProblemMessageProperty(date);
            processProperty.getProcesses().add(task.getProcess());
            task.getProcess().getProperties().add(processProperty);

            String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitle() + ": "
                    + this.problem.getMessage() + " (" + serviceManager.getUserService().getFullName(this.user) + ")";
            task.getProcess().setWikiField(
                WikiFieldHelper.getWikiMessage(task.getProcess(), task.getProcess().getWikiField(), "error", message));
            serviceManager.getTaskService().save(temp);
            task.getProcess().getHistory().add(new History(date, temp.getOrdering().doubleValue(), temp.getTitle(),
                    HistoryTypeEnum.taskError, temp.getProcess()));

            // close tasks between the current and the correction task
            closeTasksBetweenCurrentAndCorrectionTask1_(task, temp);

            // update the process so that the sort helper is saved
            this.serviceManager.getProcessService().save(task.getProcess());
        } catch (DAOException | DataException e) {
            logger.error("Task couldn't get saved/inserted", e);
        }

        this.problem.setMessage("");
        this.problem.setId(0);
        return task;
    }

    /**
     * THis one is taken out of BatchStepHelper.
     *
     * @param currentTask
     *            Task
     * @param problemTask
     *            String
     */
    public Task reportProblem(Task currentTask, String problemTask) {
        Date date = new Date();
        currentTask.setProcessingStatusEnum(TaskStatus.LOCKED);
        currentTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentTask.setProcessingTime(date);
        if (this.user != null) {
            currentTask.setProcessingUser(this.user);
        }
        currentTask.setProcessingBegin(null);

        try {
            Task temp = null;
            for (Task task : currentTask.getProcess().getTasks()) {
                if (task.getTitle().equals(problemTask)) {
                    temp = task;
                }
            }
            if (temp != null) {
                temp.setProcessingStatusEnum(TaskStatus.OPEN);
                temp = serviceManager.getTaskService().setCorrectionStep(temp);
                temp.setProcessingEnd(null);

                Property processProperty = prepareProblemMessageProperty(date);
                processProperty.getProcesses().add(currentTask.getProcess());
                currentTask.getProcess().getProperties().add(processProperty);

                String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitle() + ": "
                        + this.problem.getMessage() + " (" + serviceManager.getUserService().getFullName(this.user) + ")";
                currentTask.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(currentTask.getProcess(),
                    currentTask.getProcess().getWikiField(), "error", message));

                this.serviceManager.getTaskService().save(temp);
                currentTask.getProcess().getHistory().add(new History(date, temp.getOrdering().doubleValue(),
                        temp.getTitle(), HistoryTypeEnum.taskError, temp.getProcess()));

                // close all tasks between the current and the correction task
                closeTasksBetweenCurrentAndCorrectionTask2_(currentTask, temp);
            }
            // update the process so that the sort helper is saved
        } catch (DataException e) {
            logger.error(e);
        }
        return currentTask;
    }

    /**
     * Solve problem. This one is taken from AktuelleSchritteForm
     *
     * @return Task
     */
    public Task solveProblem(Task task, WebDav webDav) {
        if (this.user == null) {
            Helper.setFehlerMeldung("userNotFound");
            return null;
        }
        Date date = new Date();
        webDav.uploadFromHome(task.getProcess());
        task.setProcessingStatusEnum(TaskStatus.DONE);
        task.setProcessingEnd(date);
        task.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        task.setProcessingTime(new Date());
        task.setProcessingUser(this.user);

        try {
            Task temp = serviceManager.getTaskService().getById(this.solution.getId());
            // close all tasks between the current and the correction task
            closeTasksBetweenCurrentAndCorrectionTask1(task, temp, date);

            // update the process so that the sort helper is saved
            String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitle() + ": "
                    + this.solution.getMessage() + " (" + serviceManager.getUserService().getFullName(this.user) + ")";
            task.getProcess().setWikiField(
                WikiFieldHelper.getWikiMessage(task.getProcess(), task.getProcess().getWikiField(), "info", message));

            Property processProperty = prepareSolveMessageProperty(temp);
            processProperty.getProcesses().add(task.getProcess());
            task.getProcess().getProperties().add(processProperty);
            serviceManager.getProcessService().save(task.getProcess());
        } catch (DAOException | DataException e) {
            logger.error("task couldn't get saved/inserted", e);
        }

        this.solution.setMessage("");
        this.solution.setId(0);
        return task;
    }

    /**
     * This one is taken out of BatchStepHelper.
     *
     * @param currentTask
     *            Task
     * @param solutionTask
     *            String
     * @param webDav
     *            WebDav
     */
    public Task solveProblem(Task currentTask, String solutionTask, WebDav webDav) throws AuthenticationException {
        if (this.user == null) {
            throw new AuthenticationException("userNotFound");
        }
        Date date = new Date();
        webDav.uploadFromHome(currentTask.getProcess());
        currentTask.setProcessingStatusEnum(TaskStatus.DONE);
        currentTask.setProcessingEnd(date);
        currentTask.setEditTypeEnum(TaskEditType.MANUAL_SINGLE);
        currentTask.setProcessingTime(date);
        currentTask.setProcessingUser(this.user);

        try {
            Task temp = null;
            for (Task task : currentTask.getProcess().getTasks()) {
                if (task.getTitle().equals(solutionTask)) {
                    temp = task;
                }
            }
            if (temp != null) {
                // close tasks between the current and the correction task
                closeTasksBetweenCurrentAndCorrectionTask2(currentTask, temp, date);

                Property processProperty = prepareSolveMessageProperty(temp);
                processProperty.getProcesses().add(currentTask.getProcess());
                currentTask.getProcess().getProperties().add(processProperty);

                String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitle() + ": "
                        + this.solution.getMessage() + " (" + serviceManager.getUserService().getFullName(this.user) + ")";
                currentTask.getProcess().setWikiField(WikiFieldHelper.getWikiMessage(currentTask.getProcess(),
                    currentTask.getProcess().getWikiField(), "info", message));
                // update the process so that the collation helper is saved
            }
        } catch (DataException e) {
            logger.error(e);
        }
        return currentTask;
    }

    //TODO: find out if method should save or not task
    private void closeTasksBetweenCurrentAndCorrectionTask1_(Task currentTask, Task correctionTask) throws DataException {
        List<Task> allTasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(correctionTask.getOrdering(),
                currentTask.getOrdering(), currentTask.getProcess().getId());
        for (Task taskInBetween : allTasksInBetween) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.LOCKED);
            taskInBetween = serviceManager.getTaskService().setCorrectionStep(taskInBetween);
            taskInBetween.setProcessingEnd(null);
            serviceManager.getTaskService().save(taskInBetween);
        }
    }

    private void closeTasksBetweenCurrentAndCorrectionTask2_(Task currentTask, Task correctionTask) {
        List<Task> tasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(
                currentTask.getOrdering(), correctionTask.getOrdering(), currentTask.getProcess().getId());
        for (Task task : tasksInBetween) {
            task.setProcessingStatusEnum(TaskStatus.LOCKED);
            task = serviceManager.getTaskService().setCorrectionStep(task);
            task.setProcessingEnd(null);
        }
    }

    // TODO: shouldn't both methods be the same?! Why for batch is different than
    // for form?!
    private void closeTasksBetweenCurrentAndCorrectionTask1(Task currentTask, Task correctionTask, Date date)
            throws DataException {
        List<Task> allTasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(
            correctionTask.getOrdering(), currentTask.getOrdering(), currentTask.getProcess().getId());
        for (Task taskInBetween : allTasksInBetween) {
            taskInBetween.setProcessingStatusEnum(TaskStatus.DONE);
            taskInBetween.setProcessingEnd(date);
            taskInBetween.setPriority(0);
            if (taskInBetween.getId().intValue() == currentTask.getId().intValue()) {
                taskInBetween.setProcessingStatusEnum(TaskStatus.OPEN);
                taskInBetween = serviceManager.getTaskService().setCorrectionStep(taskInBetween);
                taskInBetween.setProcessingEnd(null);
                taskInBetween.setProcessingTime(date);
            }
            currentTask.setProcessingTime(date);
            currentTask.setProcessingUser(this.user);
            serviceManager.getTaskService().save(taskInBetween);
        }
    }

    private void closeTasksBetweenCurrentAndCorrectionTask2(Task currentTask, Task correctionTask, Date date)
            throws DataException {
        List<Task> tasksInBetween = serviceManager.getTaskService().getAllTasksInBetween(correctionTask.getOrdering(),
            currentTask.getOrdering(), currentTask.getProcess().getId());
        for (Task task : tasksInBetween) {
            task.setProcessingStatusEnum(TaskStatus.DONE);
            task.setProcessingEnd(date);
            task.setPriority(0);
            if (task.getId().intValue() == correctionTask.getId().intValue()) {
                task.setProcessingStatusEnum(TaskStatus.OPEN);
                task = serviceManager.getTaskService().setCorrectionStep(task);
                task.setProcessingEnd(null);
                task.setProcessingTime(date);
            }
            this.serviceManager.getTaskService().save(task);
        }
    }

    private Property prepareProblemMessageProperty(Date date) {
        Property processProperty = new Property();
        processProperty.setTitle(Helper.getTranslation("Korrektur notwendig"));
        processProperty.setValue("[" + this.formatter.format(date) + ", "
                + serviceManager.getUserService().getFullName(this.user) + "] " + this.problem.getMessage());
        processProperty.setType(PropertyType.messageError);
        return processProperty;
    }

    private Property prepareSolveMessageProperty(Task correctionTask) {
        Property processProperty = new Property();
        processProperty.setTitle(Helper.getTranslation("Korrektur durchgefuehrt"));
        processProperty.setValue(
                "[" + this.formatter.format(new Date()) + ", " + serviceManager.getUserService().getFullName(this.user)
                        + "] " + Helper.getTranslation("KorrekturloesungFuer") + " " + correctionTask.getTitle() + ": "
                        + this.solution.getMessage());
        processProperty.setType(PropertyType.messageImportant);
        return processProperty;
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

                    History historyOpen = new History(new Date(), task.getOrdering(), task.getTitle(),
                            HistoryTypeEnum.taskOpen, task.getProcess());
                    serviceManager.getHistoryService().save(historyOpen);

                    // if it is an automatic task with script
                    if (task.isTypeAutomatic()) {
                        automaticTasks.add(task);
                    } else if (task.isTypeAcceptClose()) {
                        tasksToFinish.add(task);
                    }

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

    /**
     * Update process status.
     *
     * @param process
     *            the process
     */
    private void updateProcessStatus(Process process) throws DataException {
        String value = serviceManager.getProcessService().getProgress(process, null);
        process.setSortHelperStatus(value);
        serviceManager.getProcessService().save(process);
    }
}
