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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.WorkflowCondition;
import org.kitodo.data.database.converter.ProcessConverter;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.CorrectionComments;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.test.utils.ProcessTestUtils;

public class WorkflowControllerServiceIT {

    private static final File scriptCreateDirUserHome = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
    private static final File scriptCreateSymLink = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_SYMLINK));
    private static final File scriptDeleteSymLink = new File(
            ConfigCore.getParameter(ParameterCore.SCRIPT_DELETE_SYMLINK));
    private static final File scriptNotWorking = new File("src/test/resources/scripts/not_working_script.sh");
    private static final File scriptWorking = new File("src/test/resources/scripts/working_script.sh");
    private static final File usersDirectory = new File("src/test/resources/users");
    private static final FileService fileService = ServiceManager.getFileService();
    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final WorkflowControllerService workflowService = new WorkflowControllerService();
    private static int workflowTestProcessId = -1;
    private static int workflowTestProcessId2 = -1;
    private static final String METADATA_TEST_FILENAME = "testMetadataForNonBlockingParallelTasksTest.xml";

    @BeforeEach
    public void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        workflowTestProcessId = Math.toIntExact(ServiceManager.getProcessService().count());
        ProcessTestUtils.copyTestFiles(workflowTestProcessId, "testmetaNewspaper.xml");
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        Task task = ServiceManager.getTaskService().getById(35);
        workflowTestProcessId2 = task.getProcess().getId();
        ProcessTestUtils.copyTestMetadataFile(workflowTestProcessId2, METADATA_TEST_FILENAME);

        usersDirectory.mkdir();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
            ExecutionPermission.setExecutePermission(scriptCreateSymLink);
            ExecutionPermission.setExecutePermission(scriptDeleteSymLink);
            ExecutionPermission.setExecutePermission(scriptNotWorking);
            ExecutionPermission.setExecutePermission(scriptWorking);
        }
    }

    @AfterEach
    public void cleanDatabase() throws Exception {
        ProcessTestUtils.removeTestProcess(workflowTestProcessId);
        ProcessTestUtils.removeTestProcess(workflowTestProcessId2);
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();

        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
            ExecutionPermission.setNoExecutePermission(scriptCreateSymLink);
            ExecutionPermission.setNoExecutePermission(scriptDeleteSymLink);
            ExecutionPermission.setNoExecutePermission(scriptNotWorking);
            ExecutionPermission.setNoExecutePermission(scriptWorking);
        }

        usersDirectory.delete();
    }

    @Test
    public void shouldSetTaskStatusUp() throws Exception {
        Task task = taskService.getById(10);

        workflowService.setTaskStatusUp(task);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status was not set up!");

        workflowService.setTaskStatusDown(task);
        taskService.save(task);
    }

    @Test
    public void shouldSetTasksStatusUp() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        List<Task> tasks = process.getTasks();
        assertEquals(TaskStatus.OPEN, tasks.get(3).getProcessingStatus(), "Task '" + tasks.get(3).getTitle() + "' status should be OPEN!");
        assertEquals(TaskStatus.INWORK, tasks.get(2).getProcessingStatus(), "Task '" + tasks.get(2).getTitle() + "' status should be INWORK!");

        workflowService.setTasksStatusUp(process);
        for (Task task : process.getTasks()) {
            if (Objects.equals(task.getId(), 9)) {
                assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status was not set up!");
            } else if (Objects.equals(task.getId(), 10)) {
                assertEquals(TaskStatus.LOCKED, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status should not be set up!");
            } else {
                assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' status was not set up!");
            }
        }
    }

    @Test
    public void shouldSetTasksStatusDown() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        //Due to testszenario there are multiple current tasks, so task with id 2 is set down twice (inwork->open->locked)
        List<Task> tasks = process.getTasks();
        assertEquals(TaskStatus.OPEN, tasks.get(3).getProcessingStatus(), "Task '" + tasks.get(3).getTitle() + "' status should be OPEN!");
        assertEquals(TaskStatus.INWORK, tasks.get(2).getProcessingStatus(), "Task '" + tasks.get(2).getTitle() + "' status should be INWORK!");

        workflowService.setTasksStatusDown(process);
        tasks = process.getTasks();
        assertEquals(TaskStatus.LOCKED, tasks.get(3).getProcessingStatus(), "Task '" + tasks.get(3).getTitle() + "' status was not set down!");
        assertEquals(TaskStatus.LOCKED, tasks.get(2).getProcessingStatus(), "Task '" + tasks.get(2).getTitle() + "' status was not set down!");
    }

    @Test
    public void shouldClose() throws Exception {
        Task task = taskService.getById(9);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        Task nextTask = taskService.getById(10);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");
    }

    @Test
    public void shouldCloseForProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(19);
        ProcessTestUtils.copyTestMetadataFile(task.getProcess().getId(), ProcessTestUtils.testFileChildProcessToKeep);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        // Task 2 and 4 are set up to open because they are concurrent and conditions
        // were evaluated to true
        Task nextTask = taskService.getById(20);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        // Task 3 has XPath which evaluates to false - it gets immediately closed
        nextTask = taskService.getById(21);
        assertEquals(TaskStatus.DONE, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to done!");

        nextTask = taskService.getById(22);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        nextTask = taskService.getById(23);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was set up to open!");

        ProcessTestUtils.removeTestProcess(task.getProcess().getId());
    }

    @Test
    public void shouldCloseForInWorkProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(25);
        ProcessTestUtils.copyTestMetadataFile(task.getProcess().getId(), ProcessTestUtils.testFileChildProcessToKeep);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        // Task 3 has XPath which evaluates to false - it gets immediately closed
        Task nextTask = taskService.getById(26);
        assertEquals(TaskStatus.DONE, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to done!");

        // Task 3 and 4 are concurrent - 3 got immediately finished, 4 is set to open
        nextTask = taskService.getById(27);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        nextTask = taskService.getById(28);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was set up to open!");
        ProcessTestUtils.removeTestProcess(task.getProcess().getId());
    }

    @Test
    public void shouldCloseForInWorkProcessWithBlockingParallelTasks() throws Exception {
        Task task = taskService.getById(30);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        Task nextTask = taskService.getById(31);
        assertEquals(TaskStatus.INWORK, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' is not in work!");

        nextTask = taskService.getById(32);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set to locked!");

        nextTask = taskService.getById(33);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was set up to open!");
    }

    @Test
    public void shouldCloseForInWorkProcessWithNonBlockingParallelTasks() throws Exception {
        Task task = taskService.getById(35);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        Task nextTask = taskService.getById(36);
        assertEquals(TaskStatus.INWORK, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' is not in work!");

        nextTask = taskService.getById(37);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        nextTask = taskService.getById(38);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was set up to open!");
    }

    @Test
    public void shouldCloseForAlmostFinishedProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(42);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        Task nextTask = taskService.getById(43);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");
    }

    //TODO: find out why it doesn't work in github ci
    @Disabled("Doesn't work on gitHub ci")
    @Test
    public void shouldCloseAndAssignNextForProcessWithParallelTasks() throws Exception {
        Task task = taskService.getById(44);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        // Task 2 and 4 are set up to open because they are concurrent and conditions
        // were evaluated to true
        Task nextTask = taskService.getById(45);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        // Task 3 has XPath which evaluates to false - it gets immediately closed
        nextTask = taskService.getById(46);
        assertEquals(TaskStatus.DONE, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to done!");

        nextTask = taskService.getById(47);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        nextTask = taskService.getById(48);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was set up to open!");

        fileService.createDirectory(URI.create("9"), "images");

        workflowService.assignTaskToUser(taskService.getById(45));

        fileService.delete(URI.create("9/images"));

        // Task 4 should be kept open
        Task nextConcurrentTask = taskService.getById(47);
        assertEquals(TaskStatus.OPEN, nextConcurrentTask.getProcessingStatus(), "Task '" + nextConcurrentTask.getTitle() + "' was not kept to open!");
    }

    @Test
    public void shouldCloseForProcessWithScriptParallelTasks() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS);
        // if you want to execute test on windows change sh to bat in
        // gateway-test5.bpmn20.xml

        Task task = taskService.getById(54);

        workflowService.close(task);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not closed!");

        // Task 2 and 4 are set up to open because they are concurrent and conditions
        // were evaluated to true
        Task nextTask = taskService.getById(55);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        // Task 3 has Script which evaluates to false - it gets immediately closed
        nextTask = taskService.getById(56);
        assertEquals(TaskStatus.DONE, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to done!");

        nextTask = taskService.getById(57);
        assertEquals(TaskStatus.OPEN, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was not set up to open!");

        nextTask = taskService.getById(58);
        assertEquals(TaskStatus.LOCKED, nextTask.getProcessingStatus(), "Task '" + nextTask.getTitle() + "' was set up to open!");
    }

    @Test
    public void shouldCloseForProcessWithSkippedTask() throws DAOException, IOException {
        int processId = MockDatabase.insertTestProcess("Test process", 1, 1, 1);
        Process process = ServiceManager.getProcessService().getById(processId);
        process.getTasks().clear();
        ProcessTestUtils.copyTestMetadataFile(processId, ProcessTestUtils.testFileForHierarchyParent);
        WorkflowCondition workflowCondition = new WorkflowCondition("xpath", "/mets:nothing");
        ServiceManager.getWorkflowConditionService().save(workflowCondition);
        Task taskToClose =  createAndSaveTask(TaskStatus.INWORK, 1, process, null);
        Task skippedTask = createAndSaveTask(TaskStatus.LOCKED, 2, process, workflowCondition);
        Task secondSkippedTask = createAndSaveTask(TaskStatus.LOCKED, 2, process, workflowCondition);
        Task thirdTaskToSkip = createAndSaveTask(TaskStatus.LOCKED, 3, process, workflowCondition);
        Task taskToOpen = createAndSaveTask(TaskStatus.LOCKED, 3, process, null);
        Task secondTaskToOpen = createAndSaveTask(TaskStatus.LOCKED, 3, process, null);

        List<Task> tasks = process.getTasks();
        tasks.add(taskToClose);
        tasks.add(skippedTask);
        tasks.add(secondSkippedTask);
        tasks.add(thirdTaskToSkip);
        tasks.add(taskToOpen);
        tasks.add(secondTaskToOpen);

        ServiceManager.getProcessService().save(process);

        workflowService.close(taskToClose);

        assertEquals(TaskStatus.DONE, taskToClose.getProcessingStatus(), "Task '" + taskToClose.getTitle() + "' was not closed!");
        taskService.refresh(skippedTask);
        assertEquals(TaskStatus.DONE, skippedTask.getProcessingStatus(), "Task '" + skippedTask.getTitle() + "' was not skipped!");
        taskService.refresh(secondSkippedTask);
        assertEquals(TaskStatus.DONE, secondSkippedTask.getProcessingStatus(), "Task '" + secondSkippedTask.getTitle() + "' was not skipped!");
        taskService.refresh(taskToOpen);
        assertEquals(TaskStatus.OPEN, taskToOpen.getProcessingStatus(), "Task '" + taskToOpen.getTitle() + "' was not opened!");
        taskService.refresh(secondTaskToOpen);
        assertEquals(TaskStatus.OPEN, secondTaskToOpen.getProcessingStatus(), "Task '" + secondTaskToOpen.getTitle() + "' was not opened!");
        taskService.refresh(thirdTaskToSkip);
        assertEquals(TaskStatus.DONE, thirdTaskToSkip.getProcessingStatus(), "Task '" + thirdTaskToSkip.getTitle() + "' was not skipped!");

        process.getTasks().clear();
        ProcessTestUtils.removeTestProcess(processId);
    }

    /**
     * Checks that a task is not closed if it is set up to trigger image validation, 
     * the task is set up to be verified when closed, and the image validation fails.
     * 
     * @throws DAOException if loading process fails
     */
    @Test
    public void shouldNotCloseWhenImageValidationFails() throws DAOException {
        Process process = ServiceManager.getProcessService().getById(1);

        Task task = new Task();
        task.setTypeCloseVerify(true);
        task.setTypeValidateImages(true);
        task.setProcess(process);

        assertThrows(DAOException.class, () -> workflowService.closeTaskByUser(task), "image validation should fail and raise exception");
        assertNotEquals(TaskStatus.DONE, task.getProcessingStatus(), "task should not be marked as done");
    }

    private Task createAndSaveTask(TaskStatus taskStatus, int ordering, Process process,
            WorkflowCondition workflowCondition) throws DAOException {
        Task task = new Task();
        task.setProcessingStatus(taskStatus);
        task.setOrdering(ordering);
        task.setProcess(process);
        task.setWorkflowCondition(workflowCondition);
        taskService.save(task);
        return task;
    }

    @Test
    public void shouldAssignTaskToUser() throws Exception {
        fileService.createDirectory(URI.create(""), "1");
        fileService.createDirectory(URI.create("1"), "images");

        Task task = taskService.getById(6);

        workflowService.assignTaskToUser(task);
        assertEquals(Integer.valueOf(1), task.getProcessingUser().getId(), "Incorrect user was assigned to the task!");

        fileService.delete(URI.create("1/images"));
        fileService.delete(URI.create("1"));
    }

    @Test
    public void shouldUnassignTaskFromUser() throws Exception {
        Task task = taskService.getById(6);

        workflowService.unassignTaskFromUser(task);
        assertNull(task.getProcessingUser(), "User was not unassigned from the task!");
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task was not set up to open after unassing of the user!");
    }

    @Test
    public void shouldReportProblem() throws Exception {
        Task currentTask = taskService.getById(8);
        Task correctionTask = taskService.getById(6);

        Comment problem = new Comment();
        problem.setMessage("Fix it!");
        problem.setAuthor(ServiceManager.getUserService().getById(1));
        problem.setCurrentTask(currentTask);
        problem.setCorrectionTask(correctionTask);
        problem.setProcess(currentTask.getProcess());
        problem.setType(CommentType.ERROR);
        problem.setCorrected(Boolean.FALSE);
        problem.setCreationDate(new Date());

        ServiceManager.getCommentService().save(problem);

        workflowService.reportProblem(problem, TaskEditType.MANUAL_SINGLE);

        assertEquals(TaskStatus.OPEN, correctionTask.getProcessingStatus(), "Report of problem was incorrect - task '" + correctionTask.getTitle() + "' is not set up to open!");

        assertTrue(correctionTask.isCorrection(), "Report of problem was incorrect - task '" + correctionTask.getTitle() + "' is not a correction task!");

        Process process = problem.getCurrentTask().getProcess();
        for (Task task : process.getTasks()) {
            if (correctionTask.getOrdering() < task.getOrdering() && task.getOrdering() < currentTask.getOrdering()) {
                assertEquals(TaskStatus.LOCKED, task.getProcessingStatus(), "Report of problem was incorrect - tasks between were not set up to locked!");
            }
        }
    }

    @Test
    public void shouldSolveProblem() throws Exception {

        Comment correctionComment = prepareCorrectionComment();
        Task currentTask = correctionComment.getCurrentTask();
        Task correctionTask = correctionComment.getCorrectionTask();

        workflowService.reportProblem(correctionComment, TaskEditType.MANUAL_SINGLE);
        ServiceManager.getCommentService().refresh(correctionComment);
        workflowService.solveProblem(correctionComment, TaskEditType.MANUAL_SINGLE);

        Process process = ServiceManager.getProcessService().getById(currentTask.getProcess().getId());
        for (Task task : process.getTasks()) {
            if (correctionComment.getCorrectionTask().getOrdering() < task.getOrdering()
                    && task.getOrdering() < correctionComment.getCurrentTask().getOrdering()) {
                assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Solving reported problem was unsuccessful - tasks between '"
                                + correctionTask.getTitle() + "' and '" + currentTask.getTitle()
                                + "' were not set to processing status DONE!");
            }
        }

        assertEquals(TaskStatus.DONE, correctionComment.getCorrectionTask().getProcessingStatus(), "Solving reported problem was unsuccessful - correction task '" + correctionTask.getTitle()
                        + "' was not set to processing status DONE!");

        assertEquals(TaskStatus.OPEN, correctionComment.getCurrentTask().getProcessingStatus(), "Solving reported problem was unsuccessful - current task '" + currentTask.getTitle()
                + "' was not set to processing status 'OPEN'!");
    }

    @Test
    public void shouldGetCorrectCommentStatus() throws DAOException, IOException {
        Comment correctionComment = prepareCorrectionComment();
        workflowService.reportProblem(correctionComment, TaskEditType.MANUAL_SINGLE);
        assertEquals(CorrectionComments.OPEN_CORRECTION_COMMENTS, ProcessConverter
                .getCorrectionCommentStatus(correctionComment.getProcess()));
        workflowService.solveProblem(correctionComment, TaskEditType.MANUAL_SINGLE);
        assertEquals(CorrectionComments.NO_OPEN_CORRECTION_COMMENTS, ProcessConverter
                .getCorrectionCommentStatus(correctionComment.getProcess()));
    }

    private Comment prepareCorrectionComment() throws DAOException {
        Task currentTask = taskService.getById(8);
        Task correctionTask = taskService.getById(6);

        Comment correctionComment = new Comment();
        correctionComment.setMessage("Fix it!");
        correctionComment.setAuthor(ServiceManager.getUserService().getById(1));
        correctionComment.setCurrentTask(currentTask);
        correctionComment.setCorrectionTask(correctionTask);
        correctionComment.setProcess(currentTask.getProcess());
        correctionComment.setType(CommentType.ERROR);
        correctionComment.setCorrected(Boolean.FALSE);
        correctionComment.setCreationDate(new Date());

        ServiceManager.getCommentService().save(correctionComment);

        return correctionComment;
    }
}
