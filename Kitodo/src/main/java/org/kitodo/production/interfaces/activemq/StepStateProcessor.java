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

package org.kitodo.production.interfaces.activemq;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.jms.JMSException;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.forms.CommentForm;
import org.kitodo.production.forms.CurrentTaskForm;
import org.kitodo.production.services.ServiceManager;

/**
 * This is a web service interface to close steps. You have to provide the step id as “id”; you can add a field
 * “message” which will be added to the wiki field.
 */
public class StepStateProcessor extends ActiveMQProcessor {

    /**
     * The default constructor looks up the queue name to use in kitodo_config.properties. If that is not configured and
     * “null” is passed to the super constructor, this will prevent ActiveMQDirector.registerListeners() from starting
     * this service.
     */
    public StepStateProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_STEP_STATE_QUEUE).orElse(null));
    }

    /**
     * This is the main routine processing incoming tickets. It gets an CurrentTaskForm object, sets it to the
     * appropriate step which is retrieved from the database, appends the message − if any − to the wiki field, and
     * executes the form’s the step close function.
     *
     * @param ticket
     *         the incoming message
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws DAOException, JMSException {
        CurrentTaskForm currentTaskForm = new CurrentTaskForm();
        Integer taskId = ticket.getMandatoryInteger("id");
        String state = ticket.getMandatoryString("state");
        String message = ticket.getMandatoryString("message");
        Task currentTask = ServiceManager.getTaskService().getById(taskId);
        currentTaskForm.setCurrentTask(currentTask);
        User currentUser = ServiceManager.getUserService().getCurrentUser();

        Comment comment = new Comment();
        comment.setProcess(currentTaskForm.getCurrentTask().getProcess());
        comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
        comment.setMessage(message);
        comment.setCreationDate(new Date());
        comment.setType(CommentType.INFO);
        comment.setCurrentTask(currentTask);

        if (StepState.PROCESS.name().equals(state)) {
            if (!TaskStatus.OPEN.equals(currentTask.getProcessingStatus())) {
                return;
            }
            currentTask.setProcessingStatus(TaskStatus.INWORK);
            currentTask.setEditType(TaskEditType.AUTOMATIC);
            currentTask.setProcessingTime(new Date());
            ServiceManager.getTaskService().replaceProcessingUser(currentTask, currentUser);
            if (Objects.isNull(currentTask.getProcessingBegin())) {
                currentTask.setProcessingBegin(new Date());
                try {
                    ServiceManager.getTaskService().save(currentTask);
                } catch (DataException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (StepState.ERROR_OPEN.name().equals(state)) {
            if (ticket.hasField("correctionTaskId")) {
                Integer correctionTaskId = Integer.parseInt(ticket.getString("correctionTaskId"));
                Task correctionTask = ServiceManager.getTaskService().getById(correctionTaskId);
                comment.setCorrectionTask(correctionTask);
            }
            comment.setType(CommentType.ERROR);
        } else if (StepState.ERROR_CLOSE.name().equals(state)) {
            List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByCurrentTask(currentTask);
            Optional<Comment> optionalComment;
            if (ticket.hasField("correctionTaskId")) {
                Integer correctionTaskId = Integer.parseInt(ticket.getString("correctionTaskId"));
                optionalComment = comments.stream().filter(currentTaskComment -> CommentType.ERROR.equals(
                        currentTaskComment.getType()) && !currentTaskComment.isCorrected() && correctionTaskId.equals(
                        currentTaskComment.getCorrectionTask().getId())).findFirst();
            } else {
                optionalComment = comments.stream().filter(currentTaskComment -> CommentType.ERROR.equals(
                        currentTaskComment.getType()) && !currentTaskComment.isCorrected() && Objects.isNull(
                        currentTaskComment.getCorrectionTask())).findFirst();
            }
            if (optionalComment.isPresent()) {
                CommentForm commentForm = new CommentForm();
                commentForm.solveProblem(optionalComment.get());

                currentTask.setProcessingStatus(TaskStatus.OPEN);
                currentTask.setEditType(TaskEditType.AUTOMATIC);
                currentTask.setProcessingBegin(null);
                ServiceManager.getTaskService().replaceProcessingUser(currentTask, currentUser);
                try {
                    ServiceManager.getTaskService().save(currentTask);
                } catch (DataException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (StepState.CLOSE.name().equals(state)) {
            currentTaskForm.closeTaskByUser();
        }

        ServiceManager.getCommentService().saveToDatabase(comment);
        try {
            ServiceManager.getProcessService().saveToIndex(currentTask.getProcess(), true);

            for (Task task : currentTask.getProcess().getTasks()) {
                // update tasks in elastic search index, which includes correction comment status
                ServiceManager.getTaskService().saveToIndex(task, true);
            }
        } catch (CustomResponseException e) {
            throw new RuntimeException(e);
        } catch (DataException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (CommentType.ERROR.equals(comment.getType())) {
            currentTaskForm.reportProblem(comment);
        }

    }

}
