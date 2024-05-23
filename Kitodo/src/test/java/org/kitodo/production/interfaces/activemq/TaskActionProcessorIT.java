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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Objects;

import javax.jms.JMSException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;

public class TaskActionProcessorIT {

    private static final TaskService taskService = ServiceManager.getTaskService();

    private static final File scriptDeleteSymLink = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_DELETE_SYMLINK));
    private static final File scriptCreateDirMeta = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));

    /**
     * Prepare the data for every test.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @BeforeEach
    public void prepare() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
        ExecutionPermission.setExecutePermission(scriptDeleteSymLink);
    }

    /**
     * Clean the data after every test.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @AfterEach
    public void clean() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
        ExecutionPermission.setNoExecutePermission(scriptCreateDirMeta);
        ExecutionPermission.setNoExecutePermission(scriptDeleteSymLink);
    }

    @Test
    public void testTaskNotFound() {
        assertThrows(ProcessorException.class, () -> processAction(Integer.MIN_VALUE, TaskAction.COMMENT.name(), StringUtils.EMPTY, null));
    }

    @Test
    public void testUnsupportedAction() {
        assertThrows(ProcessorException.class, () -> processAction(9, "UNSUPPORTED", StringUtils.EMPTY, null));
    }

    /**
     * Test the task action PROCESS.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionProcess() throws Exception {
        Task task = taskService.getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
        processAction(task, TaskAction.PROCESS);
        assertEquals(TaskStatus.INWORK, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be INWORK!");
    }

    /**
     * Test the error case without task status OPEN for task action PROCESS.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionProcessWithoutTaskStatusOpen() throws Exception {
        Task task = taskService.getById(8);
        assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be INWORK!");
        assertThrows(ProcessorException.class, () -> processAction(task, TaskAction.PROCESS));
    }

    /**
     * Test the task action CLOSE.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionClose() throws Exception {
        Task task = taskService.getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
        processAction(task, TaskAction.CLOSE);
        assertEquals(TaskStatus.DONE, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be DONE!");
    }

    /**
     * Test the task action ERROR_OPEN.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorOpen() throws Exception {
        Task inWorkTask = taskService.getById(8);
        assertEquals(TaskStatus.INWORK, inWorkTask.getProcessingStatus(), "Task '" + inWorkTask.getTitle() + "' status should be INWORK!");
        processAction(inWorkTask, TaskAction.ERROR_OPEN);
        assertEquals(TaskStatus.INWORK, taskService.getById(inWorkTask.getId()).getProcessingStatus(), "Task '" + inWorkTask.getTitle() + "' status should be INWORK!");
    }

    /**
     * Test the task action ERROR_OPEN with a correction task.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorOpenWithCorrectionTask() throws Exception {
        Task task = taskService.getById(8);
        Task correctionTask = taskService.getById(6);
        assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be INWORK!");
        processAction(task, TaskAction.ERROR_OPEN, correctionTask.getId(), 1);
        assertEquals(TaskStatus.LOCKED, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be LOCKED!");
        assertEquals(TaskStatus.OPEN, taskService.getById(correctionTask.getId()).getProcessingStatus(), "Correction task '" + correctionTask.getTitle() + "' status should be OPEN!");
    }

    /**
     * Test the error case with wrong task status for task action ERROR_OPEN.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorOpenWithoutTaskStatusInWork() throws Exception {
        Task task = taskService.getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
        assertThrows(ProcessorException.class, () -> processAction(task, TaskAction.ERROR_OPEN));
    }

    /**
     * Test the error case without message for task action ERROR_OPEN.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorOpenWithoutMessage() throws Exception {
        Task task = taskService.getById(10);
        assertThrows(ProcessorException.class, () -> processAction(task.getId(), TaskAction.ERROR_OPEN.name(), StringUtils.EMPTY, null));
    }

    /**
     * Test the task action ERROR_CLOSE.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorClose() throws Exception {
        Task task = taskService.getById(8);
        assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be INWORK!");
        processAction(task, TaskAction.ERROR_CLOSE);
        assertEquals(TaskStatus.OPEN, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
    }

    /**
     * Test the task action ERROR_CLOSE with a correction task.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorCloseWithCorrectionTask() throws Exception {
        Task task = taskService.getById(8);
        Task correctionTask = taskService.getById(6);
        assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be INWORK!");

        processAction(task, TaskAction.ERROR_OPEN, correctionTask.getId(), 1);
        assertEquals(TaskStatus.LOCKED, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be LOCKED!");
        assertEquals(TaskStatus.OPEN, taskService.getById(correctionTask.getId()).getProcessingStatus(), "Correction task '" + correctionTask.getTitle() + "' status should be OPEN!");

        processAction(task, TaskAction.ERROR_CLOSE, correctionTask.getId(), 2);
        assertEquals(TaskStatus.OPEN, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
        assertEquals(TaskStatus.DONE, taskService.getById(correctionTask.getId()).getProcessingStatus(), "Correction task '" + correctionTask.getTitle() + "' status should be DONE!");
    }

    /**
     * Test the error case without task status LOCKED for task action ERROR_CLOSE.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionErrorCloseWithoutTaskStatusInWork() throws Exception {
        Task task = taskService.getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
        assertThrows(ProcessorException.class, () -> processAction(task, TaskAction.ERROR_CLOSE));
    }

    /**
     * Test the task action COMMENT.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Test
    public void testActionComment() throws Exception {
        Task task = taskService.getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
        processAction(task, TaskAction.COMMENT, null, 1);
        processAction(task, TaskAction.COMMENT, null, 2);
        assertEquals(TaskStatus.OPEN, taskService.getById(task.getId()).getProcessingStatus(), "Task '" + task.getTitle() + "' status should be OPEN!");
    }

    private static void processAction(Task task, TaskAction taskAction) throws JMSException, ProcessorException {
        processAction(task, taskAction, null, 1);
    }

    private static void processAction(Task task, TaskAction taskAction, Integer correctionTaskId, int commentCount)
            throws JMSException, ProcessorException {
        String message = "Process action " + taskAction.name();
        processAction(task.getId(), taskAction.name(), message, correctionTaskId);
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByTask(task);
        assertEquals(commentCount, comments.size(), "Comment should be created!");
        assertEquals(message, comments.get(commentCount - 1).getMessage(), "Comment message should be '" + message + "'!");
    }

    private static void processAction(Integer taskId, String action, String message, Integer correctionTaskId)
            throws JMSException, ProcessorException {
        MapMessageObjectReader mapMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mapMessageObjectReader.getMandatoryInteger(TaskActionProcessor.KEY_TASK_ID)).thenReturn(taskId);
        when(mapMessageObjectReader.getMandatoryString(TaskActionProcessor.KEY_TASK_ACTION)).thenReturn(action);
        if (StringUtils.isNotEmpty(message)) {
            when(mapMessageObjectReader.hasField(TaskActionProcessor.KEY_MESSAGE)).thenReturn(Boolean.TRUE);
            when(mapMessageObjectReader.getMandatoryString(TaskActionProcessor.KEY_MESSAGE)).thenReturn(message);
        }
        if (Objects.nonNull(correctionTaskId)) {
            when(mapMessageObjectReader.hasField(TaskActionProcessor.KEY_CORRECTION_TASK_ID)).thenReturn(Boolean.TRUE);
            when(mapMessageObjectReader.getMandatoryInteger(TaskActionProcessor.KEY_CORRECTION_TASK_ID)).thenReturn(
                    correctionTaskId);
        }
        TaskActionProcessor taskActionProcessor = spy(TaskActionProcessor.class);
        taskActionProcessor.process(mapMessageObjectReader);
    }
}
