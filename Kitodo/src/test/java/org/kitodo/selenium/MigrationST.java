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

package org.kitodo.selenium;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.WorkflowService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.SystemPage;
import org.kitodo.selenium.testframework.pages.WorkflowEditPage;

public class MigrationST extends BaseTestSelenium {

    @BeforeClass
    public static void setup() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        FileService fileService = ServiceManager.getFileService();
        String diagramDirectory = ConfigCore.getKitodoDiagramDirectory();
        URI svgDiagramURI = new File(diagramDirectory + "Task1ScriptTaskTask3Task4Task5.svg").toURI();
        URI xmlDiagramURI = new File(diagramDirectory + "Task1ScriptTaskTask3Task4Task5.bpmn20.xml").toURI();
        fileService.delete(svgDiagramURI);
        fileService.delete(xmlDiagramURI);
        Pages.getTopNavigation().logout();
    }

    @Test
    public void testMigration() throws Exception {
        Process process = ServiceManager.getProcessService().getById(4);
        process.setTemplate(null);
        ServiceManager.getProcessService().save(process);
        SystemPage systemPage = Pages.getSystemPage().goTo();

        assertEquals("wrong template", null, process.getTemplate());
        systemPage.startWorkflowMigration();
        systemPage.selectProjects();
        assertEquals("Task1ScriptTaskTask3Task4Task5", systemPage.getAggregatedTasks(2));
        WorkflowEditPage workflowEditPage = systemPage.createNewWorkflow();
        workflowEditPage.changeWorkflowStatusToActive();
        assertEquals("Task1ScriptTaskTask3Task4Task5", workflowEditPage.getWorkflowTitle());
        systemPage = workflowEditPage.saveForMigration();
        String newTemplateTitle = "newTemplate";
        systemPage.createNewTemplateFromPopup(newTemplateTitle);
        await().pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions().untilAsserted(() -> assertEquals("template of process should have changed", 4,
            (long) ServiceManager.getProcessService().getById(1).getTemplate().getId()));
        WorkflowService workflowService = ServiceManager.getWorkflowService();
        Workflow workflow = workflowService.getById(4);
        final long numberOfTemplates = workflow.getTemplates().size();
        final long workflowTemplateId = workflow.getTemplates().get(0).getId();
        String processTemplateTitle = ServiceManager.getProcessService().getById(1).getTemplate().getTitle();

        assertEquals("only one template should be assigned", 1, numberOfTemplates);
        assertEquals("wrong template", 4, workflowTemplateId);
        assertEquals("wrong title for template", newTemplateTitle, processTemplateTitle);
    }
}
