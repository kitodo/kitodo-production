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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.TemplateEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.selenium.testframework.pages.WorkflowEditPage;

public class EditingST extends BaseTestSelenium {

    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;
    private static UsersPage usersPage;

    @BeforeClass
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
        usersPage = Pages.getUsersPage();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void editProcessTest() throws Exception {
        processesPage.editProcess().changeProcessData();
        assertEquals("Header for edit process is incorrect", "First process\n(ID: 1)",
            Pages.getProcessEditPage().getHeaderText());

        Pages.getProcessEditPage().save();
        assertTrue("Redirection after save was not successful", processesPage.isAt());

        Process processAfterEdit = ServiceManager.getProcessService().getById(1);

        assertEquals("Incorrect amount of template properties", 4, processAfterEdit.getTemplates().size());
    }

    @Test
    public void editBatchTest() throws Exception {
        processesPage.editBatch();
        await().untilAsserted(() -> assertEquals("Batch was not renamed!", 1,
                ServiceManager.getBatchService().getByQuery("FROM Batch WHERE title = 'SeleniumBatch'").size()));

        assertEquals("Process was not removed from batch", 1, ServiceManager.getBatchService()
                .getByQuery("FROM Batch WHERE title = 'SeleniumBatch'").get(0).getProcesses().size());
    }

    @Test
    public void editProjectTest() throws Exception {
        final String newProjectTitle = "newTitle";

        ProjectEditPage projectEditPage = projectsPage.editProject();
        assertEquals("Header for edit project is incorrect", "Projekt bearbeiten (First project)",
                Pages.getProjectEditPage().getHeaderText());

        assertFalse(projectEditPage.areElementsEnabled());

        projectEditPage.changeTitle(newProjectTitle);
        projectEditPage.save();
        projectsPage.editProject();
        projectEditPage.toggleProjectActiveCheckbox();
        projectEditPage.save();
        boolean projectAvailable = Pages.getProjectsPage().getProjectsTitles().contains(newProjectTitle);
        assertTrue("Title was not changed", projectAvailable);
        List<String> projectsActiveStates = projectsPage.getProjectsActiveStates();
        assertTrue(projectsActiveStates.contains("fa fa-minus-square-o fa-lg checkbox-unchecked"));

        projectEditPage = projectsPage.editProject();
        projectEditPage.toggleProjectActiveCheckbox().save();
        projectsActiveStates = projectsPage.getProjectsActiveStates();
        assertFalse(projectsActiveStates.contains("fa fa-minus-square-o fa-lg checkbox-unchecked"));

    }

    @Test
    public void editTemplateTest() throws Exception {
        projectsPage = projectsPage.goToTemplateTab();
        List<String> templateDetails = projectsPage.getTemplateDetails();
        assertTrue("The first project should be assigned to this template", templateDetails.contains("First project"));
        assertFalse("The template is already assigned to second Project",
            templateDetails.stream().anyMatch(listString -> listString.contains("Second project")));

        TemplateEditPage editTemplatePage = projectsPage.editTemplate();
        assertEquals("Header for edit template is incorrect", "Produktionsvorlage bearbeiten (Fourth template)",
            Pages.getTemplateEditPage().getHeaderText());

        editTemplatePage.addSecondProject();
        templateDetails = editTemplatePage.save().getTemplateDetails();
        assertTrue("The second project should be assigned to this template",
            templateDetails.stream().anyMatch(listString -> listString.contains("Second project")));
    }

    @Test
    public void editWorkflowTest() throws Exception {
        String status = projectsPage.goToWorkflowTab().getWorkflowStatusForWorkflow();
        assertEquals("Status is not correct", "Entwurf", status);
        WorkflowEditPage workflowEditPage = projectsPage.editWorkflow();
        workflowEditPage.changeWorkflowStatusToActive();
        assertEquals("Header for edit workflow is incorrect", "Workflow bearbeiten (test)",
            Pages.getWorkflowEditPage().getHeaderText());
        projectsPage = workflowEditPage.save();
        status = projectsPage.goToWorkflowTab().getWorkflowStatusForWorkflow();
        assertEquals("Status change was not saved", "Aktiv", status);
    }

    @Test
    public void editDocketTest() throws Exception {
        projectsPage.editDocket();
        assertEquals("Header for edit docket is incorrect", "Laufzettel bearbeiten (default)",
            Pages.getDocketEditPage().getHeaderText());
    }

    @Test
    public void editRulesetTest() throws Exception {
        projectsPage.editRuleset();
        assertEquals("Header for edit ruleset is incorrect", "Regelsatz bearbeiten (SLUBDD)",
            Pages.getRulesetEditPage().getHeaderText());
    }

    @Test
    public void editUserTest() throws Exception {
        usersPage.editUser();
        assertEquals("Header for edit user is incorrect", "Benutzer bearbeiten (null, Removable user)",
            Pages.getUserEditPage().getHeaderText());
    }

    @Test
    public void editRoleTest() throws Exception {
        usersPage.editRole();
        assertEquals("Header for edit role is incorrect", "Rolle bearbeiten (Admin)",
            Pages.getRoleEditPage().getHeaderText());
    }

    @Test
    public void editLdapGroupTest() throws Exception {
        usersPage.editLdapGroup();
        assertEquals("Header for edit LDAP group is incorrect", "LDAP-Gruppe bearbeiten (LG)",
            Pages.getLdapGroupEditPage().getHeaderText());
    }

    @Test
    public void editClientTest() throws Exception {
        usersPage.editClient();
        assertEquals("Header for edit client is incorrect", "Mandant bearbeiten",
            Pages.getClientEditPage().getHeaderText());
    }
}
