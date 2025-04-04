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

package org.kitodo.production.services.data;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for TaskService class.
 */
public class TaskServiceIT {

    private static final TaskService taskService = ServiceManager.getTaskService();
    private static final int AMOUNT_TASKS = 13;

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllTasks() {
        await().untilAsserted(
            () -> assertEquals(Long.valueOf(AMOUNT_TASKS), taskService.count(), "Tasks were not counted correctly!"));
    }

    @Test
    public void shouldCountAllDatabaseRowsForTasks() throws Exception {
        Long amount = taskService.count();
        assertEquals(Long.valueOf(AMOUNT_TASKS), amount, "Tasks were not counted correctly!");
    }

    @Test
    public void shouldFindTask() {
        await().untilAsserted(() -> assertEquals("Finished", taskService.findById(1).getTitle(), "Task was not found in index!"));
    }

    @Test
    public void shouldFindAllTasks() {
        await().untilAsserted(
            () -> assertEquals(AMOUNT_TASKS, taskService.getAll().size(), "Not all tasks were found in index!"));
    }

    @Test
    public void shouldGetTask() throws Exception {
        Task task = taskService.getById(1);
        assertEquals("Finished", task.getTitle(), "Task was not found in database!");
    }

    @Test
    public void shouldGetAllTasks() throws Exception {
        List<Task> tasks = taskService.getAll();
        assertEquals(AMOUNT_TASKS, tasks.size(), "Not all tasks were found in database!");
    }

    @Test
    public void shouldGetAllTasksInGivenRange() throws Exception {
        List<Task> tasks = taskService.getAll(1, 3);
        assertEquals(3, tasks.size(), "Not all tasks were found in database!");
    }

    @Test
    public void shouldReplaceProcessingUser() throws Exception {
        UserService userService = ServiceManager.getUserService();

        /*int size = userService.findByProcessingTask(6, false).size();
        assertEquals("Incorrect amount of processing users!", 1, size);

        Task task = taskService.getById(7);
        taskService.replaceProcessingUser(task, null);
        taskService.save(task);

        await().pollDelay(3, TimeUnit.SECONDS).atMost(9, TimeUnit.SECONDS).untilAsserted(
            () -> assertNull("Processing user is not null!", taskService.findById(7, false).getProcessingUser()));
        size = userService.findByProcessingTask(7, false).size();
        assertEquals("Incorrect amount of processing users!", 0, size);

        task = taskService.getById(7);
        User user = userService.getById(1);
        taskService.replaceProcessingUser(task, user);
        taskService.save(task);

        await().untilAsserted(() -> assertEquals("Incorrect id of processing user!", 1,
            taskService.findById(7, false).getProcessingUser().getId().intValue()));
        size = userService.findByProcessingTask(7, false).size();
        assertEquals("Incorrect amount of processing users!", 1, size);

        task = taskService.getById(7);
        user = userService.getById(2);
        taskService.replaceProcessingUser(task, user);
        taskService.save(task);

        await().untilAsserted(() -> assertEquals("Incorrect id of processing user!", 2,
            taskService.findById(7, false).getProcessingUser().getId().intValue()));
        await().untilAsserted(() -> assertEquals("Incorrect amount of processing users!", 1,
            userService.findByProcessingTask(7, false).size()));*/
    }

    @Test
    public void shouldRemoveTask() throws Exception {
        Task task = new Task();
        task.setTitle("To Remove");
        task.setProcessingStatus(TaskStatus.OPEN);
        taskService.save(task);
        Task foundTask = taskService.getById(14);
        assertEquals("To Remove", foundTask.getTitle(), "Additional task was not inserted in database!");

        taskService.remove(foundTask);
        assertThrows(DAOException.class, () -> taskService.getById(14));

        task = new Task();
        task.setTitle("To remove");
        task.setProcessingStatus(TaskStatus.OPEN);
        taskService.save(task);
        foundTask = taskService.getById(15);
        assertEquals("To remove", foundTask.getTitle(), "Additional task was not inserted in database!");

        taskService.remove(foundTask);
        assertThrows(DAOException.class, () -> taskService.getById(14));
    }

    @Test
    public void shouldGetProcessingBeginAsFormattedString() throws Exception {
        Task task = taskService.getById(1);
        String expected = "2016-08-20 00:00:00";
        String actual = taskService.getProcessingBeginAsFormattedString(task);
        assertEquals(expected, actual, "Processing time date is incorrect!");
    }

    @Test
    public void shouldGetProcessingTimeAsFormattedString() throws Exception {
        Task task = taskService.getById(1);
        String expected = "2016-09-24 00:00:00";
        String actual = taskService.getProcessingTimeAsFormattedString(task);
        assertEquals(expected, actual, "Processing time date is incorrect!");

        task = taskService.getById(2);
        expected = "-";
        actual = taskService.getProcessingTimeAsFormattedString(task);
        assertEquals(expected, actual, "Processing time date is incorrect!");
    }

    @Test
    public void shouldGetProcessingEndAsFormattedString() throws Exception {
        Task task = taskService.getById(1);
        String expected = "2016-09-24 00:00:00";
        String actual = taskService.getProcessingEndAsFormattedString(task);
        assertEquals(expected, actual, "Processing end date is incorrect!");
    }

    @Test
    public void shouldGetCorrectionStep() throws Exception {
        Task task = taskService.getById(8);
        assertTrue(task.isRepeatOnCorrection(), "Task is not a correction task!");
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        Task task = taskService.getById(12);
        String expected = "Processed__and__Some";
        String actual = Helper.getNormalizedTitle(task.getTitle());
        assertEquals(expected, actual, "Normalized title of task doesn't match given plain text!");
    }

    @Test
    public void shouldGetTitleWithUserName() throws Exception {
        Task task = taskService.getById(6);
        String expected = "Finished (Kowalski, Jan)";
        String actual = taskService.getTitleWithUserName(task);
        assertEquals(expected, actual, "Task's title with user name doesn't match given plain text!");

        task = taskService.getById(3);
        expected = "Progress";
        actual = taskService.getTitleWithUserName(task);
        assertEquals(expected, actual, "Task's title with user name doesn't match given plain text!");
    }

    @Test
    public void shouldGetCurrentTasksOfBatch() {
        taskService.getCurrentTasksOfBatch("Task", 1);
    }

    @Test
    public void shouldGetAllTasksInBetween() {
        List<Task> tasks = taskService.getAllTasksInBetween(2, 4, 1);
        int actual = tasks.size();
        int expected = 1;
        assertEquals(expected, actual, "Task's list size is incorrect!");

        Task task = tasks.get(0);
        assertEquals(8, task.getId().intValue(), "");
        assertEquals("Progress", task.getTitle(), "");
        assertEquals(3, task.getOrdering().intValue(), "");
    }

    @Test
    public void shouldGetNextTasksForProblemSolution() {
        List<Task> tasks = taskService.getNextTasksForProblemSolution(2, 1);
        int actual = tasks.size();
        assertEquals(3, actual, "Task's list size is incorrect!");

        Task task = tasks.get(0);
        assertEquals(8, task.getId().intValue(), "");
        assertEquals("Progress", task.getTitle(), "");
        assertEquals(3, task.getOrdering().intValue(), "");
    }

    @Test
    public void shouldGetPreviousTaskForProblemReporting() {
        List<Task> tasks = taskService.getPreviousTasksForProblemReporting(2, 1);
        int actual = tasks.size();
        int expected = 1;
        assertEquals(expected, actual, "Task's list size is incorrect!");

        Task task = tasks.get(0);
        assertEquals(6, task.getId().intValue(), "");
        assertEquals("Finished", task.getTitle(), "");
        assertEquals(1, task.getOrdering().intValue(), "");
    }

    /**
     * Tests what task titles can be found in the elastic search index.
     * 
     * <p>Due to the way all distinct task title terms are extracted from the index,
     * this test will retrieve the raw tokens (including any modifications applied by 
     * ElasticSearch, e.g., a lower-case filter) and not the actual original task titles.
     * Accordingly, task titles are checked against their lower-case transformation.</p>
     */
    @Test
    public void shouldFindDistinctTitles() throws Exception {
        List<String> taskTitlesDistinct = taskService.findTaskTitlesDistinct();
        int size = taskTitlesDistinct.size();
        assertEquals(9, size, "Incorrect size of distinct titles for tasks!");

        String title = taskTitlesDistinct.get(0);
        assertEquals("Additional", title, "Incorrect sorting of distinct titles for tasks!");

        title = taskTitlesDistinct.get(1);
        assertEquals("Blocking", title, "Incorrect sorting of distinct titles for tasks!");

        title = taskTitlesDistinct.get(2);
        assertEquals("Closed", title, "Incorrect sorting of distinct titles for tasks!");
    }
}
