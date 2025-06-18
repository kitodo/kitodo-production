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
import org.kitodo.SecurityTestUtils;
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
        long batchesInDatabase = ServiceManager.getBatchService().count();
        assertTrue(batchesDisplayed > 0 && batchesInDatabase > 0, "Batch list is empty");
        processesPage.deleteBatch();
        assertTrue(processesPage.countListedBatches() == batchesDisplayed - 1
                && ServiceManager.getBatchService().count() == batchesInDatabase - 1,
            "Removal of batch was not successful!");
    }

    @Test
    public void removeUserTest() throws Exception {
        int usersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = ServiceManager.getUserService().count();
        assertTrue(usersDisplayed > 0 && usersInDatabase > 0, "User list is empty");
        usersPage.deleteRemovableUser();
        assertTrue(usersPage.countListedUsers() == usersDisplayed - 1
                && ServiceManager.getUserService().count() == usersInDatabase - 1,
            "Removal of first user was not successful!");
    }

    @Test
    public void removeRoleTest() throws Exception {
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
        int rolesDisplayed = usersPage.countListedRoles();
        long clientRoles = ServiceManager.getRoleService().count();
        assertTrue(rolesDisplayed > 0 && clientRoles > 0, "Role list or umber of current clients roles is empty");
        usersPage.deleteRemovableRole();
        assertTrue(usersPage.countListedRoles() == rolesDisplayed - 1
                && ServiceManager.getRoleService().count() == clientRoles - 1, "Removal of first role was not successful!");
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void removeClientTest() throws Exception {
        int clientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = ServiceManager.getClientService().count();
        assertTrue(clientsDisplayed > 0 && clientsInDatabase > 0, "Client list is empty");
        usersPage.deleteRemovableClient();
        assertTrue(usersPage.countListedClients() == clientsDisplayed - 1
                && ServiceManager.getClientService().count() == clientsInDatabase - 1,
            "Removal of first client was not successful!");
    }

    @Test
    public void removeDocketTest() throws Exception {
        int docketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = ServiceManager.getDocketService().count();
        assertTrue(docketsDisplayed > 0 && docketsInDatabase > 0, "Docket list is empty");
        projectsPage.deleteDocket();
        assertTrue(projectsPage.countListedDockets() == docketsDisplayed - 1
                && ServiceManager.getDocketService().count() == docketsInDatabase - 1,
            "Removal of first docket was not successful!");
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = ServiceManager.getRulesetService().count();
        assertTrue(rulesetsDisplayed > 0 && rulesetsInDatabase > 0, "Ruleset list is empty");
        projectsPage.deleteRuleset();
        assertTrue(projectsPage.countListedRulesets() == rulesetsDisplayed - 1
                && ServiceManager.getRulesetService().count() == rulesetsInDatabase - 1,
            "Removal of ruleset was not successful!");
    }
}
