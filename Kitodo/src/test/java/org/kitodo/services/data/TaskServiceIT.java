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

package org.kitodo.services.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.dto.TaskDTO;

/**
 * Tests for TaskService class.
 */
public class TaskServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllTasks() throws Exception {
        TaskService taskService = new TaskService();

        Long amount = taskService.count();
        assertEquals("Tasks were not counted correctly!", Long.valueOf(6), amount);
    }

    @Test
    public void shouldCountTasksAccordingToQuery() throws Exception {
        TaskService taskService = new TaskService();
        UserService userService = new UserService();

        Long amount = taskService.getAmountOfCurrentTasks(true, true, userService.getById(1));
        assertEquals("Tasks were not counted correctly!", Long.valueOf(2), amount);

        amount = taskService.getAmountOfCurrentTasks(true, false, userService.getById(1));
        assertEquals("Tasks were not counted correctly!", Long.valueOf(1), amount);

        amount = taskService.getAmountOfCurrentTasks(false, true, userService.getById(1));
        assertEquals("Tasks were not counted correctly!", Long.valueOf(1), amount);

        amount = taskService.getAmountOfCurrentTasks(true, false, userService.getById(2));
        assertEquals("Tasks were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForTasks() throws Exception {
        TaskService taskService = new TaskService();

        Long amount = taskService.countDatabaseRows();
        assertEquals("Tasks were not counted correctly!", Long.valueOf(6), amount);
    }

    @Test
    public void shouldFindTask() throws Exception {
        TaskService taskService = new TaskService();

        TaskDTO task = taskService.findById(1);
        boolean condition = task.getTitle().equals("Testing") && task.getPriority().equals(1);
        assertTrue("Task was not found in index!", condition);
    }

    @Test
    public void shouldFindAllTasks() throws Exception {
        TaskService taskService = new TaskService();

        List<TaskDTO> tasks = taskService.findAll();
        assertEquals("Not all tasks were found in index!", 6, tasks.size());
    }

    @Test
    public void shouldGetTask() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(1);
        boolean condition = task.getTitle().equals("Testing") && task.getPriority().equals(1);
        assertTrue("Task was not found in database!", condition);
    }

    @Test
    public void shouldGetAllTasks() {
        TaskService taskService = new TaskService();

        List<Task> tasks = taskService.getAll();
        assertEquals("Not all tasks were found in database!", 6, tasks.size());
    }

    @Test
    public void shouldGetAllTasksInGivenRange() throws Exception {
        TaskService taskService = new TaskService();

        List<Task> tasks = taskService.getAll(1,3);
        assertEquals("Not all tasks were found in database!", 3, tasks.size());
    }

    @Test
    public void shouldFindByProcessingStatusAndUser() throws Exception {
        TaskService taskService = new TaskService();

        List<JSONObject> tasks = taskService.findByProcessingStatusAndUser(TaskStatus.INWORK, 1, null);
        assertEquals("Some tasks were found in database!", 0, tasks.size());

        tasks = taskService.findByProcessingStatusAndUser(TaskStatus.INWORK, 2, null);
        assertEquals("Not all tasks were found in database!", 2, tasks.size());

    }

    @Test
    public void shouldRemoveTask() throws Exception {
        TaskService taskService = new TaskService();

        Task task = new Task();
        task.setTitle("To Remove");
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        taskService.save(task);
        Task foundTask = taskService.getById(7);
        assertEquals("Additional task was not inserted in database!", "To Remove", foundTask.getTitle());

        taskService.remove(foundTask);
        exception.expect(DAOException.class);
        taskService.getById(7);

        task = new Task();
        task.setTitle("To remove");
        task.setProcessingStatusEnum(TaskStatus.OPEN);
        taskService.save(task);
        foundTask = taskService.getById(8);
        assertEquals("Additional task was not inserted in database!", "To remove", foundTask.getTitle());

        taskService.remove(7);
        exception.expect(DAOException.class);
        taskService.getById(8);
    }

    @Test
    public void shouldGetProcessingBeginAsFormattedString() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(1);
        String expected = "2016-10-20 00:00:00";
        String actual = taskService.getProcessingBeginAsFormattedString(task);
        assertEquals("Processing time date is incorrect!", expected, actual);
    }

    @Test
    public void shouldGetProcessingTimeAsFormattedString() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(1);
        String expected = "2016-12-24 00:00:00";
        String actual = taskService.getProcessingTimeAsFormattedString(task);
        assertEquals("Processing time date is incorrect!", expected, actual);

        task = taskService.getById(2);
        expected = "-";
        actual = taskService.getProcessingTimeAsFormattedString(task);
        assertEquals("Processing time date is incorrect!", expected, actual);
    }

    @Test
    public void shouldGetProcessingEndAsFormattedString() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(1);
        String expected = "2016-12-24 00:00:00";
        String actual = taskService.getProcessingEndAsFormattedString(task);
        assertEquals("Processing end date is incorrect!", expected, actual);
    }

    @Test
    public void shouldGetCorrectionStep() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(2);
        assertTrue("Task is not correction task!", taskService.isCorrectionStep(task));
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(3);
        String expected = "Testing_and_Blocking";
        String actual = taskService.getNormalizedTitle(task.getTitle());
        assertEquals("Normalized title of task doesn't match given plain text!", expected, actual);
    }

    @Test
    public void shouldGetTitleWithUserName() throws Exception {
        TaskService taskService = new TaskService();

        Task task = taskService.getById(1);
        String expected = "Testing (Kowalski, Jan)";
        String actual = taskService.getTitleWithUserName(task);
        assertEquals("Task's title with user name doesn't match given plain text!", expected, actual);

        task = taskService.getById(3);
        expected = "Testing and Blocking";
        actual = taskService.getTitleWithUserName(task);
        assertEquals("Task's title with user name doesn't match given plain text!", expected, actual);
    }

    @Test
    public void shouldGetCurrentTasksOfBatch() {
        TaskService taskService = new TaskService();

        List<Task> tasks = taskService.getCurrentTasksOfBatch("Task", 1);
        System.out.println("shouldGetByTitleAndBatches: " + tasks.size());
    }

    @Test
    public void shouldGetAllTasksInBetween() {
        TaskService taskService = new TaskService();

        List<Task> tasks = taskService.getAllTasksInBetween(2, 3, 2);
        int actual = tasks.size();
        int expected = 2;
        assertEquals("Task's list size is incorrect!", expected, actual);
    }

    @Test
    public void shouldGetNextTasksForProblemSolution() {
        TaskService taskService = new TaskService();

        List<Task> tasks = taskService.getNextTasksForProblemSolution(2, 2);
        int actual = tasks.size();
        int expected = 1;
        assertEquals("Task's list size is incorrect!", expected, actual);
    }

    @Test
    public void shouldGetPreviousTaskForProblemReporting() {
        TaskService taskService = new TaskService();

        List<Task> tasks = taskService.getPreviousTasksForProblemReporting(2, 2);
        int actual = tasks.size();
        int expected = 1;
        assertEquals("Task's list size is incorrect!", expected, actual);
    }

    @Test
    public void shouldFindDistinctTitles() throws Exception {
        TaskService taskService = new TaskService();

        List<String> taskTitlesDistinct = taskService.findTaskTitlesDistinct();
        int size = taskTitlesDistinct.size();
        assertEquals("Incorrect size of distinct titles for tasks!", 5, size);

        String title = taskTitlesDistinct.get(0);
        assertEquals("Incorrect sorting of distinct titles for tasks!", "Blocking", title);

        title = taskTitlesDistinct.get(1);
        assertEquals("Incorrect sorting of distinct titles for tasks!", "Closed", title);
    }
}
