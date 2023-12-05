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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.workflow.WorkflowControllerService;

@Named("CommentForm")
@SessionScoped
public class CommentForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(CommentForm.class);
    private boolean correctionComment = false;
    private Comment editedComment = null;
    private String commentMessage;
    private String correctionTaskId;
    private Task currentTask;
    private Process process;
    private BatchTaskHelper batchHelper;
    private final WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    /**
     * Get process.
     *
     * @return value of process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Remove a given comment.
     * 
     * @param comment to be removed.
     */
    public void removeComment(Comment comment) {
        try {
            ServiceManager.getCommentService().removeComment(comment);
            saveProcessAndTasksToIndex();
        } catch (CustomResponseException | DAOException | DataException | IOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[]{ObjectType.COMMENT.getTranslationSingular()},
                    logger, e);
        }
    }

    /**
     * Set process by ID.
     *
     * @param processId process ID
     */
    public void setProcessById(int processId) {
        try {
            this.process = ServiceManager.getProcessService().getById(processId);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {ObjectType.PROCESS.getTranslationSingular(),
                processId}, logger, e);
        }
    }

    /**
     * Get all process Comments in descending order.
     *
     * @return List of Comments.
     */
    public List<Comment> getAllComments() {
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByProcess(this.process);
        Collections.reverse(comments);
        return comments;
    }

    /**
     * Get comment's message.
     *
     * @return value of message.
     */
    public String getCommentMessage() {
        return this.commentMessage;
    }

    /**
     * Set comment's message.
     *
     * @param commentMessage String
     */
    public void setCommentMessage(String commentMessage) {
        this.commentMessage = commentMessage;
    }

    /**
     * Set's edited comment.
     * 
     * @param comment to be set as editedComment.
     */
    public void setEditedComment(Comment comment) {
        this.editedComment = comment;
    }
    
    /**
     * Returns edited comment.
     * 
     * @return edited comment.
     */
    public Comment getEditedComment() {
        return this.editedComment;
    }
    
    private void saveProcessAndTasksToIndex() throws CustomResponseException, DataException, IOException {
        ServiceManager.getProcessService().saveToIndex(this.process, true);
        for (Task task : this.process.getTasks()) {
            // update tasks in elastic search index, which includes correction comment status 
            ServiceManager.getTaskService().saveToIndex(task, true);
        }
    }

    /**
     * Add a new comment to the process.
     */
    public String addComment() {
        Comment comment = new Comment();
        comment.setMessage(getCommentMessage());
        comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
        comment.setCreationDate(new Date());
        comment.setProcess(this.process);
        if (isCorrectionComment()) {
            try {
                comment.setCorrectionTask(ServiceManager.getTaskService().getById(Integer.parseInt(getCorrectionTaskId())));
            } catch (NumberFormatException | DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[]{ObjectType.TASK.getTranslationSingular(), getCorrectionTaskId()},
                        logger, e);
                return null;
            }
            comment.setType(CommentType.ERROR);
            comment.setCorrected(Boolean.FALSE);
            comment.setCurrentTask(this.currentTask);
        } else {
            comment.setType(CommentType.INFO);
        }
        try {
            ServiceManager.getCommentService().saveToDatabase(comment);
            saveProcessAndTasksToIndex();
        } catch (CustomResponseException | DAOException | DataException | IOException e) {
            Helper.setErrorMessage(ERROR_SAVING, logger, e);
        }
        newComment(false);
        if (comment.getType().equals(CommentType.ERROR)) {
            reportProblem(comment);
            return MessageFormat.format(REDIRECT_PATH, "tasks");
        }
        return null;
    }

    /**
     * Saves the edited comment to database.
     */
    public void saveEditedComment() {
        if (Objects.nonNull(this.editedComment) && this.editedComment.getType().equals(CommentType.INFO)) {
            try {
                ServiceManager.getCommentService().saveToDatabase(this.editedComment);
                saveProcessAndTasksToIndex();
            } catch (CustomResponseException | DAOException | DataException | IOException e) {
                Helper.setErrorMessage(ERROR_SAVING, logger, e);
            }
        }
        this.editedComment = null;
    }

    /**
     * Add a new comment to all batch processes.
     */
    public String addCommentToAllBatchProcesses() {
        for (Task task : this.batchHelper.getSteps()) {
            this.currentTask = task;
            addComment();
        }
        return MessageFormat.format(REDIRECT_PATH, "tasks");
    }

    /**
     * Report the problem.
     */
    private void reportProblem(Comment comment) {
        try {
            this.workflowControllerService.reportProblem(comment, TaskEditType.MANUAL_SINGLE);
        } catch (DataException e) {
            Helper.setErrorMessage("reportingProblem", logger, e);
        }
        refreshProcess(this.currentTask.getProcess());
    }

    /**
     * Get correction comment.
     *
     * @return value of correction comment
     */
    public boolean isCorrectionComment() {
        return correctionComment;
    }

    /**
     * Set correction comment.
     *
     * @param correctionComment as boolean
     */
    public void setCorrectionComment(boolean correctionComment) {
        this.correctionComment = correctionComment;
    }

    /**
     * Get correctionTaskId.
     *
     * @return value of correctionTaskId
     */
    public String getCorrectionTaskId() {
        return correctionTaskId;
    }

    /**
     * Set correctionTaskId.
     *
     * @param correctionTaskId as java.lang.String
     */
    public void setCorrectionTaskId(String correctionTaskId) {
        this.correctionTaskId = correctionTaskId;
    }

    /**
     * Correction message to previous Tasks.
     */
    public List<Task> getPreviousStepsForProblemReporting() {
        List<Task> currentTaskOptions = getCurrentTaskOptions();
        if (currentTaskOptions.isEmpty()) {
            return Collections.emptyList();
        } else {
            return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(
                    currentTaskOptions.get(0).getOrdering(),
                    this.process.getId());
        }
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    /**
     * Solve the problem.
     */
    public String solveProblem(Comment comment) {
        try {
            this.workflowControllerService.solveProblem(comment, TaskEditType.MANUAL_SINGLE);
        } catch (DataException | DAOException | IOException e) {
            Helper.setErrorMessage("SolveProblem", logger, e);
        }
        refreshProcess(comment.getCurrentTask().getProcess());
        return MessageFormat.format(REDIRECT_PATH, "tasks");
    }

    /**
     * Solve the problem to all batch processes.
     */
    public String solveProblemForAllBatchProcesses(Comment comment) {
        for (Task task : batchHelper.getSteps()) {
            for (Comment processComment : ServiceManager.getCommentService().getAllCommentsByProcess(task.getProcess())) {
                if (!processComment.isCorrected() && verifyCorrectionTasks(comment.getCorrectionTask(),
                        processComment.getCorrectionTask())) {
                    solveProblem(processComment);
                }
            }
        }
        return MessageFormat.format(REDIRECT_PATH, "tasks");
    }

    /**
     * Verify whether both correction tasks are null or share identical titles.
     *
     * @param commentCorrectionTask
     *         The comment correction task
     * @param processCommentCorrectionTask
     *         The process comment correction task
     * @return True if they are null or have equal titles
     */
    private static boolean verifyCorrectionTasks(Task commentCorrectionTask, Task processCommentCorrectionTask) {
        if (Objects.isNull(commentCorrectionTask) && Objects.isNull(processCommentCorrectionTask)) {
            return true;
        } else if (Objects.isNull(commentCorrectionTask) && Objects.nonNull(
                processCommentCorrectionTask) || Objects.nonNull(commentCorrectionTask) && Objects.isNull(
                processCommentCorrectionTask)) {
            return false;
        }
        return processCommentCorrectionTask.getTitle().equals(commentCorrectionTask.getTitle());
    }

    /**
     * refresh the process in the session.
     *
     * @param process Object process to refresh
     */
    private void refreshProcess(Process process) {
        try {
            if (!Objects.equals(process.getId(), 0)) {
                if (Objects.nonNull(this.currentTask)) {
                    this.currentTask.setProcess(ServiceManager.getProcessService().getById(process.getId()));
                }
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), this.currentTask.getProcess().getId()},
                    logger, e);
        }
    }

    /**
     * Set current task by ID.
     *
     * @param taskId
     *          ID of task to set
     */
    public void setCurrentTaskById(int taskId) {
        try {
            this.currentTask = ServiceManager.getTaskService().getById(taskId);
            this.setProcessById(this.currentTask.getProcess().getId());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.TASK.getTranslationSingular(), taskId}, logger, e);
        }
    }

    /**
     * Set batchHelper.
     *
     * @param batchHelper as org.kitodo.production.helper.batch.BatchTaskHelper
     */
    public void setBatchHelper(BatchTaskHelper batchHelper) {
        this.batchHelper = batchHelper;
        this.currentTask = this.batchHelper.getCurrentStep();
    }

    /**
     * Set default comment.
     */
    public void newComment(Boolean isCorrectionComment) {
        if (getSizeOfPreviousStepsForProblemReporting() > 0) {
            setCorrectionTaskId(getPreviousStepsForProblemReporting().get(0).getId().toString());
        } else {
            setCorrectionTaskId("");
        }
        setCommentMessage("");
        setCorrectionComment(isCorrectionComment);
    }

    /**
     * Check whether there are concurrent tasks in work or not.
     *
     * @return whether there are concurrent tasks in work or not
     */
    public boolean isConcurrentTaskInWork() {
        return !TaskService.getTasksInWorkByOtherUsers(
                TaskService.getConcurrentTasksOpenOrInWork(this.process, this.currentTask)).isEmpty();
    }

    /**
     * Check and return whether 'correction' flag is set to true for any task of the current process,
     * e.g. if process is currently in a correction workflow.
     *
     * @return whether process is in correction workflow or not
     */
    public boolean isCorrectionWorkflow() {
        return TaskService.isCorrectionWorkflow(this.process);
    }

    /**
     * Create and return a tooltip for the correction message switch.
     *
     * @return tooltip for correction message switch
     */
    public String getCorrectionMessageSwitchTooltip() {
        return TaskService.getCorrectionMessageSwitchTooltip(this.process, this.currentTask);
    }

    /**
     * Compute and return list of tasks that are eligible as 'currentTask' for a new correction comment.
     *
     * @return list of current task options for new correction comment
     */
    public List<Task> getCurrentTaskOptions() {
        return TaskService.getCurrentTaskOptions(this.process);
    }

    /**
     * Get currentTask.
     *
     * @return value of currentTask
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * Set currentTask.
     *
     * @param currentTask as org.kitodo.data.database.beans.Task
     */
    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    /**
     * Check and return whether the process has any correction comments or not.
     *
     * @param processId
     *          identifier of process to check
     * @return 0, if process has no correction comment
     *         1, if process has correction comments that are all corrected
     *         2, if process has at least one open correction comment
     */
    public int hasCorrectionComment(int processId) {
        try {
            return ProcessService.hasCorrectionComment(processId).getValue();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), processId }, logger, e);
            return 0;
        }
    }

    /**
     * Check and return whether the current process has any unresolved problems.
     *
     * @return whether the current process has any unresolved problems
     */
    public boolean hasUnsolvedProblem() {
        return getAllComments().stream()
                .anyMatch(comment -> comment.getType().equals(CommentType.ERROR) && !comment.isCorrected());
    }

    /**
     * Create and return link text for 'Close task' link on task details page.
     *
     * @return link text for 'Close task' link on task details page
     */
    public String getTaskCloseLinkText() {
        if (isCorrectionWorkflow()) {
            return Helper.getTranslation("closeTask") + " (" + Helper.getTranslation("correctionWorkflow")
                    + ")";
        } else {
            return Helper.getTranslation("closeTask");
        }
    }

    /**
     * Returns true if a correction comment is allowed to be added.
     */
    public boolean isCorrectionCommentAllowed() {
        return getSizeOfPreviousStepsForProblemReporting() > 0 && !isConcurrentTaskInWork() && !isCorrectionWorkflow();
    }
}
