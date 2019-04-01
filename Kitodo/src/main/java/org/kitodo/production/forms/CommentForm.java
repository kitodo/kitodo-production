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
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WikiFieldHelper;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.services.ServiceManager;

@Named("CommentForm")
@SessionScoped
public class CommentForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(CommentForm.class);
    private boolean correctionComment = false;
    private String commentMessage;
    private String correctionTaskId;
    private String processId;
    private Process process;
    private BatchTaskHelper batchHelper;


    public List<Comment> getAllComments() {
        WikiFieldHelper.transformWikiFieldToComment(this.process);
        return ServiceManager.getCommentService().getAllCommentsByProcess(this.process);
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
    public void addComment() {
        Comment comment = new Comment();
        comment.setMessage(getCommentMessage());
        comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
        comment.setCreationDate(new Date());
        comment.setProcess(this.process);
        if (isCorrectionComment()) {
            comment.setType(CommentType.ERROR);
            comment.setCorrected(Boolean.FALSE);
            comment.setCurrentTask(ServiceManager.getProcessService().getCurrentTask(this.process));
            try {
                comment.setCorrectionTask(ServiceManager.getTaskService().getById(Integer.parseInt(getCorrectionTaskId())));
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), getCorrectionTaskId() },
                        logger, e);
            }
            setCorrectionComment(false);
        } else {
            comment.setType(CommentType.INFO);
        }
        try {
            ServiceManager.getCommentService().saveToDatabase(comment);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorSaving", logger, e);
        }

        if (isCorrectionComment()) {
            reportProblem(comment);
        }
        setCommentMessage("");
        setCorrectionTaskId("");
    }

    /**
     * Add a new comment to all batch processes.
     */
    public void addCommentToAll() {
        for (Task task : this.batchHelper.getSteps()) {
            this.process = task.getProcess();
            addComment();
        }
    }

    /**
     * Report the problem.
     */
    public void reportProblem(Comment comment) {
        try {
            ServiceManager.getWorkflowControllerService().reportProblem(comment);
        } catch (DataException e) {
            Helper.setErrorMessage("reportingProblem", logger, e);
        }
        refreshProcess(this.process);
        setCorrectionComment(false);
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
        refreshProcess(this.process);
        return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(
                ServiceManager.getProcessService().getCurrentTask(this.process).getOrdering(), this.process.getId());
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    /**
     * Solve the problem.
     */
    public void solveProblem(Comment comment) {
        try {
            ServiceManager.getWorkflowControllerService().solveProblem(comment);
        } catch (DataException e) {
            Helper.setErrorMessage("SolveProblem", logger, e);
        }
        refreshProcess(this.process);
    }

    /**
     * Solve the problem to all batch processes.
     */
    public void solveProblemForAll(Comment comment) {
        for (Task task : batchHelper.getSteps()) {
            for (Comment processComment : ServiceManager.getCommentService().getAllCommentsByProcess(task.getProcess())) {
                if (!processComment.isCorrected()
                        && processComment.getCorrectionTask().getTitle().equals(comment.getCorrectionTask().getTitle())) {
                    solveProblem(processComment);
                }
            }
        }
    }

    /**
     * Get process.
     *
     * @return value of process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Set process.
     *
     * @param process as org.kitodo.data.database.beans.Process
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * refresh the process in the session.
     *
     * @param process Object process to refresh
     */
    public void refreshProcess(Process process) {
        try {
            if (!Objects.equals(process.getId(), 0)) {
                ServiceManager.getProcessService().refresh(process);
                setProcess(ServiceManager.getProcessService().getById(process.getId()));
            }

        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find process with ID " + process.getId(), logger, e);
        }
    }


    /**
     * Get processId.
     *
     * @return value of processId
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Set processId.
     *
     * @param processId as java.lang.String
     */
    public void setProcessId(String processId) {
        this.processId = processId;
        try {
            setProcess(ServiceManager.getProcessService().getById(Integer.parseInt(this.processId)));
        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find process with ID " + process.getId(), logger, e);
        }
    }

    /**
     * Get batchHelper.
     *
     * @return value of batchHelper
     */
    public BatchTaskHelper getBatchHelper() {
        return batchHelper;
    }

    /**
     * Set batchHelper.
     *
     * @param batchHelper as org.kitodo.production.helper.batch.BatchTaskHelper
     */
    public void setBatchHelper(BatchTaskHelper batchHelper) {
        this.batchHelper = batchHelper;
        setProcess(this.batchHelper.getCurrentStep().getProcess());
    }
}
