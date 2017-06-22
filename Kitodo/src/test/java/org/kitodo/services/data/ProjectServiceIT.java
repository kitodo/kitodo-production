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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.flow.statistics.StepInformation;
import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.enums.SearchCondition;
import org.kitodo.data.exceptions.DataException;

/**
 * Tests for ProjectService class.
 */
public class ProjectServiceIT {

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
    public void shouldFindProject() throws Exception {
        ProjectService projectService = new ProjectService();

        Project project = projectService.find(1);
        boolean condition = project.getTitle().equals("First project") && project.getNumberOfPages().equals(30);
        assertTrue("Project was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProjects() {
        ProjectService projectService = new ProjectService();

        List<Project> projects = projectService.findAll();
        assertEquals("Not all projects were found in database!", 3, projects.size());
    }

    @Test
    public void shouldRemoveProject() throws Exception {
        ProjectService projectService = new ProjectService();

        Project project = new Project();
        project.setTitle("To Remove");
        projectService.save(project);
        Project foundProject = projectService.convertSearchResultToObject(projectService.findById(4));
        assertEquals("Additional project was not inserted in database!", "To Remove", foundProject.getTitle());

        projectService.remove(foundProject);
        foundProject = projectService.convertSearchResultToObject(projectService.findById(4));
        assertEquals("Additional project was not removed from database!", null, foundProject);

        project = new Project();
        project.setTitle("To remove");
        projectService.save(project);
        foundProject = projectService.convertSearchResultToObject(projectService.findById(5));
        assertEquals("Additional project was not inserted in database!", "To remove", foundProject.getTitle());

        projectService.remove(5);
        foundProject = projectService.convertSearchResultToObject(projectService.findById(5));
        assertEquals("Additional project was not removed from database!", null, foundProject);
    }

    @Test
    public void shouldFindById() throws Exception {
        ProjectService projectService = new ProjectService();

        SearchResult project = projectService.findById(1);
        String actual = (String) project.getProperties().get("title");
        String expected = "First project";
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        ProjectService projectService = new ProjectService();

        List<SearchResult> projects = projectService.findByTitle("First project", true);
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByStartDate() throws Exception {
        ProjectService projectService = new ProjectService();

        LocalDate localDate = new LocalDate(2016, 10, 20);
        List<SearchResult> projects = projectService.findByStartDate(localDate.toDate(), SearchCondition.EQUAL);
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByStartDate(localDate.toDate(), SearchCondition.EQUAL_OR_BIGGER);
        actual = projects.size();
        expected = 2;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByStartDate(localDate.toDate(), SearchCondition.BIGGER);
        actual = projects.size();
        expected = 1;
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByEndDate() throws Exception {
        ProjectService projectService = new ProjectService();

        LocalDate localDate = new LocalDate(2017, 9, 15);
        List<SearchResult> projects = projectService.findByEndDate(localDate.toDate(), SearchCondition.EQUAL);
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByEndDate(localDate.toDate(), SearchCondition.EQUAL_OR_BIGGER);
        actual = projects.size();
        expected = 2;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByEndDate(localDate.toDate(), SearchCondition.BIGGER);
        actual = projects.size();
        expected = 1;
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByNumberOfPages() throws Exception {
        ProjectService projectService = new ProjectService();

        List<SearchResult> projects = projectService.findByNumberOfPages(30, SearchCondition.EQUAL);
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByNumberOfPages(40, SearchCondition.EQUAL);
        actual = projects.size();
        expected = 0;
        assertEquals("Project was found in index!", expected, actual);

        projects = projectService.findByNumberOfPages(80, SearchCondition.EQUAL_OR_BIGGER);
        actual = projects.size();
        expected = 2;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByNumberOfPages(80, SearchCondition.BIGGER);
        actual = projects.size();
        expected = 1;
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByNumberOfVolumes() throws Exception {
        ProjectService projectService = new ProjectService();

        List<SearchResult> projects = projectService.findByNumberOfVolumes(2, SearchCondition.EQUAL);
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByNumberOfVolumes(3, SearchCondition.EQUAL);
        actual = projects.size();
        expected = 0;
        assertEquals("Project was found in index!", expected, actual);

        projects = projectService.findByNumberOfVolumes(4, SearchCondition.EQUAL_OR_BIGGER);
        actual = projects.size();
        expected = 2;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByNumberOfVolumes(4, SearchCondition.BIGGER);
        actual = projects.size();
        expected = 1;
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Ignore("save dependencies in Process Service is called but it doesn't update project document")
    @Test
    public void shouldFindByProcessId() throws Exception {
        ProjectService projectService = new ProjectService();

        SearchResult project = projectService.findByProcessId(1);
        Integer actual = project.getId();
        Integer expected = 1;
        assertEquals("Project were not found in index!", expected, actual);

        project = projectService.findByProcessId(4);
        actual = project.getId();
        expected = null;
        assertEquals("Some project was found in index!", expected, actual);
    }

    @Ignore("save dependencies in Process Service is called but it doesn't update project document")
    @Test
    public void shouldFindByProcessTitle() throws Exception {
        ProjectService projectService = new ProjectService();

        List<SearchResult> projects = projectService.findByProcessTitle("First process");
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByProcessTitle("DBConnectionTest");
        actual = projects.size();
        expected = 0;
        assertEquals("Projects were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserId() throws Exception {
        ProjectService projectService = new ProjectService();

        List<SearchResult> projects = projectService.findByUserId(1);
        Integer actual = projects.size();
        Integer expected = 2;
        assertEquals("Projects were not found in index!", expected, actual);

        projects = projectService.findByUserId(2);
        actual = projects.size();
        expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByUserId(3);
        actual = projects.size();
        expected = 0;
        assertEquals("Projects were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByUserLogin() throws Exception {
        ProjectService projectService = new ProjectService();

        List<SearchResult> projects = projectService.findByUserLogin("kowal");
        Integer actual = projects.size();
        Integer expected = 2;
        assertEquals("Projects were not found in index!", expected, actual);

        projects = projectService.findByUserLogin("nowak");
        actual = projects.size();
        expected = 1;
        assertEquals("Project was not found in index!", expected, actual);

        projects = projectService.findByUserLogin("dora");
        actual = projects.size();
        expected = 0;
        assertEquals("Projects were found in index!", expected, actual);
    }

    @Test
    public void shouldGetWorkFlow() throws Exception {
        ProjectService projectService = new ProjectService();

        // test passes... but it can mean that something is wrong...
        Project project = projectService.find(1);
        List<StepInformation> expected = new ArrayList<>();
        List<StepInformation> actual = projectService.getWorkFlow(project);
        assertEquals("Work flow doesn't match to given work flow!", expected, actual);
    }
}
