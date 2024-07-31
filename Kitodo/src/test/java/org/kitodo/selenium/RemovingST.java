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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
    public static void setup() throws Exception {
        usersPage = Pages.getUsersPage();
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    @Test
    public void removeBatchTest() throws Exception {
        int batchesDisplayed = processesPage.countListedBatches();
        long batchesInDatabase = ServiceManager.getBatchService().countDatabaseRows();
        assertTrue(batchesDisplayed > 0 && batchesInDatabase > 0, "Batch list is empty");
        processesPage.deleteBatch();
        assertTrue(processesPage.countListedBatches() == batchesDisplayed - 1
                && ServiceManager.getBatchService().countDatabaseRows() == batchesInDatabase - 1, "Removal of batch was not successful!");
    }

    @Test
    public void removeUserTest() throws Exception {
        int usersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = ServiceManager.getUserService().countDatabaseRows();
        assertTrue(usersDisplayed > 0 && usersInDatabase > 0, "User list is empty");
        usersPage.deleteRemovableUser();
        assertTrue(usersPage.countListedUsers() == usersDisplayed - 1
                && ServiceManager.getUserService().countDatabaseRows() == usersInDatabase - 1, "Removal of first user was not successful!");
    }

    @Test
    public void removeRoleTest() throws Exception {
        int rolesDisplayed = usersPage.countListedRoles();
        long rolesInDatabase = ServiceManager.getRoleService().countDatabaseRows();
        assertTrue(rolesDisplayed > 0 && rolesInDatabase > 0, "Role list is empty");
        usersPage.deleteRemovableRole();
        assertTrue(usersPage.countListedRoles() == rolesDisplayed - 1
                && ServiceManager.getRoleService().countDatabaseRows() == rolesInDatabase - 1, "Removal of first role was not successful!");
    }

    @Test
    public void removeClientTest() throws Exception {
        int clientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = ServiceManager.getClientService().countDatabaseRows();
        assertTrue(clientsDisplayed > 0 && clientsInDatabase > 0, "Client list is empty");
        usersPage.deleteRemovableClient();
        assertTrue(usersPage.countListedClients() == clientsDisplayed - 1
                && ServiceManager.getClientService().countDatabaseRows() == clientsInDatabase - 1, "Removal of first client was not successful!");
    }

    @Test
    public void removeDocketTest() throws Exception {
        int docketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = ServiceManager.getDocketService().countDatabaseRows();
        assertTrue(docketsDisplayed > 0 && docketsInDatabase > 0, "Docket list is empty");
        projectsPage.deleteDocket();
        assertTrue(projectsPage.countListedDockets() == docketsDisplayed - 1
                && ServiceManager.getDocketService().countDatabaseRows() == docketsInDatabase - 1, "Removal of first docket was not successful!");
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = ServiceManager.getRulesetService().countDatabaseRows();
        assertTrue(rulesetsDisplayed > 0 && rulesetsInDatabase > 0, "Ruleset list is empty");
        projectsPage.deleteRuleset();
        assertTrue(projectsPage.countListedRulesets() == rulesetsDisplayed - 1
                && ServiceManager.getRulesetService().countDatabaseRows() == rulesetsInDatabase - 1, "Removal of ruleset was not successful!");
    }
}
