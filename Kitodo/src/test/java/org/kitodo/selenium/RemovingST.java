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
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertTrue;

public class RemovingST extends BaseTestSelenium {

    private static ServiceManager serviceManager = new ServiceManager();

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
        long batchesInDatabase = serviceManager.getBatchService().countDatabaseRows();
        assertTrue("Batch list is empty", batchesDisplayed > 0 && batchesInDatabase > 0);
        processesPage.deleteBatch();
        assertTrue("Removal of batch was not successful!",
                processesPage.countListedBatches() == batchesDisplayed - 1
                        && serviceManager.getBatchService().countDatabaseRows() == batchesInDatabase - 1);
    }

    @Test
    public void removeUserTest() throws Exception {
        int usersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = serviceManager.getUserService().countDatabaseRows();
        assertTrue("User list is empty", usersDisplayed > 0 && usersInDatabase > 0);
        usersPage.deleteRemovableUser();
        assertTrue("Removal of first user was not successful!",
            usersPage.countListedUsers() == usersDisplayed - 1
                    && serviceManager.getUserService().countDatabaseRows() == usersInDatabase - 1);
    }

    @Test
    public void removeUserGroupTest() throws Exception {
        int userGroupsDisplayed = usersPage.countListedUserGroups();
        long userGroupsInDatabase = serviceManager.getUserGroupService().countDatabaseRows();
        assertTrue("User group list is empty", userGroupsDisplayed > 0 && userGroupsInDatabase > 0);
        usersPage.deleteRemovableUserGroup();
        assertTrue("Removal of first user group was not successful!",
            usersPage.countListedUserGroups() == userGroupsDisplayed - 1
                    && serviceManager.getUserGroupService().countDatabaseRows() == userGroupsInDatabase - 1);
    }

    @Test
    public void removeClientTest() throws Exception {
        int clientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = serviceManager.getClientService().countDatabaseRows();
        assertTrue("Client list is empty", clientsDisplayed > 0 && clientsInDatabase > 0);
        usersPage.deleteRemovableClient();
        assertTrue("Removal of first client was not successful!",
            usersPage.countListedClients() == clientsDisplayed - 1
                    && serviceManager.getClientService().countDatabaseRows() == clientsInDatabase - 1);
    }

    @Test
    public void removeDocketTest() throws Exception {
        int docketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = serviceManager.getDocketService().countDatabaseRows();
        assertTrue("Docket list is empty", docketsDisplayed > 0 && docketsInDatabase > 0);
        projectsPage.deleteDocket();
        assertTrue("Removal of first docket was not successful!",
            projectsPage.countListedDockets() == docketsDisplayed - 1
                    && serviceManager.getDocketService().countDatabaseRows() == docketsInDatabase - 1);
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = serviceManager.getRulesetService().countDatabaseRows();
        assertTrue("Ruleset list is empty", rulesetsDisplayed > 0 && rulesetsInDatabase > 0);
        projectsPage.deleteRuleset();
        assertTrue("Removal of ruleset was not successful!",
            projectsPage.countListedRulesets() == rulesetsDisplayed - 1
                    && serviceManager.getRulesetService().countDatabaseRows() == rulesetsInDatabase - 1);
    }
}
