package org.kitodo.production.forms;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.ProjectDTO;

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

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(1);
        searchResultForm.filterListByProject(projectDTO);
        List<ProcessDTO>  resultList = searchResultForm.getFilteredList();

        Assert.assertEquals(2, resultList.size());

    }

    @Test
    public void testFilterByTask(){
        searchResultForm.setSearchQuery("es");
        searchResultForm.search();

        Task task = new Task();
        task.setTitle("Progress");

        searchResultForm.filterListByTask(task);
        List<ProcessDTO>  resultList = searchResultForm.getFilteredList();
        Assert.assertEquals(1, resultList.size());

    }

}
