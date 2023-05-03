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
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.forms.CommentForm;
import org.kitodo.production.forms.CurrentTaskForm;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;

/**
 * This is a web service interface to modify task states.
 */
public class TaskStateProcessor extends ActiveMQProcessor {

    /**
     * The default constructor looks up the queue name to use in kitodo_config.properties. If that is not configured and
     * “null” is passed to the super constructor, this will prevent ActiveMQDirector.registerListeners() from starting
     * this service.
     */
    public TaskStateProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_STEP_STATE_QUEUE).orElse(null));
    }

    /**
     * This is the main routine processing incoming tickets. It gets an CurrentTaskForm object, sets it to the
     * appropriate step which is retrieved from the database, appends the message − if any − to the wiki field, and
     * executes the form’s the step close function.
     *
     * @param mapMessageObjectReader
     *         the incoming message
     */
    @Override
    protected void process(MapMessageObjectReader mapMessageObjectReader) throws ProcessorException, JMSException {
        CurrentTaskForm currentTaskForm = new CurrentTaskForm();
        Integer taskId = mapMessageObjectReader.getMandatoryInteger("id");
        String state = mapMessageObjectReader.getMandatoryString("state");
        TaskState taskState;
        try {
            taskState = TaskState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ProcessorException("Unknown task state: " + state);
        }

        try {
            Task currentTask = ServiceManager.getTaskService().getById(taskId);
            currentTaskForm.setCurrentTask(currentTask);
            Comment comment = new Comment();
            comment.setProcess(currentTaskForm.getCurrentTask().getProcess());
            comment.setAuthor(ServiceManager.getUserService().getCurrentUser());

            String message = mapMessageObjectReader.getMandatoryString("message");
            comment.setMessage(message);
            comment.setCreationDate(new Date());
            comment.setType(CommentType.INFO);
            comment.setCurrentTask(currentTask);

            User currentUser = ServiceManager.getUserService().getCurrentUser();
            if (TaskState.PROCESS.equals(taskState) && processTaskStateProcess(currentTask, currentUser)) {
                return;
            } else if (TaskState.ERROR_OPEN.equals(taskState)) {
                processTaskStateErrorOpen(mapMessageObjectReader, comment);
            } else if (TaskState.ERROR_CLOSE.equals(taskState)) {
                processTaskStateErrorClose(mapMessageObjectReader, currentTask, currentUser);
            } else if (TaskState.CLOSE.equals(taskState)) {
                currentTaskForm.closeTaskByUser();
            }

            ServiceManager.getCommentService().saveToDatabase(comment);
            ServiceManager.getProcessService().saveToIndex(currentTask.getProcess(), true);

            for (Task task : currentTask.getProcess().getTasks()) {
                // update tasks in elastic search index, which includes correction comment status
                ServiceManager.getTaskService().saveToIndex(task, true);
            }

        } catch (DataException | DAOException | CustomResponseException | IOException e) {
            throw new ProcessorException(e);
        }
    }

    private static void processTaskStateErrorOpen(MapMessageObjectReader mapMessageObjectReader, Comment comment)
            throws JMSException, DataException, DAOException {
        if (mapMessageObjectReader.hasField("correctionTaskId")) {
            Integer correctionTaskId = Integer.parseInt(mapMessageObjectReader.getString("correctionTaskId"));
            Task correctionTask = ServiceManager.getTaskService().getById(correctionTaskId);
            comment.setCorrectionTask(correctionTask);
        }
        comment.setType(CommentType.ERROR);
        new WorkflowControllerService().reportProblem(comment);
    }

    private static boolean processTaskStateProcess(Task currentTask, User currentUser) throws DataException {
        if (!TaskStatus.OPEN.equals(currentTask.getProcessingStatus())) {
            return true;
        }
        currentTask.setProcessingStatus(TaskStatus.INWORK);
        currentTask.setEditType(TaskEditType.AUTOMATIC);
        currentTask.setProcessingTime(new Date());
        ServiceManager.getTaskService().replaceProcessingUser(currentTask, currentUser);
        if (Objects.isNull(currentTask.getProcessingBegin())) {
            currentTask.setProcessingBegin(new Date());
            ServiceManager.getTaskService().save(currentTask);
        }
        return false;
    }

    private static void processTaskStateErrorClose(MapMessageObjectReader mapMessageObjectReader, Task currentTask,
            User currentUser) throws JMSException, DataException {
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByCurrentTask(currentTask);
        Optional<Comment> optionalComment;
        if (mapMessageObjectReader.hasField("correctionTaskId")) {
            Integer correctionTaskId = Integer.parseInt(mapMessageObjectReader.getString("correctionTaskId"));
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
            currentTask.setProcessingTime(null);

            ServiceManager.getTaskService().replaceProcessingUser(currentTask, currentUser);
            ServiceManager.getTaskService().save(currentTask);
        }
    }

}
