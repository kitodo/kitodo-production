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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.LinkingMode;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ProcessState;
import org.kitodo.production.forms.process.ProcessListView;
import org.kitodo.production.forms.task.TaskEditView;
import org.kitodo.production.services.ServiceManager;

public class ProcessListViewIT {

    private final ProcessListView processListView = new ProcessListView();

    /**
     * Setup Database and start elasticsearch.
     *
     * @throws Exception If databaseConnection failed.
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        addProcesses();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    private static void addProcesses() throws Exception {
        Project projectOne = ServiceManager.getProjectService().getById(1);
        Template template = ServiceManager.getTemplateService().getById(1);

        Process forthProcess = new Process();
        forthProcess.setTitle("Forth process");
        LocalDate localDate = LocalDate.of(2020, 3, 20);
        forthProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        forthProcess.setDocket(ServiceManager.getDocketService().getById(1));
        forthProcess.setProject(projectOne);
        forthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        forthProcess.setTemplate(template);
        ServiceManager.getProcessService().save(forthProcess);

        Process fifthProcess = new Process();
        fifthProcess.setTitle("Fifth process");
        localDate = LocalDate.of(2020, 4, 20);
        fifthProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        fifthProcess.setDocket(ServiceManager.getDocketService().getById(1));
        fifthProcess.setProject(projectOne);
        fifthProcess.setRuleset(ServiceManager.getRulesetService().getById(1));
        fifthProcess.setTemplate(template);
        ServiceManager.getProcessService().save(fifthProcess);
    }

    /**
     * Cleanup the database and stop elasticsearch.
     *
     * @throws Exception if elasticsearch could not been stopped.
     */
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    /**
     * Tests the selection in the process data table.
     */
    @Test
    public void testProcessesSelection() throws Exception {
        List<Integer> selectedIds;
        processListView.setAllSelected(true);

        selectedIds = processListView.getSelectedProcesses()
                .stream().map(Process::getId).sorted().collect(Collectors.toList());
        assertEquals(new ArrayList<>(Arrays.asList(1, 2, 4, 5)), selectedIds);

        processListView.getExcludedProcessIds().add(4);
        selectedIds = processListView.getSelectedProcesses()
                .stream().map(Process::getId).sorted().collect(Collectors.toList());

        assertEquals(new ArrayList<>(Arrays.asList(1, 2, 5)), selectedIds);

        processListView.setAllSelected(false);
        selectedIds = processListView.getSelectedProcesses()
                .stream().map(Process::getId).sorted().collect(Collectors.toList());
        assertEquals(new ArrayList<>(Arrays.asList(1, 2, 5)), selectedIds);
    }

    @Test
    public void testSortHelperStatusIsUpdatedOnTaskSave() throws DAOException {
        Process process = ServiceManager.getProcessService().getById(1);

        // Close all tasks
        for (Task task : process.getTasks()) {
            task.setProcessingStatus(TaskStatus.DONE);
        }
        ServiceManager.getProcessService().save(process);

        assertEquals(ProcessState.COMPLETED.getValue(), process.getSortHelperStatus());

        // Reopen one task
        Task lastTask = process.getTasks().get(process.getTasks().size() - 1);
        
        TaskEditView form = new TaskEditView();
        form.load(lastTask.getId());
        form.getTask().setProcessingStatus(TaskStatus.OPEN);
        form.saveTaskAndRedirect();

        Process updated = ServiceManager.getProcessService().getById(process.getId());
        assertNotEquals(ProcessState.COMPLETED.getValue(), updated.getSortHelperStatus());
    }

    @Test
    public void shouldDeleteSimpleProjectFromListView() throws Exception {
        Project project = new Project();
        project.setTitle("Simple project to delete");
        ServiceManager.getProjectService().save(project);

        int projectId = project.getId();

        assertEquals(projectId, ServiceManager.getProjectService().getById(projectId).getId());

        ProjectListView projectListView = new ProjectListView();

        assertDoesNotThrow(() -> projectListView.delete(projectId));

        assertThrows(DAOException.class, () -> ServiceManager.getProjectService().getById(projectId));
    }

    @Test
    public void shouldDeleteProjectWithTemplateFoldersAndUsersFromListView() throws Exception {
        User user = ServiceManager.getUserService().getById(1);

        Project project = new Project();
        project.setTitle("Complex project to delete");
        project.setClient(ServiceManager.getClientService().getById(1));
        project.getUsers().add(user);
        ServiceManager.getProjectService().save(project);

        user.getProjects().add(project);
        ServiceManager.getUserService().save(user);

        Template template = new Template();
        template.setTitle("Template of complex project to delete");
        template.setClient(project.getClient());
        template.setDocket(ServiceManager.getDocketService().getById(1));
        template.setRuleset(ServiceManager.getRulesetService().getById(1));
        template.getProjects().add(project);
        ServiceManager.getTemplateService().save(template);

        project.getTemplates().add(template);

        Folder mediaFolder = new Folder();
        mediaFolder.setFileGroup("DEFAULT");
        mediaFolder.setMimeType("image/jpeg");
        mediaFolder.setPath("images/default");
        mediaFolder.setCopyFolder(true);
        mediaFolder.setCreateFolder(true);
        mediaFolder.setLinkingMode(LinkingMode.ALL);
        mediaFolder.setProject(project);
        project.getFolders().add(mediaFolder);
        project.setMediaView(mediaFolder);

        Folder sourceFolder = new Folder();
        sourceFolder.setFileGroup("SOURCE");
        sourceFolder.setMimeType("image/tiff");
        sourceFolder.setPath("images/source");
        sourceFolder.setCopyFolder(false);
        sourceFolder.setCreateFolder(true);
        sourceFolder.setLinkingMode(LinkingMode.NO);
        sourceFolder.setProject(project);
        project.getFolders().add(sourceFolder);
        project.setGeneratorSource(sourceFolder);

        ServiceManager.getProjectService().save(project);

        int projectId = project.getId();
        int userId = user.getId();
        int templateId = template.getId();

        Project savedProject = ServiceManager.getProjectService().getById(projectId);

        List<Integer> folderIds = savedProject.getFolders()
                .stream()
                .map(Folder::getId)
                .toList();

        assertEquals(projectId, savedProject.getId());
        assertEquals(2, folderIds.size());
        assertTrue(ServiceManager.getUserService().getById(userId).getProjects()
                .stream()
                .anyMatch(userProject -> userProject.getId().equals(projectId)));
        assertTrue(ServiceManager.getTemplateService().getById(templateId).getProjects()
                .stream()
                .anyMatch(templateProject -> templateProject.getId().equals(projectId)));

        for (Integer folderId : folderIds) {
            assertEquals(folderId, ServiceManager.getFolderService().getById(folderId).getId());
        }

        ProjectListView projectListView = new ProjectListView();
        assertDoesNotThrow(() -> projectListView.delete(projectId));
        assertThrows(DAOException.class, () -> ServiceManager.getProjectService().getById(projectId));

        for (Integer folderId : folderIds) {
            assertThrows(DAOException.class, () -> ServiceManager.getFolderService().getById(folderId));
        }

        assertFalse(ServiceManager.getUserService().getById(userId).getProjects()
                .stream()
                .anyMatch(userProject -> userProject.getId().equals(projectId)));
        assertFalse(ServiceManager.getTemplateService().getById(templateId).getProjects()
                .stream()
                .anyMatch(templateProject -> templateProject.getId().equals(projectId)));
    }
}
