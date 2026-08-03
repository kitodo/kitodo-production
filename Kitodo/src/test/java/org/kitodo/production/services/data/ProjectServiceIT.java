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

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.LinkingMode;
import org.kitodo.data.database.enums.PreviewHoverMode;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProjectDeletionException;
import org.kitodo.production.services.ServiceManager;

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
            return !Collections.singleton(projectService.getById(1)).isEmpty();
        });
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllProjects() throws DAOException {
        assertEquals(Long.valueOf(3), projectService.count(), "Projects were not counted correctly!");
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldCountAllProjectsAccordingToQuery() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldCountAllDatabaseRowsForProjects() throws Exception {
        Long amount = projectService.count();
        assertEquals(Long.valueOf(3), amount, "Projects were not counted correctly!");
    }

    @Test
    public void shouldFindById() throws DAOException {
        assertTrue(projectService.getById(1).getTitle().equals(firstProject) && projectService.getById(1).getId()
                .equals(1), projectNotFound);
        assertTrue(projectService.getById(1).isActive(), projectNotFound);
        assertEquals(2, projectService.getById(1).getTemplates().size(), projectNotFound);

        assertFalse(projectService.getById(3).isActive(), projectNotFound);
    }

    @Test
    public void shouldFindAllProjects() throws DAOException {
        assertEquals(3, projectService.getAll().size(), "Not all projects were found in index!");
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

        projectService.remove(foundProject);
        assertThrows(DAOException.class, () -> projectService.getById(projectId));
    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void shouldFindByTitle() throws Exception {
        // TODO delete test stub
    }

    @Test
    public void shouldNotSaveProjectWithAlreadyExistingTitle() {
        Project project = new Project();
        project.setTitle(firstProject);
        assertThrows(DAOException.class, () -> projectService.save(project));
    }

    @Test
    public void shouldGetClientOfProject() throws Exception {
        Project project = projectService.getById(1);
        assertEquals("First client", project.getClient().getName(), "Client names do not match");
    }

    @Test
    public void findByIds() throws Exception {
        List<Project> byQuery = ServiceManager.getUserService().getCurrentUser().getProjects();
        assertEquals(2, byQuery.size(), "Wrong amount of projects found");
    }

    @Test
    public void shouldReturnFalseWhenProjectHasNoProcesses() throws Exception {
        Project project = new Project();
        project.setTitle("Empty Project");
        Integer id = null;
        try {
            projectService.save(project);
            id = project.getId();
            boolean result = projectService.hasProcesses(id);
            assertFalse(result, "Project without processes incorrectly reported as having processes!");
        } finally {
            if (id != null) {
                projectService.remove(project);
            }
        }
    }

    @Test
    public void shouldReturnTrueWhenProjectHasProcesses() throws Exception {
        Project project = projectService.getById(1);
        boolean result = projectService.hasProcesses(project.getId());
        assertTrue(result, "Project with processes incorrectly reported as empty!");
    }

    @Test
    public void testProjectDuplication() throws DAOException {
        // create new project with all settings set
        Project baseProject = new Project();
        baseProject.setTitle("Project for duplication");
        ClientService clientService = ServiceManager.getClientService();
        baseProject.setClient(clientService.getById(1));
        LocalDate localDate = LocalDate.of(2026, 8, 7);
        baseProject.setStartDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        baseProject.setEndDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        baseProject.setNumberOfPages(32);
        baseProject.setNumberOfVolumes(2);
        baseProject.setActive(false);
        baseProject.setDmsImportRootPath("/foo/baar");
        baseProject.setMetsRightsOwner("Kitodo e.V.");
        baseProject.setMetsRightsOwnerLogo("kitodo.png");
        baseProject.setMetsRightsOwnerMail("foo@example.org");
        baseProject.setMetsRightsOwnerSite("www.kitodo.org");
        baseProject.setMetsDigiprovPresentation("Foo Baz");
        baseProject.setMetsDigiprovReference("Baz Foo");
        baseProject.setMetsPointerPath("pointer path");
        baseProject.setMetsPurl("purl");
        baseProject.setMetsContentIDs("contend id");

        // sub folder
        Folder firstFolder = new Folder();
        firstFolder.setFileGroup("FOO");
        firstFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/");
        firstFolder.setMimeType("image/jpeg");
        firstFolder.setPath("jpgs/max");
        firstFolder.setCopyFolder(true);
        firstFolder.setCreateFolder(true);
        firstFolder.setDerivative(1.0);
        firstFolder.setLinkingMode(LinkingMode.EXISTING);

        Folder secondFolder = new Folder();
        secondFolder.setFileGroup("BAR");
        secondFolder.setUrlStructure("http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/");
        secondFolder.setMimeType("image/jpeg");
        secondFolder.setPath("jpgs/default");
        secondFolder.setCopyFolder(true);
        secondFolder.setCreateFolder(true);
        secondFolder.setDerivative(0.8);
        secondFolder.setLinkingMode(LinkingMode.ALL);

        baseProject.getFolders().add(firstFolder);
        baseProject.getFolders().add(secondFolder);

        // set some folder use settings
        baseProject.setGeneratorSource(secondFolder);
        baseProject.setMediaView(secondFolder);
        baseProject.setPreview(firstFolder);
        baseProject.setPreviewHoverMode(PreviewHoverMode.TOOLTIP_PREVIEW);

        // duplicate the project
        ProjectService projectService = ServiceManager.getProjectService();
        Project duplicatedProject = projectService.duplicateProject(baseProject);

        // general settings
        assertTrue(duplicatedProject.getTitle().startsWith(baseProject.getTitle()));
        assertEquals(baseProject.getClient(), duplicatedProject.getClient());
        assertEquals(baseProject.getStartDate(), duplicatedProject.getStartDate());
        assertEquals(baseProject.getEndDate(), duplicatedProject.getEndDate());
        assertEquals(baseProject.getNumberOfPages(), duplicatedProject.getNumberOfPages());
        assertEquals(baseProject.getNumberOfVolumes(), duplicatedProject.getNumberOfVolumes());
        assertEquals(baseProject.isActive(), duplicatedProject.isActive());
        assertEquals(baseProject.getDmsImportRootPath(), duplicatedProject.getDmsImportRootPath());
        assertEquals(baseProject.getMetsRightsOwner(), duplicatedProject.getMetsRightsOwner());
        assertEquals(baseProject.getMetsRightsOwnerLogo(), duplicatedProject.getMetsRightsOwnerLogo());
        assertEquals(baseProject.getMetsRightsOwnerMail(), duplicatedProject.getMetsRightsOwnerMail());
        assertEquals(baseProject.getMetsRightsOwnerSite(), duplicatedProject.getMetsRightsOwnerSite());
        assertEquals(baseProject.getMetsDigiprovPresentation(), duplicatedProject.getMetsDigiprovPresentation());
        assertEquals(baseProject.getMetsDigiprovReference(), duplicatedProject.getMetsDigiprovReference());
        assertEquals(baseProject.getMetsPointerPath(), duplicatedProject.getMetsPointerPath());
        assertEquals(baseProject.getMetsPurl(), duplicatedProject.getMetsPurl());
        assertEquals(baseProject.getMetsContentIDs(), duplicatedProject.getMetsContentIDs());

        // simple sub folder check
        assertEquals(baseProject.getFolders().size(), duplicatedProject.getFolders().size());

        // folder use settings
        assertEquals(baseProject.getGeneratorSource(), duplicatedProject.getGeneratorSource());
        assertEquals(baseProject.getMediaView(), duplicatedProject.getMediaView());
        assertEquals(baseProject.getPreview(), duplicatedProject.getPreview());
        assertEquals(baseProject.getAudioMediaView(), duplicatedProject.getAudioMediaView());
        assertEquals(baseProject.getAudioPreview(), duplicatedProject.getAudioPreview());
        assertEquals(baseProject.isAudioMediaViewWaveform(), duplicatedProject.isAudioMediaViewWaveform());
        assertEquals(baseProject.getVideoMediaView(), duplicatedProject.getVideoMediaView());
        assertEquals(baseProject.getVideoPreview(), duplicatedProject.getVideoPreview());
        assertEquals(baseProject.getPreviewHoverMode(), duplicatedProject.getPreviewHoverMode());

        // project template
        assertEquals(baseProject.getTemplates(),  duplicatedProject.getTemplates());

        // import configuration
        assertEquals(
                baseProject.getDefaultImportConfiguration(),
                duplicatedProject.getDefaultImportConfiguration()
        );
        assertEquals(
                baseProject.getDefaultChildProcessImportConfiguration(),
                duplicatedProject.getDefaultChildProcessImportConfiguration()
        );
    }

    @Test
    public void shouldCheckWhetherProjectIsAssignedToCurrentUser()
            throws DAOException, ProjectDeletionException {
        UserService userService = ServiceManager.getUserService();
        User authenticatedUser = userService.getCurrentUser();
        Client sessionClient = userService.getSessionClientOfAuthenticatedUser();

        Project assignedProject = new Project();
        assignedProject.setTitle("Assigned project lookup test");
        assignedProject.setClient(sessionClient);

        assignedProject.getUsers().add(authenticatedUser);
        authenticatedUser.getProjects().add(assignedProject);

        try {
            projectService.save(assignedProject);
            userService.save(authenticatedUser);

            assertTrue(
                    projectService.isProjectAssignedToCurrentUser(assignedProject.getId()),
                    "Project should be assigned to the current user");

            assignedProject.getUsers().remove(authenticatedUser);
            authenticatedUser.getProjects().remove(assignedProject);

            projectService.save(assignedProject);
            userService.save(authenticatedUser);

            assertFalse(
                    projectService.isProjectAssignedToCurrentUser(assignedProject.getId()),
                    "Project should no longer be assigned to the current user");
        } finally {
            authenticatedUser.getProjects().remove(assignedProject);
            assignedProject.getUsers().remove(authenticatedUser);

            if (Objects.nonNull(assignedProject.getId())) {
                ProjectService.delete(assignedProject.getId());
            }
        }
    }
}
