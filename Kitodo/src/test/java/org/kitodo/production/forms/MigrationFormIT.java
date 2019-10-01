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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;

public class MigrationFormIT {
    private MigrationForm migrationForm = new MigrationForm();

    /**
     * Setup Database and start elasticsearch.
     * 
     * @throws Exception
     *             If databaseConnection failed.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    /**
     * Cleanup the database and stop elasticsearch.
     *
     * @throws Exception
     *             if elasticsearch could not been stopped.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testShowPossibleProjects() {
        Assert.assertEquals("Projectslist should be empty", Collections.emptyList(), migrationForm.getAllProjects());
        Assert.assertFalse("Projectslist should not be shown", migrationForm.isProjectListShown());
        migrationForm.showPossibleProjects();
        Assert.assertEquals("Should get all Projects", 3, migrationForm.getAllProjects().size());
        Assert.assertTrue("Projectslist should be shown", migrationForm.isProjectListShown());
    }

    @Test
    public void testShowProcessesForProjects() throws DAOException {
        Assert.assertEquals(Collections.emptyList(), migrationForm.getAggregatedTasks());

        ArrayList<Project> selectedProjects = new ArrayList<>();
        selectedProjects.add(ServiceManager.getProjectService().getById(1));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        String processesShouldBeFound = "Processes should be found";
        Assert.assertEquals(processesShouldBeFound, 2, migrationForm.getAggregatedTasks().size());

        selectedProjects.add(ServiceManager.getProjectService().getById(2));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        Assert.assertEquals(processesShouldBeFound, 3, migrationForm.getAggregatedTasks().size());

        selectedProjects.add(ServiceManager.getProjectService().getById(2));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        Assert.assertEquals(processesShouldBeFound, 3, migrationForm.getAggregatedTasks().size());

        selectedProjects.remove(2);
        selectedProjects.remove(1);
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        Assert.assertEquals(processesShouldBeFound, 2, migrationForm.getAggregatedTasks().size());
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

        Assert.assertFalse("Lists should have a different size",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        Task taskTwo = new Task();
        taskTwo.setTitle("testTwo");
        tasksToCompare.add(taskTwo);

        Assert.assertFalse("Tasks should have different Titles",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        tasksToCompare.set(1, null);

        Assert.assertFalse("Null task should fail", migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        Task correctTaskOne = new Task();
        correctTaskOne.setTitle("Finished");
        correctTaskOne.setOrdering(1);

        Task correctTaskTwo = new Task();
        correctTaskTwo.setTitle("Blocking");
        correctTaskTwo.setOrdering(2);

        tasksToCompare.clear();
        tasksToCompare.add(correctTaskOne);
        tasksToCompare.add(correctTaskTwo);

        Assert.assertFalse("scriptPath should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskTwo.setScriptPath("../type/automatic/script/path");

        Assert.assertTrue("Tasks should be equal", migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setBatchStep(false);
        tasksToCompare.clear();
        tasksToCompare.add(correctTaskTwo);
        tasksToCompare.add(correctTaskOne);

        Assert.assertFalse("Tasks are in the wrong order", migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

    }

    @Test
    public void testBooleans() throws DAOException {
        TaskService taskService = ServiceManager.getTaskService();
        List<Task> originalTasks = new ArrayList<>();
        originalTasks.add(taskService.getById(1));
        originalTasks.add(taskService.getById(2));

        List<Task> tasksToCompare = new ArrayList<>();
        Task correctTaskOne = new Task();
        correctTaskOne.setTitle("Finished");
        correctTaskOne.setOrdering(1);

        Task correctTaskTwo = new Task();
        correctTaskTwo.setTitle("Blocking");
        correctTaskTwo.setOrdering(2);

        tasksToCompare.add(correctTaskOne);
        tasksToCompare.add(correctTaskTwo);

        correctTaskOne.setTypeMetadata(true);

        Assert.assertFalse("TypeMetadata should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeMetadata(false);
        correctTaskOne.setTypeImagesWrite(true);

        Assert.assertFalse("typeImagesWrite should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeImagesWrite(false);
        correctTaskOne.setTypeImagesRead(true);

        Assert.assertFalse("typeImagesRead should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeImagesRead(false);
        correctTaskOne.setTypeAutomatic(true);

        Assert.assertFalse("typeAutomatic should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeAutomatic(false);
        correctTaskOne.setTypeExportDMS(true);

        Assert.assertFalse("TypeExportDMS should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeExportDMS(false);
        correctTaskOne.setTypeAcceptClose(true);

        Assert.assertFalse("TypeAcceptClose should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeAcceptClose(false);
        correctTaskOne.setTypeCloseVerify(true);

        Assert.assertFalse("TypeCloseVerify should be different",
            migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

        correctTaskOne.setTypeCloseVerify(false);
        correctTaskOne.setBatchStep(true);

        Assert.assertFalse("batchStep should be different", migrationForm.tasksAreEqual(originalTasks, tasksToCompare));

    }

    @Test
    public void getMatchingTemplatesTest() throws DAOException {
        Template template = new Template();
        template.setDocket(ServiceManager.getDocketService().getById(2));
        template.setRuleset(ServiceManager.getRulesetService().getById(1));
        template.setClient(ServiceManager.getClientService().getById(1));

        List<Template> existingTemplates = ServiceManager.getTemplateService().getAll();

        HashSet newTemplates = new HashSet<>();
        newTemplates.add(template);
        Map matchingTemplates = migrationForm.getMatchingTemplates(newTemplates);

        Assert.assertNotNull(matchingTemplates.get(template));
        Assert.assertEquals(existingTemplates.get(0), matchingTemplates.get(template));

        template.setDocket(null);

        Assert.assertNull(matchingTemplates.get(template));
        Assert.assertNotEquals(existingTemplates.get(0), matchingTemplates.get(template));
    }

    @Test
    public void addProcessesToTemplateTest() throws DAOException, DataException {
        Template firstTemplate = ServiceManager.getTemplateService().getById(1);
        Template secondTemplate = ServiceManager.getTemplateService().getById(2);

        List<Process> firstTemplateProcesses = firstTemplate.getProcesses();
        Assert.assertEquals(2, firstTemplateProcesses.size());
        Assert.assertEquals(0, secondTemplate.getProcesses().size());
        Assert.assertEquals(1, (long) firstTemplateProcesses.get(0).getTemplate().getId());
        migrationForm.addProcessesToTemplate(secondTemplate, firstTemplateProcesses);

        Assert.assertEquals(2, firstTemplateProcesses.size());
        Assert.assertEquals(2, secondTemplate.getProcesses().size());
        Assert.assertEquals(2, (long) firstTemplateProcesses.get(0).getTemplate().getId());
    }

}
