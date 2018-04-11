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
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.json.JsonObject;

import org.elasticsearch.index.query.Operator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.services.ServiceManager;

/**
 * Tests for ProjectService class.
 */
public class ProjectServiceIT {

    private static final ProjectService projectService = new ServiceManager().getProjectService();

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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProjects() throws Exception {
        Long amount = projectService.count();
        assertEquals("Projects were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldCountAllProjectsAccordingToQuery() throws Exception {
        String query = matchQuery("title", "First project").operator(Operator.AND).toString();
        Long amount = projectService.count(query);
        assertEquals("Projects were not counted correctly!", Long.valueOf(1), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForProjects() throws Exception {
        Long amount = projectService.countDatabaseRows();
        assertEquals("Projects were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindProject() throws Exception {
        ProjectDTO project = projectService.findById(1);
        boolean condition = project.getTitle().equals("First project") && project.getId().equals(1);
        assertTrue("Project was not found in index!", condition);
        assertTrue("Project was not found in index!", project.isActive().equals(true));
        assertEquals("Project was not found in index!", 1, project.getProcesses().size());

        project = projectService.findById(3);
        assertTrue("Project was not found in index!", project.isActive().equals(false));
    }

    @Test
    public void shouldFindAllProjects() throws Exception {
        List<ProjectDTO> projects = projectService.findAll();
        assertEquals("Not all projects were found in index!", 3, projects.size());
    }

    @Test
    public void shouldGetProject() throws Exception {
        Project project = projectService.getById(1);
        boolean condition = project.getTitle().equals("First project") && project.getId().equals(1);
        assertTrue("Project was not found in database!", condition);
    }

    @Test
    public void shouldGetAllProjects() {
        List<Project> projects = projectService.getAll();
        assertEquals("Not all projects were found in database!", 3, projects.size());
    }

    @Test
    public void shouldGetAllProjectsInGivenRange() throws Exception {
        List<Project> projects = projectService.getAll(2, 10);
        assertEquals("Not all projects were found in database!", 1, projects.size());
    }

    @Test
    public void shouldRemoveProjectById() throws Exception {
        Project project = new Project();
        project.setTitle("To Remove");
        projectService.save(project);
        Integer projectId = project.getId();
        Project foundProject = projectService.getById(projectId);
        assertEquals("Additional project was not inserted in database!", "To Remove", foundProject.getTitle());

        projectService.remove(foundProject);
        exception.expect(DAOException.class);
        projectService.getById(projectId);
    }

    @Test
    public void shouldRemoveProjectByObject() throws Exception {
        Project project = new Project();
        project.setTitle("To remove");
        projectService.save(project);
        Integer projectId = project.getId();
        Project foundProject = projectService.getById(projectId);
        assertEquals("Additional project was not inserted in database!", "To remove", foundProject.getTitle());

        projectService.remove(projectId);
        exception.expect(DAOException.class);
        projectService.getById(projectId);
    }

    @Test
    public void shouldFindById() throws Exception {
        String actual = projectService.findById(1).getTitle();
        String expected = "First project";
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByTitle() throws Exception {
        List<JsonObject> projects = projectService.findByTitle("First project", true);
        Integer actual = projects.size();
        Integer expected = 1;
        assertEquals("Project was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessId() throws Exception {
        JsonObject project = projectService.findByProcessId(1);
        Integer actual = projectService.getIdFromJSONObject(project);
        Integer expected = 1;
        assertEquals("Project were not found in index!", expected, actual);

        project = projectService.findByProcessId(4);
        actual = projectService.getIdFromJSONObject(project);
        expected = 0;
        assertEquals("Some project was found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessTitle() throws Exception {
        List<JsonObject> projects = projectService.findByProcessTitle("First process");
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
        List<JsonObject> projects = projectService.findByUserId(1);
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
        List<JsonObject> projects = projectService.findByUserLogin("kowal");
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
    public void shouldNotSaveProjectWithAlreadyExistingTitle() throws DataException {
        Project project = new Project();
        project.setTitle("First project");
        exception.expect(DataException.class);
        projectService.save(project);
    }

    @Test
    public void shouldDuplicateProject() throws DAOException {
        ProjectService projectService = new ServiceManager().getProjectService();

        Project initialProject = projectService.getById(1);

        Project duplicatedProject = projectService.duplicateProject(1);

        assertEquals(
            "DMS export file format of duplicated project does not match DMS export file format of original project!",
            duplicatedProject.getFileFormatDmsExport(), initialProject.getFileFormatDmsExport());
    }

    @Test
    public void shouldGetClientOfProject() throws Exception {
        Project project = projectService.getById(1);
        assertEquals("Client names doesnt match", "First client", project.getClient().getName());
    }
}
