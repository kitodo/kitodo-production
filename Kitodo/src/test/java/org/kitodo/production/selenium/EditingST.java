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

package org.kitodo.production.selenium;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.production.services.ServiceManager;

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
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        processesPage.editProcess().changeProcessData();
        assertEquals("Header for edit process is incorrect", "Vorgang bearbeiten (First process)",
            Pages.getProcessEditPage().getHeaderText());

        Pages.getProcessEditPage().save();
        assertTrue("Redirection after save was not successful", processesPage.isAt());

        Process processAfterEdit = ServiceManager.getProcessService().getById(1);

        assertEquals("Incorrect amount of template properties", 4,
                processAfterEdit.getTemplates().size());
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
        projectsPage.editProject();
        assertEquals("Header for edit project is incorrect", "Projekt bearbeiten (First project)",
            Pages.getProjectEditPage().getHeaderText());
    }

    @Test
    public void editTemplateTest() throws Exception {
        projectsPage.editTemplate();
        assertEquals("Header for edit template is incorrect", "Produktionsvorlage bearbeiten (First template)",
            Pages.getTemplateEditPage().getHeaderText());
    }

    @Test
    public void editWorkflowTest() throws Exception {
        projectsPage.editWorkflow();
        assertEquals("Header for edit ruleset is incorrect", "Workflow bearbeiten (say-hello)",
            Pages.getWorkflowEditPage().getHeaderText());
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

    @Ignore("user page is there, link is there, not clicking - find out why")
    @Test
    public void editUserTest() throws Exception {
        usersPage.editUser();
        assertEquals("Header for edit user is incorrect", "Benutzer bearbeiten (Kowalski, Jan)",
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
