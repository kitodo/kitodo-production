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
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WikiFieldHelper;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;

@Named("CommentForm")
@SessionScoped
public class CommentForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(CommentForm.class);
    private boolean correctionComment = false;
    private String commentMessage;
    private String correctionTaskId;
    private Task currentTask;
    private BatchTaskHelper batchHelper;
    private WorkflowControllerService workflowControllerService = new WorkflowControllerService();

    /**
     * Get all process Comments.
     *
     * @return List of Comments.
     */
    public List<Comment> getAllComments() {
        try {
            this.currentTask.setProcess(WikiFieldHelper.transformWikiFieldToComment(this.currentTask.getProcess()));
        } catch (DAOException | DataException | ParseException e) {
            Helper.setErrorMessage("Error in conversion of Wiki field to comments: ", logger, e);
        }
        return ServiceManager.getCommentService().getAllCommentsByProcess(this.currentTask.getProcess());
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
     * Add a new comment to the process.
     */
    public String addComment() {
        Comment comment = new Comment();
        comment.setMessage(getCommentMessage());
        comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
        comment.setCreationDate(new Date());
        comment.setProcess(this.currentTask.getProcess());
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
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, logger, e);
        }
        newComment();
        if (comment.getType().equals(CommentType.ERROR)) {
            reportProblem(comment);
            return redirect();
        }
        return null;
    }

    private String redirect() {
        HttpServletRequest origRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (origRequest.getRequestURI().contains("metadataEditor")) {
            return MessageFormat.format(REDIRECT_PATH, "processes");
        } else {
            return MessageFormat.format(REDIRECT_PATH, "tasks");
        }
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
            this.workflowControllerService.reportProblem(comment);
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
        refreshProcess(this.currentTask.getProcess());
        if (Objects.isNull(currentTask)) {
            Helper.setErrorMessage("Invalid process state: no 'active' or 'open' task found!");
            return Collections.emptyList();
        } else {
            return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(
                    this.currentTask.getOrdering(),
                    this.currentTask.getProcess().getId());
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
            this.workflowControllerService.solveProblem(comment);
        } catch (DataException e) {
            Helper.setErrorMessage("SolveProblem", logger, e);
        }
        refreshProcess(this.currentTask.getProcess());
        return redirect();
    }

    /**
     * Solve the problem to all batch processes.
     */
    public String solveProblemForAllBatchProcesses(Comment comment) {
        for (Task task : batchHelper.getSteps()) {
            for (Comment processComment : ServiceManager.getCommentService().getAllCommentsByProcess(task.getProcess())) {
                if (!processComment.isCorrected()
                        && processComment.getCorrectionTask().getTitle().equals(comment.getCorrectionTask().getTitle())) {
                    solveProblem(processComment);
                }
            }
        }
        return MessageFormat.format(REDIRECT_PATH, "tasks");
    }

    /**
     * refresh the process in the session.
     *
     * @param process Object process to refresh
     */
    private void refreshProcess(Process process) {
        try {
            if (!Objects.equals(process.getId(), 0)) {
                ServiceManager.getProcessService().refresh(process);
                this.currentTask.setProcess(ServiceManager.getProcessService().getById(process.getId()));
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
    public void newComment() {
        if (getSizeOfPreviousStepsForProblemReporting() > 0) {
            setCorrectionTaskId(getPreviousStepsForProblemReporting().get(0).getId().toString());
        } else {
            setCorrectionTaskId("");
        }
        setCommentMessage("");
        setCorrectionComment(false);
    }

    /**
     * Check whether there are concurrent tasks in work or not.
     *
     * @return whether there are concurrent tasks in work or not
     */
    public boolean isConcurrentTaskInWork() {
        return !getConcurrentTasksInWork().isEmpty();
    }

    /**
     * Create a tooltip explaining that there are concurrent tasks to the current task.
     *
     * @return concurrent task in work tooltip
     */
    public String getConcurrentTaskInWorkTooltip() {
        List<Task> concurrentTasks = getConcurrentTasksInWork();
        if (concurrentTasks.isEmpty()) {
            return "";
        } else {
            return MessageFormat.format(Helper.getTranslation("dataEditor.comment.parallelTaskInWorkText"),
                    concurrentTasks.get(0).getTitle(), concurrentTasks.get(0).getProcessingUser().getFullName());
        }
    }

    private List<Task> getConcurrentTasksInWork() {
        int authenticatedUserId = ServiceManager.getUserService().getAuthenticatedUser().getId();
        if (currentTask.isConcurrent()) {
            return currentTask.getProcess().getTasks().stream()
                    .filter(t -> !t.getId().equals(this.currentTask.getId())
                            && TaskStatus.INWORK.equals(t.getProcessingStatus())
                            && authenticatedUserId != t.getProcessingUser().getId())
                    .collect(Collectors.toList());
        }
        return new LinkedList<>();
    }
}
