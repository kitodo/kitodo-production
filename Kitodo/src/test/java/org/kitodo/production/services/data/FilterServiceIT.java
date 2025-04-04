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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.exceptions.DAOException;
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
        given().ignoreExceptions().await().until(() -> Objects.nonNull(filterService.getById(1)));
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldCountAllFilters() throws DAOException {
        assertEquals(Long.valueOf(2), filterService.count(), "Filters were not counted correctly!");
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldCountAllFiltersAccordingToQuery() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldCountAllDatabaseRowsForFilters() throws Exception {
        Long amount = filterService.count();
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
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByProjectTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByTaskTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByBatchId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByProperty() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByProcessServiceByMultipleConditions() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByProcessId() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByProjectTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByProcessTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByTaskTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByProperty() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByClosedTasks() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByOpenTasks() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByInProgressTasks() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByLockedTasks() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceWithDisjunctions() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryAndFindByTaskServiceByMultipleConditions() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryForDefaultConditions() throws Exception {
        // TODO delete test stub
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldBuildQueryForEmptyConditions() throws Exception {
        // TODO delete test stub
    }
}
