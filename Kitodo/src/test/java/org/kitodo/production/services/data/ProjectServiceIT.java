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
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;

/**
 * Tests for ProjectService class.
 */
public class ProjectServiceIT {

    private static final ProjectService projectService = ServiceManager.getProjectService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllProjects() {
        await().untilAsserted(
            () -> assertEquals("Projects were not counted correctly!", Long.valueOf(3), projectService.count()));
    }

    @Test
    public void shouldCountAllProjectsAccordingToQuery() {
        QueryBuilder query = matchQuery("title", "First project").operator(Operator.AND);
        await().untilAsserted(
            () -> assertEquals("Projects were not counted correctly!", Long.valueOf(1), projectService.count(query)));
    }

    @Test
    public void shouldCountAllDatabaseRowsForProjects() throws Exception {
        Long amount = projectService.countDatabaseRows();
        assertEquals("Projects were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindById() {
        await().untilAsserted(() -> assertTrue("Project was not found in index!",
            projectService.findById(1).getTitle().equals("First project")
                    && projectService.findById(1).getId().equals(1)));
        await().untilAsserted(
            () -> assertTrue("Project was not found in index!", projectService.findById(1).isActive()));
        await().untilAsserted(
            () -> assertEquals("Project was not found in index!", 1, projectService.findById(1).getTemplates().size()));

        await().untilAsserted(
            () -> assertFalse("Project was not found in index!", projectService.findById(3).isActive()));
    }

    @Test
    public void shouldFindAllProjects() {
        await().untilAsserted(
            () -> assertEquals("Not all projects were found in index!", 3, projectService.findAll().size()));
    }

    @Test
    public void shouldGetProject() throws Exception {
        Project project = projectService.getById(1);
        boolean condition = project.getTitle().equals("First project") && project.getId().equals(1);
        assertTrue("Project was not found in database!", condition);

        assertEquals("Project was found but templates were not inserted!", 1, project.getTemplates().size());
        assertEquals("Project was found but templates were not inserted!", 2, project.getProcesses().size());
    }

    @Test
    public void shouldGetAllProjects() throws Exception {
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
    public void shouldFindByTitle() {
        await().untilAsserted(() -> assertEquals("Project was not found in index!", 1,
            projectService.findByTitle("First project", true).size()));
    }

    @Test
    public void shouldFindByProcessId() {
        Integer expected = 1;
        await().untilAsserted(() -> assertEquals("Project were not found in index!", expected,
            projectService.getIdFromJSONObject(projectService.findByProcessId(1))));
    }

    @Test
    public void shouldNotFindByProcessId() {
        Integer expected = 0;
        await().untilAsserted(() -> assertEquals("Some project was found in index!", expected,
            projectService.getIdFromJSONObject(projectService.findByProcessId(4))));
    }

    @Test
    public void shouldFindByProcessTitle() {
        await().untilAsserted(() -> assertEquals("Project was not found in index!", 1,
            projectService.findByProcessTitle("First process").size()));
    }

    @Test
    public void shouldNotFindByProcessTitle() {
        await().untilAsserted(() -> assertEquals("Projects were found in index!", 0,
            projectService.findByProcessTitle("DBConnectionTest").size()));
    }

    @Test
    public void shouldFindByUserId() {
        await().untilAsserted(
            () -> assertEquals("Projects were not found in index!", 2, projectService.findByUserId(1).size()));

        await().untilAsserted(
            () -> assertEquals("Project was not found in index!", 1, projectService.findByUserId(3).size()));
    }

    @Test
    public void shouldFindByUserLogin() {
        await().untilAsserted(
            () -> assertEquals("Projects were not found in index!", 2, projectService.findByUserLogin("kowal").size()));

        await().untilAsserted(
            () -> assertEquals("Project was not found in index!", 1, projectService.findByUserLogin("dora").size()));
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
        Project initialProject = projectService.getById(1);
        Project duplicatedProject = projectService.duplicateProject(initialProject);

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
