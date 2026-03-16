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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private String randomWorkflowName;

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @AfterEach
    public void logout() throws Exception {
        FileService fileService = ServiceManager.getFileService();
        String diagramDirectory = ConfigCore.getKitodoDiagramDirectory();
        // remove XML and SVG files of migration workflow
        URI svgDiagramURI = new File(diagramDirectory + "migrationWorkflow.svg").toURI();
        URI xmlDiagramURI = new File(diagramDirectory + "migrationWorkflow.bpmn20.xml").toURI();
        fileService.delete(svgDiagramURI);
        fileService.delete(xmlDiagramURI);
        // remove XML file of workflow with randomly generated name
        xmlDiagramURI = new File(diagramDirectory + randomWorkflowName + ".bpmn20.xml").toURI();
        fileService.delete(xmlDiagramURI);
        Pages.getTopNavigation().logout();
    }

    @Test
    public void testMigration() throws Exception {
        Process process = ServiceManager.getProcessService().getById(1);
        process.setTemplate(null);
        ServiceManager.getProcessService().save(process);
        SystemPage systemPage = Pages.getSystemPage().goTo();

        assertNull(process.getTemplate(), "wrong template");
        systemPage.startWorkflowMigration();
        systemPage.selectProjects();
        assertEquals("Finished, Closed, Progress, Open, Locked", systemPage.getAggregatedTasks(2));
        WorkflowEditPage workflowEditPage = systemPage.createNewWorkflow();
        randomWorkflowName = workflowEditPage.getWorkflowTitle();
        workflowEditPage.changeWorkflowStatusToActive();
        assertTrue(workflowEditPage.getWorkflowTitle().contains("ChangeME"));
        workflowEditPage.changeWorkflowTitle("migrationWorkflow");
        systemPage = workflowEditPage.saveForMigration();
        String newTemplateTitle = "newTemplate";
        systemPage.createNewTemplateFromPopup(newTemplateTitle);
        await().pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions().untilAsserted(() -> assertEquals(5, (long) ServiceManager.getProcessService().getById(1).getTemplate().getId(), "template of process should have changed"));
        WorkflowService workflowService = ServiceManager.getWorkflowService();
        Workflow workflow = workflowService.getById(4);
        final long numberOfTemplates = workflow.getTemplates().size();
        final long workflowTemplateId = workflow.getTemplates().getFirst().getId();
        String processTemplateTitle = ServiceManager.getProcessService().getById(1).getTemplate().getTitle();

        assertEquals(1, numberOfTemplates, "only one template should be assigned");
        assertEquals(5, workflowTemplateId, "wrong template");
        assertEquals(newTemplateTitle, processTemplateTitle, "wrong title for template");
    }
}
