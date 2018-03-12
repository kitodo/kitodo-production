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

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
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

    @Test
    public void shouldCountAllFilters() throws Exception {
        Long amount = filterService.count();
        assertEquals("Filters were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllFiltersAccordingToQuery() throws Exception {
        String query = matchQuery("value", "\"id:1\"").operator(Operator.AND).toString();
        Long amount = filterService.count(query);
        assertEquals("Filters were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForFilters() throws Exception {
        Long amount = filterService.countDatabaseRows();
        assertEquals("Filters were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindFilter() throws Exception {
        Filter filter = filterService.getById(1);
        String actual = filter.getValue();
        String expected = "\"id:1\"";
        assertEquals("Filter was not found in database!", expected, actual);
    }

    @Test
    public void shouldFindAllFilters() {
        List<Filter> filters = filterService.getAll();
        assertEquals("Not all filters were found in database!", 2, filters.size());
    }

    @Test
    public void shouldGetAllFiltersInGivenRange() throws Exception {
        List<Filter> filters = filterService.getAll(1,10);
        assertEquals("Not all filters were found in database!", 1, filters.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessId() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"id:2\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes with id equal 2!", 1, processDTOS.size());

        assertEquals("Incorrect id for found process!", Integer.valueOf(2), processDTOS.get(0).getId());

        query = filterService.queryBuilder("\"id:1 2\"", ObjectType.PROCESS, false, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes with id equal 1 or 2!", 1, processDTOS.size());

        assertEquals("Incorrect id for found process!", Integer.valueOf(2), processDTOS.get(0).getId());

        query = filterService.queryBuilder("\"id:1 2\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes with id equal 1 or 2!", 1, processDTOS.size());

        assertEquals("Incorrect id for found process!", Integer.valueOf(1), processDTOS.get(0).getId());

        query = filterService.queryBuilder("\"id:2 3\"", ObjectType.PROCESS, false, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes with id equal 2 or 23!", 2, processDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProjectTitle() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"project:First\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for project with title containing 'First'!", 2, processDTOS.size());

        query = filterService.queryBuilder("\"project:First\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for project with title containing 'First'!", 1, processDTOS.size());

        query = filterService.queryBuilder("\"project:Second\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for project with title containing 'Second'!", 0,
                processDTOS.size());

        query = filterService.queryBuilder("\"project:Inactive\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for project with title containing 'Inactive'!", 1,
                processDTOS.size());

        query = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for project with with title containing 'First Inactive'!", 0,
                processDTOS.size());

        query = filterService.queryBuilder("\"project:First project\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for project with with title containing 'First Project'!", 1,
                processDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessTitle() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"process:process\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for title containing 'process'!", 2, processDTOS.size());

        query = filterService.queryBuilder("\"process:First\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for title containing 'First'!", 1, processDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByTaskTitle() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"step:Testing\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        // assertEquals("Incorrect amount of processes for title containing 'Testing'!",
        // 2, processDTOS.size());

        query = filterService.queryBuilder("\"stepopen:Testing\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for title containing 'Testing'!", 1, processDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByBatchId() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"batch:1\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for batch with id 1!", 0, processDTOS.size());

        query = filterService.queryBuilder("\"batch:1\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for batch with id 1!", 1, processDTOS.size());

        query = filterService.queryBuilder("\"batch:1 2\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for batch with id 1 or 2!", 1, processDTOS.size());

        query = filterService.queryBuilder("\"-batch:1 2\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for batch with id 1 or 2!", 2, processDTOS.size());

        query = filterService.queryBuilder("\"-batch:1 2\"", ObjectType.PROCESS, false, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for batch with id 1 or 2!", 3, processDTOS.size());
    }

    // TODO: filters are not working for search only by title
    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProperty() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.PROCESS, true, false,
                false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for property with value containing 'fix'!", 1, processDTOS.size());

        query = filterService.queryBuilder("\"processproperty:value\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for property with value containing 'value'!", 1,
                processDTOS.size());

        query = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for property with title 'Process' and value containing 'value'!", 1,
                processDTOS.size());

        query = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!", 1,
                processDTOS.size());

        query = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"", ObjectType.PROCESS, true,
                false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!", 1,
                processDTOS.size());

        query = filterService.queryBuilder("\"-processproperty:fix\"", ObjectType.PROCESS, true, false, false);
        processDTOS = processService.findByQuery(query, true); // it returns number 5 which has no properties
        assertEquals("Incorrect amount of processes for property with value containing 'fix'!", 1, processDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByMultipleConditions() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"project:First\" \"processproperty:fix\"", ObjectType.PROCESS,
                true, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals(
                "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!",
                1, processDTOS.size());

        query = filterService.queryBuilder("\"project:First project\" \"processproperty:fix\"", ObjectType.PROCESS,
                true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals(
                "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!",
                1, processDTOS.size());

        query = filterService.queryBuilder("\"project:First process\" \"processproperty:fix\"", ObjectType.PROCESS,
                true, false, false);
        processDTOS = processService.findByQuery(query, true);
        assertEquals(
                "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!",
                0, processDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessId() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"id:1\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with id equal 1!", 0, taskDTOS.size());

        query = filterService.queryBuilder("\"id:1\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with id equal 1!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"id:2\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with id equal 2!", 3, taskDTOS.size());

        query = filterService.queryBuilder("\"id:1 5\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with id equal 1 or 5!", 3, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProjectTitle() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"project:First\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for project with title containing 'First'!", 3, taskDTOS.size());

        query = filterService.queryBuilder("\"project:Inactive\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for project with title containing 'Inactive'!", 2, taskDTOS.size());

        query = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for project with title containing 'First Inactive'!", 0,
                taskDTOS.size());

        query = filterService.queryBuilder("\"project:First project\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for project with title containing 'First project'!", 1,
                taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessTitle() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, true, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with title containing 'First'!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with title containing 'Second process'!", 3,
                taskDTOS.size());

        query = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process with title containing 'Second process'!", 0,
                taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByTaskTitle() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        // TODO: why "step" creates something called historical filter?
        QueryBuilder query = filterService.queryBuilder("\"step:Testing\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        // assertEquals("Incorrect amount of tasks with title containing 'Testing'!", 1,
        // taskDTOS.size());

        query = filterService.queryBuilder("\"stepopen:Testing\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks with title containing 'Testing'!", 1, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProperty() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, true, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process property with value containing 'fix'!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"processproperty:value\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks for process property with value containing 'value'!", 1,
                taskDTOS.size());

        query = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals(
                "Incorrect amount of tasks for process property with title 'Process' and value containing 'value'!", 1,
                taskDTOS.size());

        query = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals(
                "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!", 1,
                taskDTOS.size());

        query = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"", ObjectType.TASK, true,
                false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals(
                "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!", 1,
                taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByClosedTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"stepdone:1\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with ordering 1!", 0, taskDTOS.size());

        query = filterService.queryBuilder("\"stepdone:1\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with ordering 1!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"stepdone:Closed\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with title 'Closed'!", 0, taskDTOS.size());

        query = filterService.queryBuilder("\"stepdone:Closed\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with title 'Closed'!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"-stepdone:Closed\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of not closed tasks with title different than 'Closed'!", 2, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByOpenTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"stepopen:1\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of open tasks with ordering 1!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"stepopen:Blocking\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of open tasks with title 'Blocking'!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"stepopen:Blocking\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of open tasks with title 'Blocking'!", 0, taskDTOS.size());

        query = filterService.queryBuilder("\"-stepopen:Blocking\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of not open tasks with title different than 'Blocking'!", 2, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByInProgressTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"stepinwork:3\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks in progress with ordering 3!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"-stepinwork:3\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks not in progress with ordering different than 3!", 2, taskDTOS.size());

        query = filterService.queryBuilder("\"-stepinwork:2\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of tasks not in progress with ordering different than 2!", 1, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByLockedTasks() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"steplocked:2\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of locked tasks with ordering 2!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"-steplocked:2\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of not locked tasks with ordering different than 2!", 2, taskDTOS.size());

        query = filterService.queryBuilder("\"-steplocked:3\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of not locked tasks with ordering different than 3!", 1, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByMultipleConditions() throws Exception {
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"id:2\" \"steplocked:2\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with ordering 1 assigned to process with id 2!", 1, taskDTOS.size());

        query = filterService.queryBuilder("\"id:2\" \"steplocked:2\"", ObjectType.TASK, true, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of locked tasks with ordering 2 assigned to process with id 2!", 0, taskDTOS.size());

        query = filterService.queryBuilder("\"id:2\" \"-stepdone:3\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of not closed tasks with ordering 4 assigned to process with id 2!", 2, taskDTOS.size());

        query = filterService.queryBuilder("\"id:2\" \"-stepdone:4\"", ObjectType.TASK, false, false, false);
        taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of not closed tasks with ordering 4 assigned to process with id 2!", 3, taskDTOS.size());
    }

    @Test
    public void shouldBuildQueryForDefaultConditions() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();
        TaskService taskService = new ServiceManager().getTaskService();

        QueryBuilder query = filterService.queryBuilder("\"Second\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with default condition!", 3, taskDTOS.size());

        query = filterService.queryBuilder("\"Second\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of process with default condition!", 2, processDTOS.size());

    }

    @Test
    public void shouldBuildQueryForEmptyConditions() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();
        TaskService taskService = new ServiceManager().getTaskService();

        // empty condition is not allowed and returns no results
        QueryBuilder query = filterService.queryBuilder("\"steplocked:\"", ObjectType.TASK, false, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with no ordering!", 0, taskDTOS.size());

        // empty condition is not allowed and returns no results
        query = filterService.queryBuilder("\"id:\"", ObjectType.PROCESS, false, false, false);
        List<ProcessDTO> processDTOS = processService.findByQuery(query, true);
        assertEquals("Incorrect amount of process with no id!", 0, processDTOS.size());

    }
}
