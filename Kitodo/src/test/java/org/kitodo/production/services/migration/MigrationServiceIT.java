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

package org.kitodo.production.services.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.data.TaskService;

public class MigrationServiceIT {

    private MigrationService migrationService = ServiceManager.getMigrationService();

    @BeforeEach
    public void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterEach
    public void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testTasksAreEqual() throws DAOException {
        TaskService taskService = ServiceManager.getTaskService();
        List<Task> originalTasks = new ArrayList<>();
        originalTasks.add(taskService.getById(1));
        originalTasks.add(taskService.getById(2));

        List<Task> tasksToCompare = new ArrayList<>();
        Task taskOne = new Task();
        taskOne.setTitle("test");
        tasksToCompare.add(taskOne);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "Lists should have a different size");

        Task taskTwo = new Task();
        taskTwo.setTitle("testTwo");
        tasksToCompare.add(taskTwo);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "Tasks should have different Titles");

        tasksToCompare.set(1, null);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "Null task should fail");

        Task correctTaskOne = new Task();
        correctTaskOne.setTitle("Finished");
        correctTaskOne.setOrdering(1);

        Task correctTaskTwo = new Task();
        correctTaskTwo.setTitle("Blocking");
        correctTaskTwo.setOrdering(2);

        tasksToCompare.clear();
        tasksToCompare.add(correctTaskOne);
        tasksToCompare.add(correctTaskTwo);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "scriptPath should be different");

        correctTaskTwo.setScriptPath("../type/automatic/script/path");

        assertTrue(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "Tasks should be equal");

        correctTaskOne.setBatchStep(false);
        tasksToCompare.clear();
        tasksToCompare.add(correctTaskTwo);
        tasksToCompare.add(correctTaskOne);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "Tasks are in the wrong order");

    }

    @Test
    public void testBooleans() throws DAOException {
        TaskService taskService = ServiceManager.getTaskService();
        List<Task> originalTasks = new ArrayList<>();
        originalTasks.add(taskService.getById(1));
        originalTasks.add(taskService.getById(2));

        Task correctTaskOne = new Task();
        correctTaskOne.setTitle("Finished");
        correctTaskOne.setOrdering(1);

        Task correctTaskTwo = new Task();
        correctTaskTwo.setTitle("Blocking");
        correctTaskTwo.setOrdering(2);

        List<Task> tasksToCompare = new ArrayList<>();
        tasksToCompare.add(correctTaskOne);
        tasksToCompare.add(correctTaskTwo);

        correctTaskOne.setTypeMetadata(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "TypeMetadata should be different");

        correctTaskOne.setTypeMetadata(false);
        correctTaskOne.setTypeImagesWrite(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "typeImagesWrite should be different");

        correctTaskOne.setTypeImagesWrite(false);
        correctTaskOne.setTypeImagesRead(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "typeImagesRead should be different");

        correctTaskOne.setTypeImagesRead(false);
        correctTaskOne.setTypeAutomatic(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "typeAutomatic should be different");

        correctTaskOne.setTypeAutomatic(false);
        correctTaskOne.setTypeExportDMS(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "TypeExportDMS should be different");

        correctTaskOne.setTypeExportDMS(false);
        correctTaskOne.setTypeAcceptClose(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "TypeAcceptClose should be different");

        correctTaskOne.setTypeAcceptClose(false);
        correctTaskOne.setTypeCloseVerify(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "TypeCloseVerify should be different");

        correctTaskOne.setTypeCloseVerify(false);
        correctTaskOne.setBatchStep(true);

        assertFalse(migrationService.tasksAreEqual(originalTasks, tasksToCompare), "batchStep should be different");

    }

    @Test
    @Disabled("functionality nowhere used, no longer implemented")
    public void getMatchingTemplatesTest() throws DAOException {
        Template template = new Template();
        template.setDocket(ServiceManager.getDocketService().getById(2));
        template.setRuleset(ServiceManager.getRulesetService().getById(1));
        template.setClient(ServiceManager.getClientService().getById(1));
        template.setWorkflow(ServiceManager.getWorkflowService().getById(1));

        List<Template> existingTemplates = ServiceManager.getTemplateService().getAll();

        HashSet<Template> newTemplates = new HashSet<>();
        newTemplates.add(template);
        Map<Template, Template> matchingTemplates = migrationService.getMatchingTemplates(newTemplates);

        assertNotNull(matchingTemplates.get(template));
        assertEquals(existingTemplates.getFirst(), matchingTemplates.get(template));
    }

    @Test
    public void testAddToTemplate() throws DAOException {
        ProcessService processService = ServiceManager.getProcessService();
        Project project = ServiceManager.getProjectService().getById(1);
        Process firstProcess = new Process();
        firstProcess.setTitle("firstMigrationProcess");
        firstProcess.setProject(project);
        project.getProcesses().add(firstProcess);
        ServiceManager.getProjectService().save(project);
        processService.save(firstProcess);
        Process secondProcess = new Process();
        secondProcess.setTitle("secondMigrationProcess");
        processService.save(secondProcess);

        List<Process> processes = new ArrayList<>();
        processes.add(firstProcess);
        processes.add(secondProcess);

        Template template = new Template();
        template.setTitle("testTemplate");
        ServiceManager.getTemplateService().save(template);
        assertEquals(0, template.getProcesses().size());
        assertNull(firstProcess.getTemplate());
        assertNull(secondProcess.getTemplate());

        migrationService.addProcessesToTemplate(template, processes);

        ServiceManager.getTemplateService().refresh(template);
        assertEquals(2, template.getProcesses().size());
        assertEquals(5, (long) firstProcess.getTemplate().getId());
        assertEquals(5, (long) secondProcess.getTemplate().getId());
    }

    @Test
    public void addProcessesToTemplateTest() throws DAOException {
        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        Template secondTemplate = ServiceManager.getTemplateService().getById(2);

        List<Process> firstTemplateProcesses = firstTemplate.getProcesses();
        assertEquals(2, firstTemplateProcesses.size());
        assertEquals(0, secondTemplate.getProcesses().size());
        assertEquals(1, (long) firstTemplateProcesses.getFirst().getTemplate().getId());
        migrationService.addProcessesToTemplate(secondTemplate, firstTemplateProcesses);

        assertEquals(2, firstTemplateProcesses.size());
        secondTemplate = ServiceManager.getTemplateService().getById(2);
        assertEquals(2, secondTemplate.getProcesses().size());
        assertEquals(2, (long) firstTemplateProcesses.getFirst().getTemplate().getId());
    }

    @Test
    public void testCreateTemplatesForProcesses() throws DAOException {
        Workflow workflow = ServiceManager.getWorkflowService().getById(1);
        List<Process> processes = ServiceManager.getProjectService().getById(1).getProcesses();
        Map<Template, List<Process>> templatesForProcesses = migrationService.createTemplatesForProcesses(processes,
            workflow);

        assertEquals(1, templatesForProcesses.size());
        assertEquals(processes.getFirst().getDocket(), templatesForProcesses.keySet().iterator().next().getDocket());
        assertEquals(processes.getFirst().getRuleset(), templatesForProcesses.keySet().iterator().next().getRuleset());
        assertEquals(2, templatesForProcesses.values().iterator().next().size());

    }

    @Test
    public void testCreateTaskString() throws DAOException {
        assertEquals("Finished, Closed, Progress, Open, Locked" + MigrationService.SEPARATOR + "9c43055e", migrationService.createTaskString(ServiceManager.getProcessService().getById(1).getTasks()));
        List<Task> secondTasks = ServiceManager.getProcessService().getById(2).getTasks();
        assertEquals("Additional, Processed and Some, Next Open" + MigrationService.SEPARATOR + "848a8483", migrationService.createTaskString(secondTasks));
        secondTasks.getFirst().setTitle("test/test");
        assertEquals("test/test, Processed and Some, Next Open" + MigrationService.SEPARATOR + "56f49a2b", migrationService.createTaskString(secondTasks));
        assertEquals(MigrationService.SEPARATOR + "0", migrationService.createTaskString(ServiceManager.getProcessService().getById(3).getTasks()));
    }

    @Test
    public void testTitleIsValid() throws DAOException {
        Template newTemplate = new Template();
        newTemplate.setClient(ServiceManager.getClientService().getById(1));
        assertFalse(migrationService.isTitleValid(newTemplate));
        newTemplate.setTitle("test");
        assertTrue(migrationService.isTitleValid(newTemplate));
        newTemplate.setTitle("First template");
        assertFalse(migrationService.isTitleValid(newTemplate));
    }
}
