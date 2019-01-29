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

package org.kitodo.production.services.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.production.ExecutionPermission;
import org.kitodo.production.MockDatabase;
import org.kitodo.production.SecurityTestUtils;
import org.kitodo.production.config.ConfigCore;
import org.kitodo.production.config.enums.ParameterCore;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.workflow.Problem;
import org.kitodo.production.workflow.Solution;

public class WorkflowControllerServiceIT {

    private static final File scriptCreateDirUserHome = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
    private static final File scriptCreateSymLink = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_SYMLINK));
    private static final FileService fileService = ServiceManager.getFileService();
    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final WorkflowControllerService workflowService = ServiceManager.getWorkflowControllerService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);

        fileService.createDirectory(URI.create(""), "users");

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
            ExecutionPermission.setExecutePermission(scriptCreateSymLink);
        }
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
            ExecutionPermission.setNoExecutePermission(scriptCreateSymLink);
        }

        fileService.delete(URI.create("users"));
    }

    @Test
    public void shouldSetTaskStatusUp() throws Exception {
        Task task = taskService.getById(10);

        workflowService.setTaskStatusUp(task);
        assertEquals("Task status was not set up!", TaskStatus.OPEN, task.getProcessingStatusEnum());

        workflowService.setTaskStatusDown(task);
        taskService.save(task);
    }

    @Test
    public void shouldSetTasksStatusUp() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        workflowService.setTasksStatusUp(process);
        for (Task task : process.getTasks()) {
            if (Objects.equals(task.getId(), 9)) {
                assertEquals("Task status was not set up!", TaskStatus.INWORK, task.getProcessingStatusEnum());
            } else if (Objects.equals(task.getId(), 10)) {
                assertEquals("Task status was not set up!", TaskStatus.OPEN, task.getProcessingStatusEnum());
            } else {
                assertEquals("Task status was not set up!", TaskStatus.DONE, task.getProcessingStatusEnum());
            }
        }

        // set up task to previous state
        Task task = taskService.getById(6);
        workflowService.setTaskStatusDown(task);
        taskService.save(task);
    }

    @Test
    public void shouldSetTasksStatusDown() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);

        workflowService.setTasksStatusDown(process);
        List<Task> tasks = process.getTasks();
        // TODO: shouldn't be changed this status from done to in work?
        // assertEquals("Task status was not set down for first task!",
        // TaskStatus.INWORK, tasks.get(0).getProcessingStatusEnum());
        assertEquals("Task status was not set down!", TaskStatus.OPEN, tasks.get(3).getProcessingStatusEnum());

        // set up task to previous state
        Task task = taskService.getById(8);
        workflowService.setTaskStatusUp(task);
        taskService.save(task);
    }

    @Test
    public void shouldClose() throws Exception {
        Task task = taskService.getById(9);

        workflowService.close(task);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = taskService.getById(10);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        // set up tasks to previous states
        workflowService.setTaskStatusDown(task);
        workflowService.setTaskStatusDown(nextTask);

        taskService.save(task);
        taskService.save(nextTask);
    }

    @Test
    public void shouldCloseForProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(19);

        workflowService.close(task);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = taskService.getById(20);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(21);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(22);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(23);
        assertEquals("Task was set up to open!", TaskStatus.LOCKED, nextTask.getProcessingStatusEnum());
    }

    @Test
    public void shouldCloseForInWorkProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(25);

        workflowService.close(task);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = taskService.getById(26);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(27);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(28);
        assertEquals("Task was set up to open!", TaskStatus.LOCKED, nextTask.getProcessingStatusEnum());
    }

    @Test
    public void shouldCloseForInWorkProcessWithBlockingParallelTasks() throws Exception {
        Task task = taskService.getById(30);

        workflowService.close(task);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = taskService.getById(31);
        assertEquals("Task is not in work!", TaskStatus.INWORK, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(32);
        assertEquals("Task was not set to open!", TaskStatus.LOCKED, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(33);
        assertEquals("Task was set up to open!", TaskStatus.LOCKED, nextTask.getProcessingStatusEnum());
    }

    @Test
    public void shouldCloseForInWorkProcessWithNonBlockingParallelTasks() throws Exception {
        Task task = taskService.getById(35);

        workflowService.close(task);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = taskService.getById(36);
        assertEquals("Task is not in work!", TaskStatus.INWORK, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(37);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        nextTask = taskService.getById(38);
        assertEquals("Task was set up to open!", TaskStatus.LOCKED, nextTask.getProcessingStatusEnum());
    }

    @Test
    public void shouldCloseForAlmostFinishedProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(42);

        workflowService.close(task);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = taskService.getById(43);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());
    }

    @Test
    public void shouldAssignTaskToUser() throws Exception {
        fileService.createDirectory(URI.create(""), "1");
        fileService.createDirectory(URI.create("1"), "images");

        Task task = taskService.getById(6);

        workflowService.assignTaskToUser(task);
        assertEquals("Incorrect user was assigned to the task!", Integer.valueOf(1), task.getProcessingUser().getId());

        fileService.delete(URI.create("1/images"));
        fileService.delete(URI.create("1"));
    }

    @Test
    public void shouldUnassignTaskFromUser() throws Exception {
        Task task = taskService.getById(6);

        workflowService.unassignTaskFromUser(task);
        assertNull("User was not unassigned from the task!", task.getProcessingUser());
        assertEquals("Task was not set up to open after unassing of the user!", TaskStatus.OPEN,
            task.getProcessingStatusEnum());
    }

    @Test
    public void shouldReportProblem() throws Exception {
        Problem problem = new Problem();
        problem.setId(6);
        problem.setMessage("Fix it!");
        workflowService.setProblem(problem);

        Task currentTask = taskService.getById(8);
        workflowService.reportProblem(currentTask);

        Task correctionTask = taskService.getById(6);
        assertEquals("Report of problem was incorrect - task is not set up to open!", TaskStatus.OPEN,
            correctionTask.getProcessingStatusEnum());

        assertTrue("Report of problem was incorrect - task is not a correction task!",
            workflowService.isCorrectionTask(correctionTask));

        Process process = currentTask.getProcess();
        for (Task task : process.getTasks()) {
            if (correctionTask.getOrdering() < task.getOrdering() && task.getOrdering() < currentTask.getOrdering()) {
                assertEquals("Report of problem was incorrect - tasks between were not set up to locked!",
                    TaskStatus.LOCKED, task.getProcessingStatusEnum());
            }
        }

        // set up tasks to previous states
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesForWorkflowFull();
    }

    @Test
    public void shouldSolveProblem() throws Exception {
        Problem problem = new Problem();
        problem.setId(6);
        problem.setMessage("Fix it!");

        Solution solution = new Solution();
        solution.setId(8);
        solution.setMessage("Fixed");

        workflowService.setProblem(problem);
        workflowService.setSolution(solution);

        Task currentTask = taskService.getById(8);
        workflowService.reportProblem(currentTask);
        currentTask = taskService.getById(6);
        workflowService.solveProblem(currentTask);

        Task correctionTask = taskService.getById(8);

        Process process = currentTask.getProcess();
        for (Task task : process.getTasks()) {
            if (currentTask.getOrdering() < task.getOrdering() && task.getOrdering() < correctionTask.getOrdering()) {
                assertEquals("Solve of problem was incorrect - tasks between were not set up to done!", TaskStatus.DONE,
                    task.getProcessingStatusEnum());
            }
        }

        assertEquals("Solve of problem was incorrect - tasks from which correction was send was not set up to open!",
            TaskStatus.OPEN, correctionTask.getProcessingStatusEnum());

        // set up tasks to previous states
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesForWorkflowFull();
    }
}
