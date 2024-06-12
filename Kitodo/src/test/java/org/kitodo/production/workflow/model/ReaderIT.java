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

package org.kitodo.production.workflow.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.workflow.model.beans.TaskInfo;

public class ReaderIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();

        FileLoader.createExtendedDiagramTestFile();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        FileLoader.deleteExtendedDiagramTestFile();

        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldLoadProcess() throws Exception {
        Reader reader = new Reader("extended-test");

        boolean result = Objects.nonNull(reader.getModelInstance());
        assertTrue(result, "Process definition was not loaded!");
    }

    @Test
    public void shouldReadWorkflow() throws Exception {
        Reader reader = new Reader("extended-test");

        reader.readWorkflowTasks();
        Map<Task, TaskInfo> tasks = reader.getTasks();
        assertEquals(2, tasks.size(), "Process definition - workflow was read incorrectly!");

        Set<Map.Entry<Task, TaskInfo>> taskEntries = tasks.entrySet();
        Map.Entry<Task, TaskInfo>[] entry = new Map.Entry[2];
        taskEntries.toArray(entry);

        Task task = entry[0].getKey();
        TaskInfo taskInfo = entry[0].getValue();
        assertCorrectTask(task, taskInfo, "Say hello", 1, "");

        task = entry[1].getKey();
        taskInfo = entry[1].getValue();
        assertCorrectTask(task, taskInfo, "Execute script", 2, "");
    }

    @Test
    public void shouldReadConditionalWorkflow() throws Exception {
        Reader reader = new Reader("gateway-test1");

        reader.readWorkflowTasks();
        Map<Task, TaskInfo> tasks = reader.getTasks();
        assertEquals(5, tasks.size(), "Process definition - workflow was read incorrectly!");

        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            Task task = entry.getKey();
            TaskInfo taskInfo = entry.getValue();
            String title = task.getName();
            switch (title) {
                case "Task1":
                    assertCorrectTask(task, taskInfo, "Task1", 1, "");
                    assertFalse(taskInfo.isLast(), "Process definition - workflow's task last property were determined incorrectly!");
                    break;
                case "ScriptTask":
                    assertCorrectTask(task, taskInfo, "ScriptTask", 2, "${type==1}");
                    break;
                case "Task3":
                    assertCorrectTask(task, taskInfo, "Task3", 2, "${type==2}");
                    break;
                case "Task4":
                    assertCorrectTask(task, taskInfo, "Task4", 2, "default");
                    break;
                case "Task5":
                    assertCorrectTask(task, taskInfo, "Task5", 3, "");
                    assertTrue(taskInfo.isLast(), "Process definition - workflow's task last property were determined incorrectly!");
                    break;
                default:
                    fail("Task should have one of the above titles!");
                    break;
            }
        }
    }

    @Test
    public void shouldNotReadConditionalWorkflow() throws Exception {
        Reader reader = new Reader("gateway-test2");

        Exception exception = assertThrows(WorkflowException.class, () -> reader.readWorkflowTasks());
        assertEquals(Helper.getTranslation("workflowExceptionParallelBranch", "Task9"), exception.getMessage());
    }

    @Test
    public void shouldReadConditionalWorkflowWithTwoEnds() throws Exception {
        Reader reader = new Reader("gateway-test3");

        reader.readWorkflowTasks();
        Map<Task, TaskInfo> tasks = reader.getTasks();
        assertEquals(7, tasks.size(), "Process definition - workflow was read incorrectly!");

        for (Map.Entry<Task, TaskInfo> entry : tasks.entrySet()) {
            Task task = entry.getKey();
            TaskInfo taskInfo = entry.getValue();
            String title = task.getName();
            switch (title) {
                case "Task1":
                    assertCorrectTask(task, taskInfo, "Task1", 1, "");
                    assertFalse(taskInfo.isLast(), "Process definition - workflow's task last property were determined incorrectly!");
                    break;
                case "Task2":
                    assertCorrectTask(task, taskInfo, "Task2", 2, "");
                    break;
                case "Task3":
                    assertCorrectTask(task, taskInfo, "Task3", 3, "type=2");
                    assertFalse(taskInfo.isLast(), "Process definition - workflow's task last property were determined incorrectly!");
                    break;
                case "Task4":
                    assertCorrectTask(task, taskInfo, "Task4", 4, "type=2");
                    break;
                case "Task5":
                    assertCorrectTask(task, taskInfo, "Task5", 4, "type=2");
                    break;
                case "Task6":
                    assertCorrectTask(task, taskInfo, "Task6", 5, "type=2");
                    assertTrue(taskInfo.isLast(), "Process definition - workflow's task last property were determined incorrectly!");
                    break;
                case "Task7":
                    assertCorrectTask(task, taskInfo, "Task7", 3, "type=1");
                    assertTrue(taskInfo.isLast(), "Process definition - workflow's task last property were determined incorrectly!");
                    break;
                default:
                    fail("Task should have one of the above titles!");
                    break;
            }
        }
    }

    @Test
    public void shouldNotReadWorkflowWithLoop() throws Exception {
        Reader reader = new Reader("gateway-test6");

        Exception exception = assertThrows(WorkflowException.class, () -> reader.readWorkflowTasks());
        assertEquals(Helper.getTranslation("workflowExceptionLoop", "Task1"), exception.getMessage());
    }

    private void assertCorrectTask(Task task, TaskInfo taskInfo, String title, int ordering, String condition) {
        assertEquals(title, task.getName(), "Process definition - workflow's task title was read incorrectly!");
        assertEquals(ordering, taskInfo.getOrdering(), "Process definition - workflow's task ordering was determined incorrectly!");
    }
}
