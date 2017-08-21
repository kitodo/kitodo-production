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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Filter;

/**
 * Integration tests for FilterService.
 */
public class FilterServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldCountAllFilters() throws Exception {
        FilterService filterService = new FilterService();

        Long amount = filterService.count();
        assertEquals("Filters were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllFiltersAccordingToQuery() throws Exception {
        FilterService filterService = new FilterService();

        String query = matchQuery("value", "\"id:1\"").operator(Operator.AND).toString();
        Long amount = filterService.count(query);
        assertEquals("Filters were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForFilters() throws Exception {
        FilterService filterService = new FilterService();

        Long amount = filterService.countDatabaseRows();
        assertEquals("Filters were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindFilter() throws Exception {
        FilterService filterService = new FilterService();

        Filter filter = filterService.find(1);
        String actual = filter.getValue();
        String expected = "\"id:1\"";
        assertEquals("Filter was not found in database!", expected, actual);
    }

    @Test
    public void shouldFindAllFilters() throws Exception {
        FilterService filterService = new FilterService();

        List<Filter> filters = filterService.findAll();
        assertEquals("Not all filters were found in database!", 2, filters.size());
    }
}
