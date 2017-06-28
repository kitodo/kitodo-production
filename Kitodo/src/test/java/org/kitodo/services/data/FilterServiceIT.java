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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for FilterService.
 */
public class FilterServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, DataException {
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
