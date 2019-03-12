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

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void testSearch(){
        searchResultForm.setSearchQuery("es");
        searchResultForm.search();
        List<ProcessDTO> resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(3, resultList.size());

        searchResultForm.setSearchQuery("process");
        searchResultForm.search();
        resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(2, resultList.size());

        searchResultForm.setSearchQuery("First process");
        searchResultForm.search();
        resultList = searchResultForm.getFilteredList();

        //TODO: it's not working
        //Assert.assertEquals(1, resultList.size());

        searchResultForm.setSearchQuery("Not Existing");
        searchResultForm.search();
        resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(0, resultList.size());

        searchResultForm.setSearchQuery("First project");
        searchResultForm.search();
        resultList = searchResultForm.getFilteredList();

        //TODO: search with withspace obvisously not working correctly
        // Assert.assertEquals(2, resultList.size());
    }

    @Test
    public void testFilterByProject() throws DAOException {
        searchResultForm.setSearchQuery("es");
        searchResultForm.search();


        searchResultForm.filterListByProject(1000);
        List<ProcessDTO>  resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(0, resultList.size());

        searchResultForm.search();
        searchResultForm.filterListByProject(1);
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(2, resultList.size());

        searchResultForm.search();
        searchResultForm.filterListByProject(null);
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(5, resultList.size());

    }

    @Test
    public void testFilterByTask(){
        searchResultForm.setSearchQuery("es");
        searchResultForm.search();

        searchResultForm.filterListByTask("notExistent");
        List<ProcessDTO>  resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(0, resultList.size());

        searchResultForm.search();
        searchResultForm.filterListByTask("Progress");
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(1, resultList.size());

    }

    @Test
    public void testFilterList(){
        searchResultForm.setSearchQuery("es");
        searchResultForm.search();

        searchResultForm.setCurrentProjectFilter(1);
        searchResultForm.filterList();
        List<ProcessDTO>  resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(2, resultList.size());

        searchResultForm.search();
        searchResultForm.setCurrentTaskFilter("");
        searchResultForm.filterList();
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(2, resultList.size());

        searchResultForm.search();
        searchResultForm.setCurrentTaskFilter("TaskNotExistent");
        searchResultForm.filterList();
        resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(0, resultList.size());

    }


}
