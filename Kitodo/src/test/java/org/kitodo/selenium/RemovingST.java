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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UsersPage;

public class RemovingST extends BaseTestSelenium {

    private static UsersPage usersPage;
    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;

    @BeforeClass
    public static void setup() throws Exception {
        usersPage = Pages.getUsersPage();
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    @Test
    public void removeBatchTest() throws Exception {
        int batchesDisplayed = processesPage.countListedBatches();
        long batchesInDatabase = ServiceManager.getBatchService().count();
        assertTrue("Batch list is empty", batchesDisplayed > 0 && batchesInDatabase > 0);
        processesPage.deleteBatch();
        assertTrue("Removal of batch was not successful!",
                processesPage.countListedBatches() == batchesDisplayed - 1
                        && ServiceManager.getBatchService().count() == batchesInDatabase - 1);
    }

    @Test
    public void removeUserTest() throws Exception {
        int usersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = ServiceManager.getUserService().count();
        assertTrue("User list is empty", usersDisplayed > 0 && usersInDatabase > 0);
        usersPage.deleteRemovableUser();
        assertTrue("Removal of first user was not successful!",
            usersPage.countListedUsers() == usersDisplayed - 1
                    && ServiceManager.getUserService().count() == usersInDatabase - 1);
    }

    @Test
    public void removeRoleTest() throws Exception {
        int rolesDisplayed = usersPage.countListedRoles();
        long rolesInDatabase = ServiceManager.getRoleService().count();
        assertTrue("Role list is empty", rolesDisplayed > 0 && rolesInDatabase > 0);
        usersPage.deleteRemovableRole();
        assertTrue("Removal of first role was not successful!",
            usersPage.countListedRoles() == rolesDisplayed - 1
                    && ServiceManager.getRoleService().count() == rolesInDatabase - 1);
    }

    @Test
    public void removeClientTest() throws Exception {
        int clientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = ServiceManager.getClientService().count();
        assertTrue("Client list is empty", clientsDisplayed > 0 && clientsInDatabase > 0);
        usersPage.deleteRemovableClient();
        assertTrue("Removal of first client was not successful!",
            usersPage.countListedClients() == clientsDisplayed - 1
                    && ServiceManager.getClientService().count() == clientsInDatabase - 1);
    }

    @Test
    public void removeDocketTest() throws Exception {
        int docketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = ServiceManager.getDocketService().count();
        assertTrue("Docket list is empty", docketsDisplayed > 0 && docketsInDatabase > 0);
        projectsPage.deleteDocket();
        assertTrue("Removal of first docket was not successful!",
            projectsPage.countListedDockets() == docketsDisplayed - 1
                    && ServiceManager.getDocketService().count() == docketsInDatabase - 1);
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = ServiceManager.getRulesetService().count();
        assertTrue("Ruleset list is empty", rulesetsDisplayed > 0 && rulesetsInDatabase > 0);
        projectsPage.deleteRuleset();
        assertTrue("Removal of ruleset was not successful!",
            projectsPage.countListedRulesets() == rulesetsDisplayed - 1
                    && ServiceManager.getRulesetService().count() == rulesetsInDatabase - 1);
    }
}
