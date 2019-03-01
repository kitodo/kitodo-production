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

package org.kitodo.production.forms.dataeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.ProcessingNote;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.Problem;

/**
 * Backing bean for the comment panel of the meta-data editor
 */
public class CommentPanel {
    private static final Logger logger = LogManager.getLogger(CommentPanel.class);

    /**
     * List of comments showing on the panel.
     */
    private final List<String> commentList = new ArrayList<>();

    /**
     * The meta-data editor this panel belongs to.
     */
    private final DataEditorForm dataEditor;

    /**
     * The list of comments in the METS file. This variable is initialized to
     * the live list of the data bean, so adding comments to this list will
     * automatically result in them being saved to the file later.
     */
    private List<ProcessingNote> editHistory;

    /**
     * Backing bean property for the input box to input the text for a new
     * comment.
     */
    private String newComment;

    private boolean newCommentIsCorrection = false;

    private Problem problem = new Problem();

    /**
     * Whether the input box to input a new comment is showing. In this case,
     * the list of existing comments is hidden. If false, the list of existing
     * comments is showing.
     */
    private boolean showingNewComment;

    /**
     * Creates a new comment panel backing bean.
     * 
     * @param dataEditor
     *            the meta-data editor this panel belongs to
     */
    CommentPanel(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Formats a comment that is stored in different fields internally to a
     * single string. The METS role and type attributes are verbalized if they
     * conform to one of the default strings.
     * 
     * @param processingNote
     *            comment in internal format
     * @return comment as string
     */
    private static String formatProcessingNote(ProcessingNote processingNote) {
        String name = Optional.ofNullable(processingNote.getName()).orElse("");
        String role = Optional.ofNullable(processingNote.getRole()).orElse("");
        role = Helper.getTranslation("dataEditor.comment.role.".concat(role.toLowerCase()), role);
        String type = Optional.ofNullable(processingNote.getType()).orElse("");
        type = Helper.getTranslation("dataEditor.comment.type.".concat(type.toLowerCase()), type);
        String note = Optional.ofNullable(processingNote.getNote()).orElse("");

        StringBuilder stringBuilder = new StringBuilder();
        if (!name.isEmpty()) {
            stringBuilder.append(name);
            if (!role.isEmpty() || !type.isEmpty()) {
                stringBuilder.append(" (");
                stringBuilder.append(type);
                if (!role.isEmpty() && !type.isEmpty()) {
                    stringBuilder.append(' ');
                }
                stringBuilder.append(role);
                stringBuilder.append(')');
            }
            stringBuilder.append(": ");
        }
        stringBuilder.append(note);
        return stringBuilder.toString();
    }

    /**
     * Returns the list of comments showing on the panel.
     * 
     * @return the list of comments showing on the panel
     */
    public List<String> getCommentList() {
        return commentList;
    }

    /**
     * Returns the backing bean property for the input box to input the text for
     * a new comment.
     * 
     * @return the backing bean property for the input box to input the text for
     *         a new comment
     */
    public String getNewComment() {
        return newComment;
    }

    /**
     * Get a list of all previous tasks that could be used for correction
     * purposes.
     * 
     * @return list of previous tasks
     */
    public List<Task> getPreviousStepsForCorrection() {
        Process process = refreshProcess();
        return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(
            ServiceManager.getProcessService().getCurrentTask(process).getOrdering(), process.getId());
    }

    /**
     * Get problem ID.
     *
     * @return value of problem ID
     */
    public Integer getProblemId() {
        return problem.getId();
    }

    /**
     * Get problem message.
     *
     * @return value of problem message
     */
    public String getProblemMessage() {
        return problem.getMessage();
    }

    /**
     * Get newCommentIsCorrection.
     *
     * @return value of newCommentIsCorrection
     */
    public boolean isNewCommentIsCorrection() {
        return newCommentIsCorrection;
    }

    /**
     * Returns whether the input box to input a new comment is showing.
     * 
     * @return whether the input box to input a new comment is showing
     */
    public boolean isShowingNewComment() {
        return showingNewComment;
    }

    /**
     * Refresh the process in the session.
     *
     * @param process
     *            Object process to refresh
     * @return
     */
    private Process refreshProcess() {
        Process process = dataEditor.getProcess();
        try {
            if (process.getId() != 0) {
                ServiceManager.getProcessService().refresh(process);
                process = ServiceManager.getProcessService().getById(process.getId());
                dataEditor.setProcess(process);
            }

        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find process with ID " + process.getId(), logger, e);
        }
        return process;
    }

    /**
     * Method invoked if the saveNewComment commandButton is clicked.
     */
    public void saveNewCommentCommandButtonClick() {
        ProcessingNote processingNote = new ProcessingNote();
        User user = ServiceManager.getUserService().getCurrentUser();
        processingNote.setName(user.getFullName());
        processingNote.setRole(user.getRoles().parallelStream().map(Role::getTitle).collect(Collectors.joining(", ")));
        processingNote.setNote(newComment);
        editHistory.add(processingNote);
        commentList.add(formatProcessingNote(processingNote));
        showingNewComment = false;
        newComment = "";
    }

    /**
     * Save new comment and set process to specified task for correction
     * purposes.
     */
    public void sendProblemCommandButtonClick() {
        List<Task> taskList = new ArrayList<>();
        taskList.add(ServiceManager.getProcessService().getCurrentTask(dataEditor.getProcess()));
        BatchTaskHelper batchStepHelper = new BatchTaskHelper(taskList);
        batchStepHelper.setProblem(this.problem);
        batchStepHelper.reportProblemForSingle();
        refreshProcess();
        this.showingNewComment = false;
        this.newCommentIsCorrection = false;
        this.problem = new Problem();
    }

    /**
     * Sets whether the input box to input a new comment is showing.
     * 
     * @param showingNewComment
     *            whether the input box to input a new comment is showing
     */
    public void setShowingNewComment(boolean showingNewComment) {
        this.showingNewComment = showingNewComment;
    }

    /**
     * Sets the backing bean property for the input box to input the text for a
     * new comment.
     * 
     * @param newComment
     *            the backing bean property for the input box to input the text
     *            for a new comment
     */
    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    /**
     * Set newCommentIsCorrection.
     *
     * @param newCommentIsCorrection
     *            as boolean
     */
    public void setNewCommentIsCorrection(boolean newCommentIsCorrection) {
        this.newCommentIsCorrection = newCommentIsCorrection;
    }

    /**
     * Initializes the panel to show the comments from a workpiece.
     * 
     * @param workpeace
     *            workpiece whose comments are to show
     */
    void show(Workpiece workpeace) {
        editHistory = workpeace.getEditHistory();
        commentList.clear();
        for (ProcessingNote processingNote : editHistory) {
            commentList.add(formatProcessingNote(processingNote));
        }
    }

    /**
     * Mark the reported problem solved.
     */
    public void solveProblem(String comment) {
        BatchTaskHelper batchStepHelper = new BatchTaskHelper();
        Process process = dataEditor.getProcess();
        batchStepHelper.solveProblemForSingle(ServiceManager.getProcessService().getCurrentTask(process));
        process = refreshProcess();
        String wikiField = process.getWikiField();
        wikiField = wikiField.replace(comment.trim(), comment.trim().replace("Red K", "Orange K "));
        ServiceManager.getProcessService().setWikiField(wikiField, process);
        try {
            ServiceManager.getProcessService().save(process);
        } catch (DataException e) {
            Helper.setErrorMessage("correctionSolveProblem", logger, e);
        }
        process = refreshProcess();
    }
}
