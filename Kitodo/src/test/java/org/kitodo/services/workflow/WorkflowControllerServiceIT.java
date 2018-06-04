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

package org.kitodo.services.workflow;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.TaskService;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.Problem;
import org.kitodo.workflow.Solution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WorkflowControllerServiceIT {

    private static final File scriptCreateDirUserHome = new File(ConfigCore.getParameter("script_createDirUserHome"));
    private static final File scriptCreateSymLink = new File(ConfigCore.getParameter("script_createSymLink"));
    private static final ServiceManager serviceManager = new ServiceManager();
    private static final FileService fileService = serviceManager.getFileService();
    private static final TaskService taskService = serviceManager.getTaskService();
    private static final WorkflowControllerService workflowService = serviceManager.getWorkflowControllerService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(new ServiceManager().getUserService().getById(1));

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
    }

    @Test
    public void shouldWorkflowAsTemplate() throws Exception {
        fileService.createDirectory(URI.create(""), "diagrams");
        FileLoader.createExtendedGatewayDiagramTestFile();

        workflowService.saveWorkflowAsTemplate("gateway");

        Template template = serviceManager.getTemplateService().getByQuery("FROM Template WHERE title = 'test-gateway'").get(0);
        assertEquals("Tasks of template were not saved correctly!", "Test Gateway", template.getOutputName());
        assertEquals("Tasks of template were not saved correctly!", 5, template.getTasks().size());

        List<Workflow> workflows = serviceManager.getWorkflowService().getByQuery("FROM Workflow WHERE title = 'Process_1'");
        assertEquals("Workflow of template was not saved correctly!", 1, workflows.size());

        FileLoader.deleteExtendedGatewayDiagramTestFile();
        fileService.delete(URI.create("diagrams"));
    }

    @Test
    public void shouldSetTaskStatusUp() throws Exception {
        Task task = taskService.getById(4);

        task = workflowService.setTaskStatusUp(task);
        assertEquals("Task status was not set up!", TaskStatus.OPEN, task.getProcessingStatusEnum());

        taskService.save(workflowService.setTaskStatusDown(task));
    }

    @Test
    public void shouldSetTasksStatusUp() throws Exception {
        Process process = serviceManager.getProcessService().getById(1);

        workflowService.setTasksStatusUp(process);
        for (Task task : process.getTasks()) {
            if (Objects.equals(task.getId(), 7)) {
                assertEquals("Task status was not set up!", TaskStatus.INWORK, task.getProcessingStatusEnum());
            } else {
                assertEquals("Task status was not set up!", TaskStatus.DONE, task.getProcessingStatusEnum());
            }
        }

        // set up task to previous state
        taskService.save(workflowService.setTaskStatusDown(taskService.getById(6)));
    }

    @Test
    public void shouldSetTasksStatusDown() throws Exception {
        Process process = serviceManager.getProcessService().getById(1);

        workflowService.setTasksStatusDown(process);
        List<Task> tasks = process.getTasks();
        //TODO: shouldn't be changed this status from done to in work?
        //assertEquals("Task status was not set down for first task!", TaskStatus.INWORK, tasks.get(0).getProcessingStatusEnum());
        assertEquals("Task status was not set down!", TaskStatus.OPEN, tasks.get(1).getProcessingStatusEnum());

        // set up task to previous state
        taskService.save(workflowService.setTaskStatusUp(taskService.getById(6)));
    }

    @Test
    public void shouldClose() throws Exception {
        Task task = taskService.getById(6);

        workflowService.close(task);
        task = serviceManager.getTaskService().getById(6);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatusEnum());

        Task nextTask = serviceManager.getTaskService().getById(7);
        assertEquals("Task was not set up to open!", TaskStatus.OPEN, nextTask.getProcessingStatusEnum());

        // set up tasks to previous states
        taskService.save(workflowService.setTaskStatusDown(task));
        taskService.save(workflowService.setTaskStatusDown(nextTask));
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
        Task task = taskService.getById(5);

        workflowService.unassignTaskFromUser(task);
        assertNull("User was not unassigned from the task!", task.getProcessingUser());
        assertEquals("Task was not set up to open after unassing of the user!", TaskStatus.OPEN, task.getProcessingStatusEnum());
    }

    @Test
    public void shouldReportProblem() throws Exception {

        Problem problem = new Problem();
        problem.setId(5);
        problem.setMessage("Fix it!");
        workflowService.setProblem(problem);

        Task currentTask = taskService.getById(7);
        workflowService.reportProblem(currentTask);

        Task correctionTask = taskService.getById(5);
        assertEquals("Report of problem was incorrect - task is not set up to open!", TaskStatus.OPEN, correctionTask.getProcessingStatusEnum());

        assertTrue("Report of problem was incorrect - task is not a correction task!", workflowService.isCorrectionTask(correctionTask));

        Process process = currentTask.getProcess();
        for (Task task : process.getTasks()) {
            if (correctionTask.getOrdering() < task.getOrdering() && task.getOrdering() < currentTask.getOrdering()) {
                assertEquals("Report of problem was incorrect - tasks between were not set up to locked!", TaskStatus.LOCKED, task.getProcessingStatusEnum());
            }
        }

        // set up tasks to previous states
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesForWorkflowFull();
    }

    @Test
    public void shouldSolveProblem() throws Exception {
        Problem problem = new Problem();
        problem.setId(1);
        problem.setMessage("Fix it!");

        Solution solution = new Solution();
        solution.setId(1);
        solution.setMessage("Fixed");

        workflowService.setProblem(problem);
        workflowService.setSolution(solution);

        Task currentTask = taskService.getById(6);
        workflowService.reportProblem(currentTask);
        workflowService.solveProblem(currentTask);

        Task correctionTask = taskService.getById(1);

        Process process = currentTask.getProcess();
        for (Task task : process.getTasks()) {
            if (correctionTask.getOrdering() < task.getOrdering() && task.getOrdering() < currentTask.getOrdering()) {
                assertEquals("Solve of problem was incorrect - tasks between were not set up to done!", TaskStatus.DONE, task.getProcessingStatusEnum());
            }
        }

        // set up tasks to previous states
        MockDatabase.cleanDatabase();
        MockDatabase.insertProcessesForWorkflowFull();
    }
}
