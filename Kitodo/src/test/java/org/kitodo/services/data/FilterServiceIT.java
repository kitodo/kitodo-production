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

import de.sub.goobi.helper.Helper;

import static org.awaitility.Awaitility.await;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.TaskDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.services.ServiceManager;

/**
 * Integration tests for FilterService.
 */
public class FilterServiceIT {

    private static final FilterService filterService = new ServiceManager().getFilterService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(new ServiceManager().getUserService().getById(1));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldCountAllFilters() {
        await().untilAsserted(
            () -> assertEquals("Filters were not counted correctly!", Long.valueOf(2), filterService.count()));
    }

    @Test
    public void shouldCountAllFiltersAccordingToQuery() {
        String query = matchQuery("value", "\"id:1\"").operator(Operator.AND).toString();
        await().untilAsserted(
            () -> assertEquals("Filters were not counted correctly!", Long.valueOf(1), filterService.count(query)));
    }

    @Test
    public void shouldCountAllDatabaseRowsForFilters() throws Exception {
        Long amount = filterService.countDatabaseRows();
        assertEquals("Filters were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldGetFilterById() throws Exception {
        Filter filter = filterService.getById(1);
        String actual = filter.getValue();
        String expected = "\"id:1\"";
        assertEquals("Filter was not found in database!", expected, actual);
    }

    @Test
    public void shouldGetAllFilters() {
        List<Filter> filters = filterService.getAll();
        assertEquals("Not all filters were found in database!", 2, filters.size());
    }

    @Test
    public void shouldGetAllFiltersInGivenRange() throws Exception {
        List<Filter> filters = filterService.getAll(1, 10);
        assertEquals("Not all filters were found in database!", 1, filters.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessId() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:2\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes with id equal 2!", 1,
            processService.findByQuery(firstQuery, true).size()));

        await().untilAsserted(() -> assertEquals("Incorrect id for found process!", Integer.valueOf(2),
            processService.findByQuery(firstQuery, true).get(0).getId()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1 2\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes with id equal 1 or 2!", 2,
            processService.findByQuery(secondQuery, true).size()));

        await().untilAsserted(() -> assertEquals("Incorrect id for found process!", Integer.valueOf(2),
            processService.findByQuery(secondQuery, true).get(0).getId()));

        // TODO: here should be 2
        QueryBuilder thirdQuery = filterService.queryBuilder("\"id:2 3\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes with id equal 2 or 3!", 1,
            processService.findByQuery(thirdQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProjectTitle() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for project with title containing 'First'!", 2,
                processService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:Second\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for project with title containing 'Second'!", 0,
                processService.findByQuery(secondQuery, true).size()));

        // it has only 2 templates - no processes
        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:Inactive\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for project with title containing 'Inactive'!", 0,
                processService.findByQuery(thirdQuery, true).size()));

        QueryBuilder fourthQuery = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.PROCESS, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for project with with title containing 'First Inactive'!",
                0, processService.findByQuery(fourthQuery, true).size()));

        QueryBuilder fifthQuery = filterService.queryBuilder("\"project:First project\"", ObjectType.PROCESS, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for project with with title containing 'First Project'!",
                2, processService.findByQuery(fifthQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessTitle() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"process:process\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for title containing 'process'!", 2,
            processService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"process:First\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for title containing 'First'!", 1,
            processService.findByQuery(secondQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByTaskTitle() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"step:Testing\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for title containing 'Testing'!", 0,
            processService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Blocking\"", ObjectType.PROCESS, false,
            false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for title containing 'Blocking'!", 1,
            processService.findByQuery(secondQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByBatchId() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"batch:1\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for batch with id 1!", 1,
            processService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"batch:1 2\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for batch with id 1 or 2!", 1,
            processService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-batch:1 2\"", ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of processes for batch with id 1 or 2!", 2,
            processService.findByQuery(thirdQuery, true).size()));
    }

    // TODO: filters are not working for search only by title
    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProperty() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.PROCESS, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for property with value containing 'fix'!", 1,
                processService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"processproperty:value\"", ObjectType.PROCESS, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for property with value containing 'value'!", 1,
                processService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.PROCESS,
            false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of processes for property with title 'Process' and value containing 'value'!", 1,
            processService.findByQuery(thirdQuery, true).size()));

        QueryBuilder fourthQuery = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.PROCESS,
            false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!", 1,
            processService.findByQuery(fourthQuery, true).size()));

        QueryBuilder fifthQuery = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"",
            ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!", 1,
            processService.findByQuery(fifthQuery, true).size()));

        QueryBuilder sixthQuery = filterService.queryBuilder("\"-processproperty:fix\"", ObjectType.PROCESS, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of processes for property with value not containing 'fix'!", 2,
                processService.findByQuery(sixthQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByMultipleConditions() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!", 1,
            processService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:First project\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!", 1,
            processService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:First process\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!", 0,
            processService.findByQuery(thirdQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessId() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:1\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks for process with id equal 1!", 2,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1\"", ObjectType.TASK, true, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of open tasks for process with id equal 1!", 1,
            taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"id:1 2\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks for process with id equal to 1 or 2!", 4,
            taskService.findByQuery(thirdQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProjectTitle() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks for project with title containing 'First'!",
            4, taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:Inactive\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for project with title containing 'Inactive'!", 0,
                taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.TASK, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for project with title containing 'First Inactive'!", 0,
                taskService.findByQuery(thirdQuery, true).size()));

        QueryBuilder fourthQuery = filterService.queryBuilder("\"project:First project\"", ObjectType.TASK, true,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for project with title containing 'First project'!", 2,
                taskService.findByQuery(fourthQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessTitle() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks for process with title containing 'First'!",
            2, taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, true, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks for process with title containing 'First'!",
            1, taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for process with title containing 'Second process'!", 2,
                taskService.findByQuery(thirdQuery, true).size()));

        QueryBuilder fourthQuery = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, true,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for process with title containing 'Second process'!", 1,
                taskService.findByQuery(fourthQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByTaskTitle() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        // TODO: why "step" creates something called historical filter?
        QueryBuilder query = filterService.queryBuilder("\"step:Testing\"", ObjectType.TASK, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        //assertEquals("Incorrect amount of tasks with title containing 'Testing'!", 1, taskDTOS.size());

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepopen:Blocking\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks with title containing 'Testing'!", 1,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Testing\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks with title containing 'Testing'!", 0,
            taskService.findByQuery(secondQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProperty() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for process property with value containing 'fix'!", 2,
                taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, true, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for process property with value containing 'fix'!", 1,
                taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"processproperty:value\"", ObjectType.TASK, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks for process property with value containing 'value'!", 2,
                taskService.findByQuery(thirdQuery, true).size()));

        QueryBuilder fourthQuery = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.TASK,
            false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of tasks for process property with title 'Process' and value containing 'value'!", 2,
            taskService.findByQuery(fourthQuery, true).size()));

        QueryBuilder fifthQuery = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.TASK,
            false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!", 2,
            taskService.findByQuery(fifthQuery, true).size()));

        QueryBuilder sixthQuery = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"",
            ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals(
            "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!", 2,
            taskService.findByQuery(sixthQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByClosedTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepdone:1\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of closed tasks with ordering 1!", 0,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepdone:Closed\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of closed tasks with title 'Closed'!", 0,
            taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepdone:Closed\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of not closed tasks with title different than 'Closed'!", 4,
                taskService.findByQuery(thirdQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByOpenTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepopen:1\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of open tasks with ordering 1!", 2,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Blocking\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of open tasks with title 'Blocking'!", 1,
            taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepopen:Blocking\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of not open tasks with title different than 'Blocking'!", 2,
                taskService.findByQuery(thirdQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByInProgressTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepinwork:3\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks in progress with ordering 3!", 1,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"-stepinwork:3\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks not in progress with ordering different than 3!", 2,
                taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepinwork:2\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of tasks not in progress with ordering different than 2!", 2,
                taskService.findByQuery(thirdQuery, true).size()));
    }

    @Ignore("problem with steplocked")
    @Test
    public void shouldBuildQueryAndFindByTaskServiceByLockedTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"steplocked:2\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of locked tasks with ordering 2!", 1,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"-steplocked:2\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of not locked tasks with ordering different than 2!",
            2, taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-steplocked:3\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of not locked tasks with ordering different than 3!",
            1, taskService.findByQuery(thirdQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByMultipleConditions() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:1\" \"-stepdone:3\"", ObjectType.TASK, false, false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of not closed tasks with ordering 4 assigned to process with id 1!", 1,
                taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1\" \"-stepdone:4\"", ObjectType.TASK, false,
            false);
        await().untilAsserted(
            () -> assertEquals("Incorrect amount of not closed tasks with ordering 4 assigned to process with id 1!", 2,
                taskService.findByQuery(secondQuery, true).size()));
    }

    @Test
    public void shouldBuildQueryForDefaultConditions() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"First\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks with default condition!", 2,
            taskService.findByQuery(firstQuery, true).size()));

        QueryBuilder secondQuery = filterService.queryBuilder("\"First\"", ObjectType.TASK, true, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of open tasks with default condition!", 1,
            taskService.findByQuery(secondQuery, true).size()));

        QueryBuilder thirdQuery = filterService.queryBuilder("\"Second\"", ObjectType.TASK, false, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of tasks with default condition!", 2,
            taskService.findByQuery(thirdQuery, true).size()));

        QueryBuilder fourthQuery = filterService.queryBuilder("\"Second\"", ObjectType.PROCESS, true, false);
        await().untilAsserted(() -> assertEquals("Incorrect amount of process with default condition!", 1,
            processService.findByQuery(fourthQuery, true).size()));

    }

    @Test
    public void shouldBuildQueryForEmptyConditions() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();
        TaskService taskService = new ServiceManager().getTaskService();

        // empty condition is not allowed and returns no results
        QueryBuilder query = filterService.queryBuilder("\"steplocked:\"", ObjectType.TASK, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with no ordering!", 0, taskDTOS.size());

        // empty condition is not allowed and returns no results
        query = filterService.queryBuilder("\"id:\"", ObjectType.PROCESS, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of process with no id!", 0, processDTOS.size());

    }
}
