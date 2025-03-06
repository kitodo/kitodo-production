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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opensearch.index.query.QueryBuilders.matchQuery;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.services.ServiceManager;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilder;

/**
 * Tests for ProjectService class.
 */
public class ProjectServiceIT {

    private static final ProjectService projectService = ServiceManager.getProjectService();
    private static final String firstProject = "First project";
    private static final String projectNotFound = "Project was not found in index!";

    @BeforeAll
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

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllProjects() throws DataException {
        assertEquals(Long.valueOf(3), projectService.count(), "Projects were not counted correctly!");
    }

    @Test
    public void shouldCountAllProjectsAccordingToQuery() throws DataException {
        QueryBuilder query = matchQuery("title", firstProject).operator(Operator.AND);
        assertEquals(Long.valueOf(1), projectService.count(query), "Projects were not counted correctly!");
    }

    @Test
    public void shouldCountAllDatabaseRowsForProjects() throws Exception {
        Long amount = projectService.countDatabaseRows();
        assertEquals(Long.valueOf(3), amount, "Projects were not counted correctly!");
    }

    @Test
    public void shouldFindById() throws DataException {
        assertTrue(projectService.findById(1).getTitle().equals(firstProject) && projectService.findById(1).getId().equals(1), projectNotFound);
        assertTrue(projectService.findById(1).isActive(), projectNotFound);
        assertEquals(2, projectService.findById(1).getTemplates().size(), projectNotFound);

        assertFalse(projectService.findById(3).isActive(), projectNotFound);
    }

    @Test
    public void shouldFindAllProjects() throws DataException {
        assertEquals(3, projectService.findAll().size(), "Not all projects were found in index!");
    }

    @Test
    public void shouldGetProject() throws Exception {
        Project project = projectService.getById(1);
        boolean condition = project.getTitle().equals(firstProject) && project.getId().equals(1);
        assertTrue(condition, "Project was not found in database!");

        assertEquals(2, project.getTemplates().size(), "Project was found but templates were not inserted!");
        assertEquals(2, project.getProcesses().size(), "Project was found but templates were not inserted!");
    }

    @Test
    public void shouldGetAllProjects() throws Exception {
        List<Project> projects = projectService.getAll();
        assertEquals(3, projects.size(), "Not all projects were found in database!");
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
        assertEquals(1, projects.size(), "Not all projects were found in database!");
    }

    @Test
    public void shouldRemoveProjectById() throws Exception {
        Project project = new Project();
        project.setTitle("To Remove");
        projectService.save(project);
        Integer projectId = project.getId();
        Project foundProject = projectService.getById(projectId);
        assertEquals("To Remove", foundProject.getTitle(), "Additional project was not inserted in database!");

        projectService.remove(foundProject);
        assertThrows(DAOException.class, () -> projectService.getById(projectId));
    }

    @Test
    public void shouldRemoveProjectByObject() throws Exception {
        Project project = new Project();
        project.setTitle("To remove");
        projectService.save(project);
        Integer projectId = project.getId();
        Project foundProject = projectService.getById(projectId);
        assertEquals("To remove", foundProject.getTitle(), "Additional project was not inserted in database!");

        projectService.remove(projectId);
        assertThrows(DAOException.class, () -> projectService.getById(projectId));
    }

    @Test
    public void shouldFindByTitle() throws DataException {
        assertEquals(1, projectService.findByTitle(firstProject, true).size(), projectNotFound);
    }

    @Test
    public void shouldNotSaveProjectWithAlreadyExistingTitle() {
        Project project = new Project();
        project.setTitle(firstProject);
        assertThrows(DataException.class,  () -> projectService.save(project));
    }

    @Test
    public void shouldGetClientOfProject() throws Exception {
        Project project = projectService.getById(1);
        assertEquals("First client", project.getClient().getName(), "Client names do not match");
    }

    @Test
    public void findByIds() throws DataException {
        ProjectService projectService = ServiceManager.getProjectService();
        QueryBuilder projectsForCurrentUserQuery = projectService.getProjectsForCurrentUserQuery();
        List<ProjectDTO> byQuery = projectService.findByQuery(projectsForCurrentUserQuery, true);
        assertEquals(2, byQuery.size(), "Wrong amount of projects found");
    }
}
