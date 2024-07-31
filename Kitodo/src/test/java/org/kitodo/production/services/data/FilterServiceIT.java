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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        given().ignoreExceptions().await().until(() -> Objects.nonNull(filterService.findByValue(filterValue, true)));
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldCountAllFilters() throws DataException {
        assertEquals(Long.valueOf(2), filterService.count(), "Filters were not counted correctly!");
    }

    @Test
    public void shouldCountAllFiltersAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("value", filterValue).operator(Operator.AND);
        assertEquals(Long.valueOf(1), filterService.count(query), "Filters were not counted correctly!");
    }

    @Test
    public void shouldCountAllDatabaseRowsForFilters() throws Exception {
        Long amount = filterService.countDatabaseRows();
        assertEquals(Long.valueOf(2), amount, "Filters were not counted correctly!");
    }

    @Test
    public void shouldGetFilterById() throws Exception {
        Filter filter = filterService.getById(1);
        String actual = filter.getValue();
        assertEquals(filterValue, actual, "Filter was not found in database!");
    }

    @Test
    public void shouldGetAllFilters() throws Exception {
        List<Filter> filters = filterService.getAll();
        assertEquals(2, filters.size(), "Not all filters were found in database!");
    }

    @Test
    public void shouldGetAllFiltersInGivenRange() throws Exception {
        List<Filter> filters = filterService.getAll(1, 10);
        assertEquals(1, filters.size(), "Not all filters were found in database!");
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessId() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:2\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes with id equal 2!");

        assertEquals(Integer.valueOf(2), processService.findByQuery(firstQuery, true).get(0).getId(), "Incorrect id for found process!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1 2\"", ObjectType.PROCESS, false, false);
        assertEquals(2, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes with id equal 1 or 2!");

        assertEquals(Integer.valueOf(2), processService.findByQuery(secondQuery, SortBuilders.fieldSort("id").order(SortOrder.DESC), true).get(0).getId(), "Incorrect id for found process!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"id:2 3\"", ObjectType.PROCESS, false, false);
        assertEquals(2, processService.findByQuery(thirdQuery, true).size(), "Incorrect amount of processes with id equal 2 or 3!");
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProjectTitle() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\"", ObjectType.PROCESS, false, false);
        assertEquals(2, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes for project with title containing 'First'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:Second\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes for project with title containing 'Second'!");

        // it has only 2 templates - no processes
        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:Inactive\"", ObjectType.PROCESS, false, false);
        assertEquals(0, processService.findByQuery(thirdQuery, true).size(), "Incorrect amount of processes for project with title containing 'Inactive'!");

        QueryBuilder fourthQuery = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.PROCESS, false,
            false);
        assertEquals(0, processService.findByQuery(fourthQuery, true).size(), "Incorrect amount of processes for project with with title containing 'First Inactive'!");

        QueryBuilder fifthQuery = filterService.queryBuilder("\"project:First project\"", ObjectType.PROCESS, false,
            false);
        assertEquals(2, processService.findByQuery(fifthQuery, true).size(), "Incorrect amount of processes for project with with title containing 'First Project'!");
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProcessTitle() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"process:process\"", ObjectType.PROCESS, false, false);
        assertEquals(2, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes for title containing 'process'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"process:First\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes for title containing 'First'!");
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByTaskTitle() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"step:Finished\"", ObjectType.PROCESS, false, false);
        assertEquals(0, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes for title containing 'Finished'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Open\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes for title containing 'Open'!");
    }

    @Test
    public void shouldBuildQueryAndFindByProcessServiceByBatchId() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"batch:1\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes for batch with id 1!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"batch:1 2\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes for batch with id 1 or 2!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-batch:1 2\"", ObjectType.PROCESS, false, false);
        assertEquals(2, processService.findByQuery(thirdQuery, true).size(), "Incorrect amount of processes for batch with not id 1 or 2!");
    }

    @Test
    public void shouldBuildQueryAndFindByTitle() throws DataException {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder query = filterService.queryBuilder("\"DBConnectionTest\"", ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(query, true).size(), "Incorrect amount of processes title 'DBConnectionTest'");

        query = filterService.queryBuilder("\"ocess\"", ObjectType.PROCESS, false, false);
        assertEquals(2, processService.findByQuery(query, true).size(), "Incorrect amount of processes where title contains 'ocess''");

        query = filterService.queryBuilder("\"\"", ObjectType.PROCESS, false, false);
        assertEquals(3, processService.findByQuery(query, true).size(), "Incorrect amount of processes with empty query'");

        query = filterService.queryBuilder("\"notAvailable\"", ObjectType.PROCESS, false, false);
        assertTrue(processService.findByQuery(query, true).isEmpty(), "Incorrect amount of processes with wrong title");
    }

    /**
     * find by properties.
     */
    @Disabled
    @Test
    public void shouldBuildQueryAndFindByProcessServiceByProperty() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.PROCESS, false,
            false);
        assertEquals(1, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes for property with value containing 'fix'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"processproperty:value\"", ObjectType.PROCESS, false,
            false);
        assertEquals(1, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes for property with value containing 'value'!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.PROCESS,
            false, false);
        assertEquals(1, processService.findByQuery(thirdQuery, true).size(), "Incorrect amount of processes for property with title 'Process' and value containing 'value'!");

        QueryBuilder fourthQuery = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.PROCESS,
            false, false);
        assertEquals(1, processService.findByQuery(fourthQuery, true).size(), "Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!");

        QueryBuilder fifthQuery = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"",
            ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(fifthQuery, true).size(), "Incorrect amount of processes for property with title 'Korrektur' and value containing 'fix'!");

        QueryBuilder sixthQuery = filterService.queryBuilder("\"-processproperty:fix\"", ObjectType.PROCESS, false,
            false);
        assertEquals(2, processService.findByQuery(sixthQuery, true).size(), "Incorrect amount of processes for property with value not containing 'fix'!");
    }

    /**
     * find by multiple conditions.
     */
    @Disabled
    @Test
    public void shouldBuildQueryAndFindByProcessServiceByMultipleConditions() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(firstQuery, true).size(), "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:First project\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        assertEquals(1, processService.findByQuery(secondQuery, true).size(), "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:First process\" \"processproperty:fix\"",
            ObjectType.PROCESS, false, false);
        assertEquals(0, processService.findByQuery(thirdQuery, true).size(), "Incorrect amount of processes for project with title 'First' and property with value containing 'fix'!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessId() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder(filterValue, ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks for process with id equal 1!");

        QueryBuilder secondQuery = filterService.queryBuilder(filterValue, ObjectType.TASK, true, false);
        assertEquals(1, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of open tasks for process with id equal 1!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"id:1 2\"", ObjectType.TASK, false, false);
        assertEquals(4, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks for process with id equal to 1 or 2!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProjectTitle() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"project:First\"", ObjectType.TASK, false, false);
        assertEquals(4, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks for project with title containing 'First'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"project:Inactive\"", ObjectType.TASK, false, false);
        assertEquals(0, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of tasks for project with title containing 'Inactive'!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"project:First Inactive\"", ObjectType.TASK, false,
            false);
        assertEquals(0, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks for project with title containing 'First Inactive'!");

        QueryBuilder fourthQuery = filterService.queryBuilder("\"project:First project\"", ObjectType.TASK, true,
            false);
        assertEquals(2, taskService.findByQuery(fourthQuery, true).size(), "Incorrect amount of tasks for project with title containing 'First project'!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProcessTitle() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks for process with title containing 'First'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"process:First\"", ObjectType.TASK, true, false);
        assertEquals(1, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of tasks for process with title containing 'First'!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, false,
            false);
        assertEquals(2, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks for process with title containing 'Second process'!");

        QueryBuilder fourthQuery = filterService.queryBuilder("\"process:Second process\"", ObjectType.TASK, true,
            false);
        assertEquals(1, taskService.findByQuery(fourthQuery, true).size(), "Incorrect amount of tasks for process with title containing 'Second process'!");
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
        assertEquals(1, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks with title containing 'Open'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Finished\"", ObjectType.TASK, false, false);
        assertEquals(0, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of tasks with title containing 'Finished'!");
    }

    /**
     * find tasks by property.
     */
    @Disabled
    @Test
    public void shouldBuildQueryAndFindByTaskServiceByProperty() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks for process property with value containing 'fix'!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"processproperty:fix\"", ObjectType.TASK, true, false);
        assertEquals(1, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of tasks for process property with value containing 'fix'!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"processproperty:value\"", ObjectType.TASK, false,
            false);
        assertEquals(2, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks for process property with value containing 'value'!");

        QueryBuilder fourthQuery = filterService.queryBuilder("\"processproperty:Process:value\"", ObjectType.TASK,
            false, false);
        assertEquals(2, taskService.findByQuery(fourthQuery, true).size(), "Incorrect amount of tasks for process property with title 'Process' and value containing 'value'!");

        QueryBuilder fifthQuery = filterService.queryBuilder("\"processproperty:Korrektur:fix\"", ObjectType.TASK,
            false, false);
        assertEquals(2, taskService.findByQuery(fifthQuery, true).size(), "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!");

        QueryBuilder sixthQuery = filterService.queryBuilder("\"processproperty:Korrektur notwendig:fix it\"",
            ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(sixthQuery, true).size(), "Incorrect amount of tasks for process property with title 'Korrektur' and value containing 'fix'!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByClosedTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        // this query will never return anything, since tasks are always filtered for "open" or "inwork" only
        QueryBuilder firstQuery = filterService.queryBuilder("\"stepdone:1\"", ObjectType.TASK, false, false);
        assertEquals(0, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of closed tasks with ordering 1!");

        // this query will never return anything, since tasks are always filtered for "open" or "inwork" only
        QueryBuilder secondQuery = filterService.queryBuilder("\"stepdone:Closed\"", ObjectType.TASK, false, false);
        assertEquals(0, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of closed tasks with title 'Closed'!");

        // this query will never exclude anything, because "done" tasks are never listed anyway
        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepdone:Closed\"", ObjectType.TASK, false, false);
        assertEquals(4, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of not closed tasks with title different than 'Closed'!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByOpenTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder allTasksQuery = filterService.queryBuilder("\"\"", ObjectType.TASK, false, false);
        int numberOfAllTasks = taskService.findByQuery(allTasksQuery, true).size();
        assertEquals(4, numberOfAllTasks, "Incorrect number of all tasks!");

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepopen:4\"", ObjectType.TASK, false, false);
        assertEquals(1, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of open tasks with ordering 4!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Open\"", ObjectType.TASK, false, false);
        int numberOfOpenTasks = taskService.findByQuery(secondQuery, true).size();
        assertEquals(1, numberOfOpenTasks, "Incorrect amount of open tasks with title 'Open'!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepopen:Open\"", ObjectType.TASK, false, false);
        int numberOfNotOpenTasks = taskService.findByQuery(thirdQuery, true).size();
        assertEquals(numberOfAllTasks - numberOfOpenTasks, numberOfNotOpenTasks, "Incorrect amount of not open tasks with title different than 'Open'!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByInProgressTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"stepinwork:3\"", ObjectType.TASK, false, false);
        assertEquals(1, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks in progress with ordering 3!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"-stepinwork:3\"", ObjectType.TASK, false, false);
        assertEquals(3, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of tasks not in progress with ordering different than 3!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-stepinwork:2\"", ObjectType.TASK, false, false);
        assertEquals(3, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks not in progress with ordering different than 2!");
    }

    @Disabled("problem with steplocked")
    @Test
    public void shouldBuildQueryAndFindByTaskServiceByLockedTasks() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"steplocked:2\"", ObjectType.TASK, false, false);
        assertEquals(1, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of locked tasks with ordering 2!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"-steplocked:2\"", ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of not locked tasks with ordering different than 2!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"-steplocked:3\"", ObjectType.TASK, false, false);
        assertEquals(1, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of not locked tasks with ordering different than 3!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceWithDisjunctions() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();

        // check that two task conditions can be combined by disjunction
        // returns "Progress" and "Open" tasks associated with any process that are either in state "inwork" or "open", respectively
        QueryBuilder firstQuery = filterService.queryBuilder("\"stepinwork:Progress | stepopen:Open\"", ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks for disjunction query \"stepinwork:Progress | stepopen:Open\"!");

        // check that a task condition can be combined with a process condition by disjunction
        // returns any tasks associated with process id=1 or "Next Open" tasks that are in state "open"
        QueryBuilder secondQuery = filterService.queryBuilder("\"stepopen:Next Open | id:1\"", ObjectType.TASK, false, false);
        assertEquals(3, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of tasks for disjunction query \"stepopen:Next Open | id:1\"!");

        // check that default queries can be combined with task conditions by disjunction
        // returns any tasks related to process "First process" or "Second process" but not "Progress" tasks in state "inwork"
        QueryBuilder thirdQuery = filterService.queryBuilder("\"First | Second\" \"-stepinwork:Progress\"", ObjectType.TASK, false, false);
        assertEquals(3, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks for disjunction query \"First | Second\"!");

        // check that conditions can be optionally enclosed in parentheses, which masks the vertical line "|"
        // returns tasks whose process title contains the string "First |" (aka none) or tasks in state "inwork" with title "Progress"
        QueryBuilder fourthQuery = filterService.queryBuilder("\"(First |) | (stepinwork:Progress)\"", ObjectType.TASK, false, false);
        assertEquals(1, taskService.findByQuery(fourthQuery, true).size(), "Incorrect amount of tasks for disjunction query \"(First |) | (stepinwork:Progress)\"!");
    }

    @Test
    public void shouldBuildQueryAndFindByTaskServiceByMultipleConditions() throws Exception {
        TaskService taskService = ServiceManager.getTaskService();     

        QueryBuilder firstQuery = filterService.queryBuilder("\"id:1\" \"-stepinwork:3\"", ObjectType.TASK, false, false);
        assertEquals(1, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of not closed tasks with ordering 4 assigned to process with id 1!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"id:1\" \"-stepopen:4\"", ObjectType.TASK, false,
            false);
        assertEquals(1, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of not closed tasks with ordering 4 assigned to process with id 1!");
    }

    @Test
    public void shouldBuildQueryForDefaultConditions() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();
        TaskService taskService = ServiceManager.getTaskService();

        QueryBuilder firstQuery = filterService.queryBuilder("\"First\"", ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(firstQuery, true).size(), "Incorrect amount of tasks with default condition!");

        QueryBuilder secondQuery = filterService.queryBuilder("\"First\"", ObjectType.TASK, true, false);
        assertEquals(1, taskService.findByQuery(secondQuery, true).size(), "Incorrect amount of open tasks with default condition!");

        QueryBuilder thirdQuery = filterService.queryBuilder("\"Second\"", ObjectType.TASK, false, false);
        assertEquals(2, taskService.findByQuery(thirdQuery, true).size(), "Incorrect amount of tasks with default condition!");

        QueryBuilder fourthQuery = filterService.queryBuilder("\"Second\"", ObjectType.PROCESS, true, false);
        assertEquals(1, processService.findByQuery(fourthQuery, true).size(), "Incorrect amount of process with default condition!");

    }

    @Test
    public void shouldBuildQueryForEmptyConditions() throws Exception {
        ProcessService processService = ServiceManager.getProcessService();
        TaskService taskService = ServiceManager.getTaskService();

        // empty condition is not allowed and returns no results
        QueryBuilder query = filterService.queryBuilder("\"steplocked:\"", ObjectType.TASK, false, false);
        List<TaskDTO> taskDTOS = taskService.findByQuery(query, true);
        assertEquals(0, taskDTOS.size(), "Incorrect amount of closed tasks with no ordering!");

        // empty condition is not allowed and throws Exception in ElasticSearch 7
        query = filterService.queryBuilder("\"id:\"", ObjectType.PROCESS, false, false);
        QueryBuilder finalQuery = query;
        assertThrows(ElasticsearchStatusException.class,
                () -> processService.findByQuery(finalQuery, true));
    }
}
