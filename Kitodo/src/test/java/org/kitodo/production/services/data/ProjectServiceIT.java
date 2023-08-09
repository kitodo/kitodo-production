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

import static org.awaitility.Awaitility.await;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for ProjectService class.
 */
public class ProjectServiceIT {

    private static final ProjectService projectService = ServiceManager.getProjectService();
    private static final String firstProject = "First project";
    private static final String projectNotFound = "Project was not found in index!";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !projectService.findByTitle(firstProject, true).isEmpty();
        });
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProjects() throws DataException {
        assertEquals("Projects were not counted correctly!", Long.valueOf(3), projectService.count());
    }

    @Test
    public void shouldCountAllProjectsAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", firstProject).operator(Operator.AND);
        assertEquals("Projects were not counted correctly!", Long.valueOf(1), projectService.count(query));
    }

    @Test
    public void shouldCountAllDatabaseRowsForProjects() throws Exception {
        Long amount = projectService.countDatabaseRows();
        assertEquals("Projects were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindById() throws DataException {
        assertTrue(projectNotFound,
            projectService.findById(1).getTitle().equals(firstProject) && projectService.findById(1).getId().equals(1));
        assertTrue(projectNotFound, projectService.findById(1).isActive());
        assertEquals(projectNotFound, 2, projectService.findById(1).getTemplates().size());

        assertFalse(projectNotFound, projectService.findById(3).isActive());
    }

    @Test
    public void shouldFindAllProjects() throws DataException {
        assertEquals("Not all projects were found in index!", 3, projectService.findAll().size());
    }

    @Test
    public void shouldGetProject() throws Exception {
        Project project = projectService.getById(1);
        boolean condition = project.getTitle().equals(firstProject) && project.getId().equals(1);
        assertTrue("Project was not found in database!", condition);

        assertEquals("Project was found but templates were not inserted!", 2, project.getTemplates().size());
        assertEquals("Project was found but templates were not inserted!", 2, project.getProcesses().size());
    }

    @Test
    public void shouldGetAllProjects() throws Exception {
        List<Project> projects = projectService.getAll();
        assertEquals("Not all projects were found in database!", 3, projects.size());
    }

    @Test
    public void shouldGetClientProjectsSortedByTitle() {
        List<Project> projects = projectService.getAllForSelectedClient();
        assertEquals("First project", projects.get(0).getTitle());
        assertEquals("Second project", projects.get(1).getTitle());
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
    public void shouldFindByTitle() throws DataException {
        assertEquals(projectNotFound, 1, projectService.findByTitle(firstProject, true).size());
    }

    @Test
    public void shouldNotSaveProjectWithAlreadyExistingTitle() throws DataException {
        Project project = new Project();
        project.setTitle(firstProject);
        exception.expect(DataException.class);
        projectService.save(project);
    }

    @Test
    public void shouldGetClientOfProject() throws Exception {
        Project project = projectService.getById(1);
        assertEquals("Client names doesnt match", "First client", project.getClient().getName());
    }

    @Test
    public void findByIds() throws DataException {
        ProjectService projectService = ServiceManager.getProjectService();
        QueryBuilder projectsForCurrentUserQuery = projectService.getProjectsForCurrentUserQuery();
        List<ProjectDTO> byQuery = projectService.findByQuery(projectsForCurrentUserQuery, true);
        assertEquals("Wrong amount of projects found",2,byQuery.size());
    }
}
