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
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.workflow.WorkflowControllerService;

/**
 * This is a web service interface to modify task status.
 */
public class TaskActionProcessor extends ActiveMQProcessor {

    public static final String KEY_CORRECTION_TASK_ID = "correctionTaskId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TASK_ACTION = "action";
    public static final String KEY_TASK_ID = "id";
    private final TaskService taskService = ServiceManager.getTaskService();
    private final WorkflowControllerService workflowControllerService;

    /**
     * The default constructor looks up the queue name to use in kitodo_config.properties. If that is not configured and
     * “null” is passed to the super constructor, this will prevent ActiveMQDirector.registerListeners() from starting
     * this service.
     */
    public TaskActionProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_TASK_ACTION_QUEUE).orElse(null));
        workflowControllerService = new WorkflowControllerService();
    }

    /**
     * This is the main routine processing incoming messages. It gets the task id and the task action for processing.
     * Every action has its own behavior so please read the comment on the action for more information.
     *
     * @param mapMessageObjectReader
     *         the incoming message
     */
    @Override
    protected void process(MapMessageObjectReader mapMessageObjectReader) throws ProcessorException, JMSException {
        Integer taskId = mapMessageObjectReader.getMandatoryInteger(KEY_TASK_ID);
        String taskActionField = mapMessageObjectReader.getMandatoryString(KEY_TASK_ACTION);
        TaskAction taskAction;
        try {
            taskAction = TaskAction.valueOf(mapMessageObjectReader.getMandatoryString(KEY_TASK_ACTION));
        } catch (IllegalArgumentException e) {
            throw new ProcessorException("Unknown task action " + taskActionField);
        }

        try {
            Task currentTask = taskService.getById(taskId);
            if (Objects.isNull(currentTask)) {
                throw new ProcessorException("Task with id " + taskId + " not found.");
            }
            processAction(mapMessageObjectReader, taskAction, currentTask);
        } catch (DataException | DAOException | IOException e) {
            throw new ProcessorException(e);
        }
    }

    private void processAction(MapMessageObjectReader mapMessageObjectReader, TaskAction taskAction, Task currentTask)
            throws JMSException, ProcessorException, DataException, DAOException, IOException {
        Comment comment = null;
        if (mapMessageObjectReader.hasField(KEY_MESSAGE)) {
            comment = buildComment(currentTask, mapMessageObjectReader.getMandatoryString(KEY_MESSAGE));
        }

        User currentUser = ServiceManager.getUserService().getCurrentUser();
        switch (taskAction) {
            case PROCESS:
                if (!TaskStatus.OPEN.equals(currentTask.getProcessingStatus())) {
                    throw new ProcessorException("Status of task is not OPEN.");
                }
                actionProcess(currentTask, currentUser);
                break;
            case ERROR_OPEN:
                if (!TaskStatus.INWORK.equals(currentTask.getProcessingStatus())) {
                    throw new ProcessorException("Status of task is not INWORK.");
                }
                if (!mapMessageObjectReader.hasField(KEY_MESSAGE)) {
                    throw new ProcessorException("Message field of task action ERROR_OPEN is required.");
                }
                actionErrorOpen(mapMessageObjectReader, comment);
                break;
            case ERROR_CLOSE:
                if ((!mapMessageObjectReader.hasField(KEY_CORRECTION_TASK_ID) && !TaskStatus.INWORK.equals(
                        currentTask.getProcessingStatus())) || (mapMessageObjectReader.hasField(
                        KEY_CORRECTION_TASK_ID) && !TaskStatus.LOCKED.equals(currentTask.getProcessingStatus()))) {
                    throw new ProcessorException(
                            "Status of task is not INWORK if there is a no corrected task ID or LOCKED if there is a corrected task ID.");
                }
                actionErrorClose(mapMessageObjectReader, currentTask, currentUser);
                break;
            case CLOSE:
                workflowControllerService.closeTaskByUser(currentTask);
                break;
            default:
                if (!mapMessageObjectReader.hasField(KEY_MESSAGE)) {
                    throw new ProcessorException("Message field of task action COMMENT is required.");
                }
        }

        if (Objects.nonNull(comment)) {
            ServiceManager.getCommentService().saveToDatabase(comment);
        }
    }

    private static Comment buildComment(Task currentTask, String message) {
        Comment comment = new Comment();
        comment.setProcess(currentTask.getProcess());
        comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
        comment.setMessage(message);
        comment.setCreationDate(new Date());
        comment.setType(CommentType.INFO);
        comment.setCurrentTask(currentTask);
        return comment;
    }

    private void actionErrorOpen(MapMessageObjectReader mapMessageObjectReader, Comment comment)
            throws ProcessorException, JMSException, DAOException, DataException {
        if (mapMessageObjectReader.hasField(KEY_CORRECTION_TASK_ID)) {
            Integer correctionTaskId = mapMessageObjectReader.getMandatoryInteger(KEY_CORRECTION_TASK_ID);
            Task correctionTask = taskService.getById(correctionTaskId);
            if (Objects.isNull(correctionTask)) {
                throw new ProcessorException("Correction task with id " + correctionTaskId + " not found.");
            }
            comment.setCorrectionTask(correctionTask);
        }
        comment.setType(CommentType.ERROR);
        workflowControllerService.reportProblem(comment, TaskEditType.QUEUE);
    }

    private void actionProcess(Task currentTask, User currentUser) throws DataException {
        currentTask.setProcessingStatus(TaskStatus.INWORK);
        currentTask.setEditType(TaskEditType.QUEUE);
        currentTask.setProcessingTime(new Date());
        taskService.replaceProcessingUser(currentTask, currentUser);
        currentTask.setProcessingBegin(new Date());
        taskService.save(currentTask);
    }

    private void actionErrorClose(MapMessageObjectReader mapMessageObjectReader, Task currentTask, User currentUser)
            throws JMSException, DataException, DAOException, IOException {
        currentTask.setProcessingStatus(TaskStatus.OPEN);
        currentTask.setEditType(TaskEditType.QUEUE);
        currentTask.setProcessingBegin(null);
        currentTask.setProcessingTime(null);
        taskService.replaceProcessingUser(currentTask, currentUser);
        taskService.save(currentTask);

        if (mapMessageObjectReader.hasField(KEY_CORRECTION_TASK_ID)) {
            markErrorCommentAsCorrected(currentTask,
                    mapMessageObjectReader.getMandatoryInteger(KEY_CORRECTION_TASK_ID));
        } else {
            markErrorCommentAsCorrected(currentTask);
        }
    }

    private void markErrorCommentAsCorrected(Task currentTask) throws DAOException, DataException, IOException {
        markErrorCommentAsCorrected(currentTask, null);
    }

    private void markErrorCommentAsCorrected(Task currentTask, Integer correctionTaskId)
            throws DAOException, DataException, IOException {
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByTask(currentTask);
        Optional<Comment> optionalComment;
        optionalComment = comments.stream().filter(currentTaskComment -> CommentType.ERROR.equals(
                currentTaskComment.getType()) && !currentTaskComment.isCorrected() && isEqualCorrectionTask(
                correctionTaskId, currentTaskComment.getCorrectionTask())).findFirst();
        if (optionalComment.isPresent()) {
            workflowControllerService.solveProblem(optionalComment.get(), TaskEditType.QUEUE);
        }
    }

    private static boolean isEqualCorrectionTask(Integer correctionTaskId, Task correctionTask) {
        return (Objects.isNull(correctionTaskId) && Objects.isNull(correctionTask)) || (Objects.nonNull(
                correctionTaskId) && correctionTaskId.equals(correctionTask.getId()));
    }

}
