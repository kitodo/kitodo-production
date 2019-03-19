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

package org.kitodo.production.forms;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.dto.ProcessDTO;

public class SearchResultFormIT {

    private SearchResultForm searchResultForm = new SearchResultForm();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    /**
     * Cleanup the database and stop elasticsearch.
     * @throws Exception if elasticsearch could not been stopped.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void testSearch() {
        searchResultForm.setSearchQuery("es");
        searchResultForm.searchForProcessesBySearchQuery();
        List<ProcessDTO> resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(3, resultList.size());

        searchResultForm.setSearchQuery("process");
        searchResultForm.searchForProcessesBySearchQuery();
        resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(2, resultList.size());

        searchResultForm.setSearchQuery("First process");
        searchResultForm.searchForProcessesBySearchQuery();
        resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(1, resultList.size());

        searchResultForm.setSearchQuery("Not Existing");
        searchResultForm.searchForProcessesBySearchQuery();
        resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(0, resultList.size());

        searchResultForm.setSearchQuery("First project");
        searchResultForm.searchForProcessesBySearchQuery();
        resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(2, resultList.size());
    }

    @Test
    public void testFilterByProject() throws DAOException {
        searchResultForm.setSearchQuery("es");
        searchResultForm.searchForProcessesBySearchQuery();

        searchResultForm.filterListByProject(1000);
        List<ProcessDTO> resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(0, resultList.size());

        searchResultForm.searchForProcessesBySearchQuery();
        searchResultForm.filterListByProject(1);
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(2, resultList.size());

        searchResultForm.searchForProcessesBySearchQuery();
        searchResultForm.filterListByProject(null);
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(3, resultList.size());

    }

    @Test
    public void testFilterByTask() {
        searchResultForm.setSearchQuery("es");
        searchResultForm.searchForProcessesBySearchQuery();

        searchResultForm.filterListByTask("notExistent");
        List<ProcessDTO> resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(0, resultList.size());

        searchResultForm.searchForProcessesBySearchQuery();
        searchResultForm.filterListByTask("Progress");
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(1, resultList.size());

    }

    @Test
    public void testFilterList() {
        searchResultForm.setSearchQuery("es");
        searchResultForm.searchForProcessesBySearchQuery();

        searchResultForm.setCurrentProjectFilter(1);
        searchResultForm.filterList();
        List<ProcessDTO> resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(2, resultList.size());

        searchResultForm.searchForProcessesBySearchQuery();
        searchResultForm.setCurrentTaskFilter("");
        searchResultForm.filterList();
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(2, resultList.size());

        searchResultForm.searchForProcessesBySearchQuery();
        searchResultForm.setCurrentTaskFilter("TaskNotExistent");
        searchResultForm.filterList();
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(0, resultList.size());

    }

}
