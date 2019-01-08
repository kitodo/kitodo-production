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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.production.services.ServiceManager;

import static org.junit.Assert.assertTrue;

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
        long batchesInDatabase = ServiceManager.getBatchService().countDatabaseRows();
        assertTrue("Batch list is empty", batchesDisplayed > 0 && batchesInDatabase > 0);
        processesPage.deleteBatch();
        assertTrue("Removal of batch was not successful!",
                processesPage.countListedBatches() == batchesDisplayed - 1
                        && ServiceManager.getBatchService().countDatabaseRows() == batchesInDatabase - 1);
    }

    @Test
    public void removeUserTest() throws Exception {
        int usersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = ServiceManager.getUserService().countDatabaseRows();
        assertTrue("User list is empty", usersDisplayed > 0 && usersInDatabase > 0);
        usersPage.deleteRemovableUser();
        assertTrue("Removal of first user was not successful!",
            usersPage.countListedUsers() == usersDisplayed - 1
                    && ServiceManager.getUserService().countDatabaseRows() == usersInDatabase - 1);
    }

    @Test
    public void removeRoleTest() throws Exception {
        int rolesDisplayed = usersPage.countListedRoles();
        long rolesInDatabase = ServiceManager.getRoleService().countDatabaseRows();
        assertTrue("Role list is empty", rolesDisplayed > 0 && rolesInDatabase > 0);
        usersPage.deleteRemovableRole();
        assertTrue("Removal of first role was not successful!",
            usersPage.countListedRoles() == rolesDisplayed - 1
                    && ServiceManager.getRoleService().countDatabaseRows() == rolesInDatabase - 1);
    }

    @Test
    public void removeClientTest() throws Exception {
        int clientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = ServiceManager.getClientService().countDatabaseRows();
        assertTrue("Client list is empty", clientsDisplayed > 0 && clientsInDatabase > 0);
        usersPage.deleteRemovableClient();
        assertTrue("Removal of first client was not successful!",
            usersPage.countListedClients() == clientsDisplayed - 1
                    && ServiceManager.getClientService().countDatabaseRows() == clientsInDatabase - 1);
    }

    @Test
    public void removeDocketTest() throws Exception {
        int docketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = ServiceManager.getDocketService().countDatabaseRows();
        assertTrue("Docket list is empty", docketsDisplayed > 0 && docketsInDatabase > 0);
        projectsPage.deleteDocket();
        assertTrue("Removal of first docket was not successful!",
            projectsPage.countListedDockets() == docketsDisplayed - 1
                    && ServiceManager.getDocketService().countDatabaseRows() == docketsInDatabase - 1);
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = ServiceManager.getRulesetService().countDatabaseRows();
        assertTrue("Ruleset list is empty", rulesetsDisplayed > 0 && rulesetsInDatabase > 0);
        projectsPage.deleteRuleset();
        assertTrue("Removal of ruleset was not successful!",
            projectsPage.countListedRulesets() == rulesetsDisplayed - 1
                    && ServiceManager.getRulesetService().countDatabaseRows() == rulesetsInDatabase - 1);
    }
}
