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

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskStatusChangeProcessorIT {

    private static final File scriptCreateDirUserHome = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
    private static final File scriptCreateSymLink = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_SYMLINK));
    private static final File scriptNotWorking = new File("src/test/resources/scripts/not_working_script.sh");
    private static final File scriptWorking = new File("src/test/resources/scripts/working_script.sh");
    private static final File usersDirectory = new File("src/test/resources/users");
    private static final FileService fileService = ServiceManager.getFileService();
    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final WorkflowControllerService workflowService = new WorkflowControllerService();

    @Before
    public void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);

        usersDirectory.mkdir();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
            ExecutionPermission.setExecutePermission(scriptCreateSymLink);
            ExecutionPermission.setExecutePermission(scriptNotWorking);
            ExecutionPermission.setExecutePermission(scriptWorking);
        }
    }

    @After
    public void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
            ExecutionPermission.setNoExecutePermission(scriptCreateSymLink);
            ExecutionPermission.setNoExecutePermission(scriptNotWorking);
            ExecutionPermission.setNoExecutePermission(scriptWorking);
        }

        usersDirectory.delete();
    }

    @Test
    public void useTaskStatusChangeTypeProcess() throws Exception {
        Task task = taskService.getById(9);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                task.getProcessingStatus());

        TaskStatusChangeProcessor taskStatusChangeProcessor = spy(TaskStatusChangeProcessor.class);

        MapMessageObjectReader mapMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mapMessageObjectReader.getMandatoryInteger("id")).thenReturn(task.getId());
        when(mapMessageObjectReader.getMandatoryString("type")).thenReturn(TaskStatusChangeType.PROCESS.name());
        taskStatusChangeProcessor.process(mapMessageObjectReader);
        assertEquals("Task '" + task.getTitle() + "' status should be INWORK!", TaskStatus.INWORK,
                taskService.getById(task.getId()).getProcessingStatus());
    }

    @Test(expected = ProcessorException.class)
    public void useTaskStatusChangeTypeProcessForTaskWithoutOpenStatus() throws Exception {
        Task task = taskService.getById(8);
        assertEquals("Task '" + task.getTitle() + "' status should be INWORK!", TaskStatus.INWORK,
                task.getProcessingStatus());

        TaskStatusChangeProcessor taskStatusChangeProcessor = spy(TaskStatusChangeProcessor.class);
        MapMessageObjectReader mapMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mapMessageObjectReader.getMandatoryInteger("id")).thenReturn(task.getId());
        when(mapMessageObjectReader.getMandatoryString("type")).thenReturn(TaskStatusChangeType.PROCESS.name());
        taskStatusChangeProcessor.process(mapMessageObjectReader);
    }


    @Test
    public void useTaskStatusChangeTypeClose() throws Exception {
        Task task = taskService.getById(9);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                task.getProcessingStatus());
        TaskStatusChangeProcessor taskStatusChangeProcessor = spy(TaskStatusChangeProcessor.class);
        MapMessageObjectReader mapMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mapMessageObjectReader.getMandatoryInteger("id")).thenReturn(task.getId());
        when(mapMessageObjectReader.getMandatoryString("type")).thenReturn(TaskStatusChangeType.CLOSE.name());
        taskStatusChangeProcessor.process(mapMessageObjectReader);
        assertEquals("Task '" + task.getTitle() + "' status should be DONE!", TaskStatus.DONE,
                taskService.getById(task.getId()).getProcessingStatus());
    }

    @Test
    public void useTaskStatusChangeTypeErrorOpen() throws Exception {
        Task task = taskService.getById(9);
        assertEquals("Task '" + task.getTitle() + "' status should be OPEN!", TaskStatus.OPEN,
                task.getProcessingStatus());
        TaskStatusChangeProcessor taskStatusChangeProcessor = spy(TaskStatusChangeProcessor.class);
        MapMessageObjectReader mapMessageObjectReader = mock(MapMessageObjectReader.class);
        when(mapMessageObjectReader.getMandatoryInteger("id")).thenReturn(task.getId());
        when(mapMessageObjectReader.getMandatoryString("type")).thenReturn(TaskStatusChangeType.ERROR_OPEN.name());
        taskStatusChangeProcessor.process(mapMessageObjectReader);
        assertEquals("Task '" + task.getTitle() + "' status should be DONE!", TaskStatus.DONE,
                taskService.getById(task.getId()).getProcessingStatus());
    }


}
