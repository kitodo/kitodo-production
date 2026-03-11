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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.RulesetEditPage;
import org.kitodo.selenium.testframework.pages.TemplateEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.selenium.testframework.pages.WorkflowEditPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class EditingST extends BaseTestSelenium {

    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;
    private static UsersPage usersPage;

    @BeforeAll
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
        usersPage = Pages.getUsersPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void editProcessTest() throws Exception {
        processesPage.editProcess().changeProcessData();
        assertEquals("First process\n(ID: 1)", Pages.getProcessEditPage().getHeaderText(), "Header for edit process is incorrect");

        Pages.getProcessEditPage().save();
        assertTrue(processesPage.isAt(), "Redirection after save was not successful");

        Process processAfterEdit = ServiceManager.getProcessService().getById(1);

        assertEquals(4, processAfterEdit.getTemplates().size(), "Incorrect amount of template properties");
    }

    @Test
    public void editBatchTest() throws Exception {
        processesPage.editBatch();
        await().untilAsserted(() -> assertEquals(1, ServiceManager.getBatchService().getByQuery("FROM Batch WHERE title = 'SeleniumBatch'").size(), "Batch was not renamed!"));

        assertEquals(1, ServiceManager.getBatchService()
                .getByQuery("FROM Batch WHERE title = 'SeleniumBatch'").getFirst().getProcesses().size(),
                "Process was not removed from batch");
    }

    @Test
    public void editProjectTest() throws Exception {
        final String newProjectTitle = "newTitle";

        ProjectEditPage projectEditPage = projectsPage.editProject();
        assertEquals("Projekt bearbeiten (First project)", Pages.getProjectEditPage().getHeaderText(), "Header for edit project is incorrect");

        assertFalse(projectEditPage.areElementsEnabled());

        projectEditPage.changeTitle(newProjectTitle);
        projectEditPage.save();
        projectsPage.editProject();
        projectEditPage.toggleProjectActiveCheckbox();
        projectEditPage.save();
        boolean projectAvailable = Pages.getProjectsPage().getProjectsTitles().contains(newProjectTitle);
        assertTrue(projectAvailable, "Title was not changed");
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
        assertTrue(templateDetails.contains("First project"), "The first project should be assigned to this template");
        assertFalse(templateDetails.stream().anyMatch(listString -> listString.contains("Second project")), "The template is already assigned to second Project");

        TemplateEditPage editTemplatePage = projectsPage.editTemplate();
        assertEquals("Produktionsvorlage bearbeiten (Fourth template)", Pages.getTemplateEditPage().getHeaderText(), "Header for edit template is incorrect");

        editTemplatePage.addSecondProject();
        templateDetails = editTemplatePage.save().getTemplateDetails();
        assertTrue(templateDetails.stream().anyMatch(listString -> listString.contains("Second project")), "The second project should be assigned to this template");
    }

    @Test
    public void editWorkflowTest() throws Exception {
        String status = projectsPage.goToWorkflowTab().getWorkflowStatusForWorkflow();
        assertEquals("Entwurf", status, "Status is not correct");
        WorkflowEditPage workflowEditPage = projectsPage.editWorkflow();
        workflowEditPage.changeWorkflowStatusToActive();
        assertEquals("Workflow bearbeiten (test_second)", Pages.getWorkflowEditPage().getHeaderText(), "Header for edit workflow is incorrect");
        projectsPage = workflowEditPage.save();
        status = projectsPage.goToWorkflowTab().getWorkflowStatusForWorkflow();
        assertEquals("Aktiv", status, "Status change was not saved");
    }

    @Test
    public void editDocketTest() throws Exception {
        projectsPage.editDocket();
        assertEquals("Laufzettel bearbeiten (default)", Pages.getDocketEditPage().getHeaderText(), "Header for edit docket is incorrect");
    }

    @Test
    public void editRulesetTest() throws Exception {
        RulesetEditPage rulesetEditPage = projectsPage.editRuleset();
        List<WebElement> functionalMetadataLists = Browser.getDriver().findElements(By.className("functional-metadata-list"));
        assertEquals(11, functionalMetadataLists.size(), "Wrong number of functional metadata lists");
        assertEquals("HauptTitel",  functionalMetadataLists.get(7)
                .findElement(By.tagName("tbody"))
                .findElement(By.tagName("tr"))
                .findElement(By.tagName("td"))
                .findElement(By.tagName("span")).getText());
        assertEquals("Regelsatz bearbeiten (SLUBDD)", rulesetEditPage.getHeaderText(), "Header for edit ruleset is incorrect");
        rulesetEditPage.changeRuleset().save();
        assertTrue(projectsPage.isAt(), "Redirection after save was not successful");
    }

    @Test
    public void editUserTest() throws Exception {
        usersPage.editUser();
        assertEquals("Benutzer bearbeiten (null, Removable user)", Pages.getUserEditPage().getHeaderText(), "Header for edit user is incorrect");
    }

    @Test
    public void editRoleTest() throws Exception {
        usersPage.editRole();
        assertEquals("Rolle bearbeiten (Admin)", Pages.getRoleEditPage().getHeaderText(), "Header for edit role is incorrect");
    }

    @Test
    public void editLdapGroupTest() throws Exception {
        usersPage.editLdapGroup();
        assertEquals("LDAP-Gruppe bearbeiten (LG)", Pages.getLdapGroupEditPage().getHeaderText(), "Header for edit LDAP group is incorrect");
    }

    @Test
    public void editClientTest() throws Exception {
        usersPage.editClient();
        assertEquals("Mandant bearbeiten", Pages.getClientEditPage().getHeaderText(), "Header for edit client is incorrect");
    }
}
