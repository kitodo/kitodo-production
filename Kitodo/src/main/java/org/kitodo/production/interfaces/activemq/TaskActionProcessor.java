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
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.workflow.WorkflowControllerService;

/**
 * This is a web service interface to modify task states.
 */
public class TaskActionProcessor extends ActiveMQProcessor {

    public static final String KEY_CORRECTION_TASK_ID = "correctionTaskId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TASK_ACTION = "action";
    public static final String KEY_TASK_ID = "id";
    private final TaskService taskService = ServiceManager.getTaskService();
    private WorkflowControllerService workflowControllerService;

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
     * This is the main routine processing incoming tickets. It gets an CurrentTaskForm object, sets it to the
     * appropriate step which is retrieved from the database, appends the message − if any − to the wiki field, and
     * executes the form’s the step close function.
     *
     * @param mapMessageObjectReader
     *         the incoming message
     */
    @Override
    protected void process(MapMessageObjectReader mapMessageObjectReader) throws ProcessorException, JMSException {
        Integer taskId = mapMessageObjectReader.getMandatoryInteger(KEY_TASK_ID);
        String state = mapMessageObjectReader.getMandatoryString(KEY_TASK_ACTION);
        TaskAction taskAction;
        try {
            taskAction = TaskAction.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ProcessorException("Unknown task state " + state);
        }

        try {
            Task currentTask = taskService.getById(taskId);
            if (Objects.isNull(currentTask)) {
                throw new ProcessorException("Task with id " + taskId + "not found.");
            }

            processAction(mapMessageObjectReader, taskAction, currentTask);
            ServiceManager.getProcessService().saveToIndex(currentTask.getProcess(), true);

            for (Task task : currentTask.getProcess().getTasks()) {
                // update tasks in elastic search index, which includes correction comment status
                taskService.saveToIndex(task, true);
            }
        } catch (DataException | DAOException | CustomResponseException | IOException e) {
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
        if (TaskAction.PROCESS.equals(taskAction)) {
            if (!TaskStatus.OPEN.equals(currentTask.getProcessingStatus())) {
                throw new ProcessorException("Status of task is not OPEN.");
            }
            actionProcess(currentTask, currentUser);
        } else if (TaskAction.ERROR_OPEN.equals(taskAction)) {
            if (!TaskStatus.OPEN.equals(currentTask.getProcessingStatus()) && !TaskStatus.INWORK.equals(
                    currentTask.getProcessingStatus())) {
                throw new ProcessorException("Status of task is not OPEN or INWORK.");
            }
            if (!mapMessageObjectReader.hasField(KEY_MESSAGE)) {
                throw new ProcessorException("Message field of task action ERROR_OPEN is required.");
            }
            actionErrorOpen(mapMessageObjectReader, comment);
        } else if (TaskAction.ERROR_CLOSE.equals(taskAction)) {
            if (!TaskStatus.LOCKED.equals(currentTask.getProcessingStatus())) {
                throw new ProcessorException("Status of task is not LOCKED.");
            }
            actionErrorClose(mapMessageObjectReader, currentTask, currentUser);
        } else if (TaskAction.CLOSE.equals(taskAction)) {
            workflowControllerService.closeTaskByUser(currentTask);
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
        if (Objects.isNull(currentTask.getProcessingBegin())) {
            currentTask.setProcessingBegin(new Date());
            taskService.save(currentTask);
        }
    }

    private void actionErrorClose(MapMessageObjectReader mapMessageObjectReader, Task currentTask, User currentUser)
            throws JMSException, DataException, DAOException, IOException {
        if (mapMessageObjectReader.hasField(KEY_CORRECTION_TASK_ID)) {
            closeCorrectionTask(currentTask, mapMessageObjectReader.getMandatoryInteger(KEY_CORRECTION_TASK_ID));
        }
        currentTask.setProcessingStatus(TaskStatus.OPEN);
        currentTask.setEditType(TaskEditType.QUEUE);
        currentTask.setProcessingBegin(null);
        currentTask.setProcessingTime(null);

        taskService.replaceProcessingUser(currentTask, currentUser);
        taskService.save(currentTask);
    }

    private void closeCorrectionTask(Task currentTask, Integer correctionTaskId)
            throws DAOException, DataException, IOException {
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByCurrentTask(currentTask);
        Optional<Comment> optionalComment;
        optionalComment = comments.stream().filter(currentTaskComment -> CommentType.ERROR.equals(
                currentTaskComment.getType()) && !currentTaskComment.isCorrected() && correctionTaskId.equals(
                currentTaskComment.getCorrectionTask().getId())).findFirst();
        if (!optionalComment.isEmpty()) {
            workflowControllerService.solveProblem(optionalComment.get());
        }
    }

}
