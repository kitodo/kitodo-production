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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.jms.JMSException;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;

public class TaskActionProcessorIT {

    private static final TaskService taskService = ServiceManager.getTaskService();

    /**
     * Prepare the data for every test.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @Before
    public void prepare() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    /**
     * Clean the data after every test.
     *
     * @throws Exception
     *         if something goes wrong
     */
    @After
    public void clean() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test(expected = ProcessorException.class)
    public void testTaskNotFound() throws Exception {
        processAction(Integer.MIN_VALUE, TaskAction.COMMENT.name(), "");
    }

    @Test(expected = ProcessorException.class)
    public void testUnsupportedAction() throws Exception {
        processAction(9, "UNSUPPORTED", "");
    }

    @Test
    public void testActionProcess() throws Exception {
        Task task = taskService.getById(9);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                task.getProcessingStatus());
        processAction(task, TaskAction.PROCESS);
        assertEquals("Task '" + task.getTitle() + "' status should be INWORK!", TaskStatus.INWORK,
                taskService.getById(task.getId()).getProcessingStatus());
    }

    @Test(expected = ProcessorException.class)
    public void testActionProcessWithoutTaskStatusOpen() throws Exception {
        Task task = taskService.getById(8);
        assertEquals("Task '" + task.getTitle() + "' status should be INWORK!", TaskStatus.INWORK,
                task.getProcessingStatus());
        processAction(task, TaskAction.PROCESS);
    }


    @Test
    public void testActionClose() throws Exception {
        Task task = taskService.getById(9);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                task.getProcessingStatus());
        processAction(task, TaskAction.CLOSE);
        assertEquals("Task '" + task.getTitle() + "' status should be DONE!", TaskStatus.DONE,
                taskService.getById(task.getId()).getProcessingStatus());
    }

    @Test
    public void testActionErrorOpen() throws Exception {
        Task openTask = taskService.getById(9);
        assertEquals("Task '" + openTask.getTitle() + "' status should be OPEN!", TaskStatus.OPEN, openTask.getProcessingStatus());
        processAction(openTask, TaskAction.ERROR_OPEN);
        assertEquals("Task '" + openTask.getTitle() + "' status should be LOCKED!", TaskStatus.LOCKED,
                taskService.getById(openTask.getId()).getProcessingStatus());

        Task inWorkTask = taskService.getById(8);
        assertEquals("Task '" + inWorkTask.getTitle() + "' status should be INWORK!", TaskStatus.INWORK,
                inWorkTask.getProcessingStatus());
        processAction(inWorkTask, TaskAction.ERROR_OPEN);
        assertEquals("Task '" + inWorkTask.getTitle() + "' status should be LOCKED!", TaskStatus.LOCKED,
                taskService.getById(inWorkTask.getId()).getProcessingStatus());
    }

    public void testActionErrorOpenWithCorrectionTask() throws Exception {
        Task openTask = taskService.getById(9);
        assertEquals("Task '" + openTask.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                openTask.getProcessingStatus());
        processAction(openTask, TaskAction.ERROR_OPEN);
        assertEquals("Task '" + openTask.getTitle() + "' status should be LOCKED!", TaskStatus.LOCKED,
                taskService.getById(openTask.getId()).getProcessingStatus());

    }

    @Test(expected = ProcessorException.class)
    public void testActionErrorOpenWithoutTaskStatusOpenOrInWork() throws Exception {
        Task task = taskService.getById(10);
        assertEquals("Task '" + task.getTitle() + "' status should be LOCKED!", TaskStatus.LOCKED,
                task.getProcessingStatus());
        processAction(task, TaskAction.ERROR_OPEN);
    }

    @Test(expected = ProcessorException.class)
    public void testActionErrorOpenWithoutMessage() throws Exception {
        Task task = taskService.getById(10);
        processAction(task.getId(), TaskAction.ERROR_OPEN.name(), StringUtils.EMPTY);
    }

    @Test
    public void testActionErrorClose() throws Exception {
        Task task = taskService.getById(10);
        assertEquals("Task '" + task.getTitle() + "' status should be LOCKED!", TaskStatus.LOCKED,
                task.getProcessingStatus());
        processAction(task, TaskAction.ERROR_CLOSE);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                taskService.getById(task.getId()).getProcessingStatus());
    }

    @Test(expected = ProcessorException.class)
    public void testActionErrorCloseWithoutTaskStatusLocked() throws Exception {
        Task task = taskService.getById(9);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                task.getProcessingStatus());
        processAction(task, TaskAction.ERROR_CLOSE);
    }

    @Test
    public void testActionComment() throws Exception {
        Task task = taskService.getById(10);
        assertEquals("Task '" + task.getTitle() + "' status should be LOCKED!", TaskStatus.LOCKED,
                task.getProcessingStatus());
        processAction(task, TaskAction.ERROR_CLOSE);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                taskService.getById(task.getId()).getProcessingStatus());
    }

    private static void processAction(Task task, TaskAction taskAction) throws JMSException, ProcessorException {
        String message = "Process action " +  taskAction.name();
        processAction(task.getId(), taskAction.name(), message);
        List<Comment> comments = ServiceManager.getCommentService().getAllCommentsByTask(task);
        assertEquals("Comment should be created!", 1, comments.size());
        assertEquals("Comment message should be '" + message + "'!", message, comments.get(0).getMessage());
    }

    private static void processAction(Integer taskId, String action, String message) throws JMSException,
            ProcessorException {
        MapMessageObjectReader mapMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mapMessageObjectReader.getMandatoryInteger("id")).thenReturn(taskId);
        when(mapMessageObjectReader.getMandatoryString("action")).thenReturn(action);
        if (StringUtils.isNotEmpty(message)) {
            when(mapMessageObjectReader.hasField("message")).thenReturn(Boolean.TRUE);
            when(mapMessageObjectReader.getMandatoryString("message")).thenReturn(message);
        }
        TaskActionProcessor taskActionProcessor = spy(TaskActionProcessor.class);
        taskActionProcessor.process(mapMessageObjectReader);
    }
}
