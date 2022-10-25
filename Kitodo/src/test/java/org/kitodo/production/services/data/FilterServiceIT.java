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

import static org.awaitility.Awaitility.given;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.TaskDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.services.ServiceManager;

/**
 * Integration tests for FilterService.
 */
public class FilterServiceIT {

    private static final FilterService filterService = ServiceManager.getFilterService();

    private static final String filterValue = "\"id:1\"";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        given().ignoreExceptions().await().until(() -> Objects.nonNull(filterService.findByValue(filterValue, true)));
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldCountAllFilters() throws DataException {
        assertEquals("Filters were not counted correctly!", Long.valueOf(2), filterService.count());
    }

    @Test
    public void shouldCountAllFiltersAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("value", filterValue).operator(Operator.AND);
        assertEquals("Filters were not counted correctly!", Long.valueOf(1), filterService.count(query));
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
        assertEquals("Filter was not found in database!", filterValue, actual);
    }

    @Test
    public void shouldGetAllFilters() throws Exception {
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
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:2\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes with id equal 2!", 1,
            processService.findByQuery(firstQuery, true).size());

        assertEquals("Incorrect id for found process!", Integer.valueOf(2),
            processService.findByQuery(firstQuery, true).get(0).getId());

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1 2\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes with id equal 1 or 2!", 2,
            processService.findByQuery(secondQuery, true).size());

        assertEquals("Incorrect id for found process!", Integer.valueOf(2),
            processService.findByQuery(secondQuery, SortBuilders.fieldSort("id").order(SortOrder.DESC), true).get(0).getId());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"id:2 3\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes with id equal 2 or 3!", 2,
            processService.findByQuery(thirdQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProjectTitle() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for project with title containing 'First'!", 2,
            processService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:Second\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for project with title containing 'Second'!", 1,
            processService.findByQuery(secondQuery, true).size());

        // it has only 2 templates - no processes
        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:Inactive\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for project with title containing 'Inactive'!", 0,
            processService.findByQuery(thirdQuery, true).size());

        QueryBuilder fourthQuery = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.PROCESS, false,
            false);
        assertEquals("Incorrect amount of processes for project with with title containing 'First Inactive'!", 0,
            processService.findByQuery(fourthQuery, true).size());

        QueryBuilder fifthQuery = filterService.queryBuilder("\"project:First project\"", ObjectType.PROCESS, false,
            false);
        assertEquals("Incorrect amount of processes for project with with title containing 'First Project'!", 2,
            processService.findByQuery(fifthQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessTitle() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"process:process\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for title containing 'process'!", 2,
            processService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"process:First\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for title containing 'First'!", 1,
            processService.findByQuery(secondQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByTaskTitle() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"step:Finished\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for title containing 'Finished'!", 0,
            processService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Open\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for title containing 'Open'!", 1,
            processService.findByQuery(secondQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByBatchId() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"batch:1\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for batch with id 1!", 1,
            processService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"batch:1 2\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for batch with id 1 or 2!", 1,
            processService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-batch:1 2\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for batch with not id 1 or 2!", 2,
            processService.findByQuery(thirdQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTitle() throws DataException {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"DBConnectionTest\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes title 'DBConnectionTest'", 1,
                processService.findByQuery(query, true).size());

        query = filterService.queryBuilder("\"ocess\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes where title contains 'ocess''", 2,
                processService.findByQuery(query, true).size());

        query = filterService.queryBuilder("\"\"", ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes with empty query'", 3,
                processService.findByQuery(query, true).size());

        query = filterService.queryBuilder("\"notAvailable\"", ObjectType.PROCESS, false, false);
        assertTrue("Incorrect amount of processes with wrong title",
                processService.findByQuery(query, true).isEmpty());
    }

    /**
     * find by properties.
     */
    @Ignore
    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProperty() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.PROCESS, false,
            false);
        assertEquals("Incorrect amount of processes for property with value containing 'fix'!", 1,
            processService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"processproperty:value\"", ObjectType.PROCESS, false,
            false);
        assertEquals("Incorrect amount of processes for property with value containing 'value'!", 1,
            processService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.PROCESS,
            false, false);
        assertEquals("Incorrect amount of processes for property with title 'Process' and value containing 'value'!", 1,
            processService.findByQuery(thirdQuery, true).size());

        QueryBuilder fourthQuery = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.PROCESS,
            false, false);
        assertEquals("Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!", 1,
            processService.findByQuery(fourthQuery, true).size());

        QueryBuilder fifthQuery = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"",
            ObjectType.PROCESS, false, false);
        assertEquals("Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!", 1,
            processService.findByQuery(fifthQuery, true).size());

        QueryBuilder sixthQuery = filterService.queryBuilder("\"-processproperty:fix\"", ObjectType.PROCESS, false,
            false);
        assertEquals("Incorrect amount of processes for property with value not containing 'fix'!", 2,
            processService.findByQuery(sixthQuery, true).size());
    }

    /**
     * find by multiple conditions.
     */
    @Ignore
    @Test
    public void shouldBuildQueryAndFindByProcessServiceByMultipleConditions() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        assertEquals(
            "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!", 1,
            processService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:First project\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        assertEquals(
            "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!", 1,
            processService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:First process\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        assertEquals(
            "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!", 0,
            processService.findByQuery(thirdQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessId() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder(filterValue, ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for process with id equal 1!", 2,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder(filterValue, ObjectType.TASK, true, false);
        assertEquals("Incorrect amount of open tasks for process with id equal 1!", 1,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"id:1 2\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for process with id equal to 1 or 2!", 4,
            taskService.findByQuery(thirdQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProjectTitle() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for project with title containing 'First'!", 4,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:Inactive\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for project with title containing 'Inactive'!", 0,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.TASK, false,
            false);
        assertEquals("Incorrect amount of tasks for project with title containing 'First Inactive'!", 0,
            taskService.findByQuery(thirdQuery, true).size());

        QueryBuilder fourthQuery = filterService.queryBuilder("\"project:First project\"", ObjectType.TASK, true,
            false);
        assertEquals("Incorrect amount of tasks for project with title containing 'First project'!", 2,
            taskService.findByQuery(fourthQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessTitle() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for process with title containing 'First'!", 2,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, true, false);
        assertEquals("Incorrect amount of tasks for process with title containing 'First'!", 1,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, false,
            false);
        assertEquals("Incorrect amount of tasks for process with title containing 'Second process'!", 2,
            taskService.findByQuery(thirdQuery, true).size());

        QueryBuilder fourthQuery = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, true,
            false);
        assertEquals("Incorrect amount of tasks for process with title containing 'Second process'!", 1,
            taskService.findByQuery(fourthQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByTaskTitle() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        // TODO: why "step" creates something called historical filter?
        QueryBuilder query = filterService.queryBuilder("\"step:Finished\"", ObjectType.TASK, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        // assertEquals("Incorrect amount of tasks with title containing
        // 'Testing'!", 1, taskDTOS.size());

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepopen:Open\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks with title containing 'Open'!", 1,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Finished\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks with title containing 'Finished'!", 0,
            taskService.findByQuery(secondQuery, true).size());
    }

    /**
     * find tasks by property.
     */
    @Ignore
    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProperty() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for process property with value containing 'fix'!", 2,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, true, false);
        assertEquals("Incorrect amount of tasks for process property with value containing 'fix'!", 1,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"processproperty:value\"", ObjectType.TASK, false,
            false);
        assertEquals("Incorrect amount of tasks for process property with value containing 'value'!", 2,
            taskService.findByQuery(thirdQuery, true).size());

        QueryBuilder fourthQuery = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.TASK,
            false, false);
        assertEquals(
            "Incorrect amount of tasks for process property with title 'Process' and value containing 'value'!", 2,
            taskService.findByQuery(fourthQuery, true).size());

        QueryBuilder fifthQuery = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.TASK,
            false, false);
        assertEquals(
            "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!", 2,
            taskService.findByQuery(fifthQuery, true).size());

        QueryBuilder sixthQuery = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"",
            ObjectType.TASK, false, false);
        assertEquals(
            "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!", 2,
            taskService.findByQuery(sixthQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByClosedTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        // this query will never return anything, since tasks are always filtered for "open" or "inwork" only
        QueryBuilder firstQuery = filterService.queryBuilder("\"stepdone:1\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of closed tasks with ordering 1!", 0,
            taskService.findByQuery(firstQuery, true).size());

        // this query will never return anything, since tasks are always filtered for "open" or "inwork" only
        QueryBuilder secondQuery = filterService.queryBuilder("\"stepdone:Closed\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of closed tasks with title 'Closed'!", 0,
            taskService.findByQuery(secondQuery, true).size());

        // this query will never exclude anything, because "done" tasks are never listed anyway
        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepdone:Closed\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of not closed tasks with title different than 'Closed'!", 4,
            taskService.findByQuery(thirdQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByOpenTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder allTasksQuery = filterService.queryBuilder("\"\"", ObjectType.TASK, false, false);
        int numberOfAllTasks = taskService.findByQuery(allTasksQuery, true).size();
        assertEquals("Incorrect number of all tasks!", 4, numberOfAllTasks);

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepopen:4\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of open tasks with ordering 4!", 1,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Open\"", ObjectType.TASK, false, false);
        int numberOfOpenTasks = taskService.findByQuery(secondQuery, true).size();
        assertEquals("Incorrect amount of open tasks with title 'Open'!", 1, numberOfOpenTasks);

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepopen:Open\"", ObjectType.TASK, false, false);
        int numberOfNotOpenTasks = taskService.findByQuery(thirdQuery, true).size();
        assertEquals(
            "Incorrect amount of not open tasks with title different than 'Open'!", 
            numberOfAllTasks - numberOfOpenTasks, numberOfNotOpenTasks
        );
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByInProgressTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepinwork:3\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks in progress with ordering 3!", 1,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"-stepinwork:3\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks not in progress with ordering different than 3!", 3,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepinwork:2\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks not in progress with ordering different than 2!", 3,
            taskService.findByQuery(thirdQuery, true).size());
    }

    @Ignore("problem with steplocked")
    @Test
    public void shouldBuildQueryAndFindByTaskServiceByLockedTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"steplocked:2\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of locked tasks with ordering 2!", 1,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"-steplocked:2\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of not locked tasks with ordering different than 2!", 2,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-steplocked:3\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of not locked tasks with ordering different than 3!", 1,
            taskService.findByQuery(thirdQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceWithDisjunctions() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        // check that two task conditions can be combined by disjunction
        // returns "Progress" and "Open" tasks associated with any process that are either in state "inwork" or "open", respectively
        QueryBuilder firstQuery = filterService.queryBuilder("\"stepinwork:Progress | stepopen:Open\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for disjunction query \"stepinwork:Progress | stepopen:Open\"!", 2,
            taskService.findByQuery(firstQuery, true).size());

        // check that a task condition can be combined with a process condition by disjunction
        // returns any tasks associated with process id=1 or "Next Open" tasks that are in state "open"
        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Next Open | id:1\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for disjunction query \"stepopen:Next Open | id:1\"!", 3,
            taskService.findByQuery(secondQuery, true).size());

        // check that default queries can be combined with task conditions by disjunction
        // returns any tasks related to process "First process" or "Second process" but not "Progress" tasks in state "inwork"
        QueryBuilder thirdQuery = filterService.queryBuilder("\"First | Second\" \"-stepinwork:Progress\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for disjunction query \"First | Second\"!", 3,
            taskService.findByQuery(thirdQuery, true).size());

        // check that conditions can be optionally enclosed in parentheses, which masks the vertical line "|"
        // returns tasks whose process title contains the string "First |" (aka none) or tasks in state "inwork" with title "Progress"
        QueryBuilder fourthQuery = filterService.queryBuilder("\"(First |) | (stepinwork:Progress)\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks for disjunction query \"(First |) | (stepinwork:Progress)\"!", 1,
            taskService.findByQuery(fourthQuery, true).size());
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByMultipleConditions() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();     

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:1\" \"-stepinwork:3\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of not closed tasks with ordering 4 assigned to process with id 1!", 1,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1\" \"-stepopen:4\"", ObjectType.TASK, false,
            false);
        assertEquals("Incorrect amount of not closed tasks with ordering 4 assigned to process with id 1!", 1,
            taskService.findByQuery(secondQuery, true).size());
    }

    @Test
    public void shouldBuildQueryForDefaultConditions() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"First\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks with default condition!", 2,
            taskService.findByQuery(firstQuery, true).size());

        QueryBuilder secondQuery = filterService.queryBuilder("\"First\"", ObjectType.TASK, true, false);
        assertEquals("Incorrect amount of open tasks with default condition!", 1,
            taskService.findByQuery(secondQuery, true).size());

        QueryBuilder thirdQuery = filterService.queryBuilder("\"Second\"", ObjectType.TASK, false, false);
        assertEquals("Incorrect amount of tasks with default condition!", 2,
            taskService.findByQuery(thirdQuery, true).size());

        QueryBuilder fourthQuery = filterService.queryBuilder("\"Second\"", ObjectType.PROCESS, true, false);
        assertEquals("Incorrect amount of process with default condition!", 1,
            processService.findByQuery(fourthQuery, true).size());

    }

    @Test
    public void shouldBuildQueryForEmptyConditions() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();
        TaskService taskService = ServiceManager.getTaskService();

        // empty condition is not allowed and returns no results
        QueryBuilder query = filterService.queryBuilder("\"steplocked:\"", ObjectType.TASK, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals("Incorrect amount of closed tasks with no ordering!", 0, taskDTOS.size());

        // empty condition is not allowed and throws Exception in ElasticSearch 7
        query = filterService.queryBuilder("\"id:\"", ObjectType.PROCESS, false, false);
        QueryBuilder finalQuery = query;
        Assertions.assertThrows(ElasticsearchStatusException.class,
                () -> processService.findByQuery(finalQuery, true));
    }
}
