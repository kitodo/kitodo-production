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

import org.junit.*;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.services.ServiceManager;

public class RemovingST extends BaseTestSelenium {

    private static ServiceManager serviceManager = new ServiceManager();

    private static UsersPage usersPage;
    private static ProjectsPage projectsPage;

    @BeforeClass
    public static void setup() throws Exception {
        usersPage = Pages.getUsersPage();
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
    public void removeUserTest() throws Exception {
        int numberOfUsersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = serviceManager.getUserService().countDatabaseRows();
        Assert.assertTrue("User list is empty",
                numberOfUsersDisplayed > 0 && usersInDatabase > 0);
        usersPage.deleteRemovableUser();
        Assert.assertTrue("Removal of first user was not successful!",
                usersPage.countListedUsers() == numberOfUsersDisplayed - 1 &&
                        serviceManager.getUserService().countDatabaseRows() == usersInDatabase - 1);
    }

    @Test
    public void removeUsergroupTest() throws Exception {
        int numbeOfUsergroupsDisplayed = usersPage.countListedUserGroups();
        long usergroupsInDatabase = serviceManager.getUserGroupService().countDatabaseRows();
        Assert.assertTrue("User group list is empty",
                numbeOfUsergroupsDisplayed > 0 && usergroupsInDatabase > 0);
        usersPage.deleteRemovableUserGroup();
        Assert.assertTrue("Removal of first user group was not successful!",
                usersPage.countListedUserGroups() == numbeOfUsergroupsDisplayed - 1 &&
                        serviceManager.getUserGroupService().countDatabaseRows() == usergroupsInDatabase - 1);
    }

    @Test
    public void removeClientTest() throws Exception {
        int numberOfClientsDisplayed = usersPage.countListedClients();
        long clientsInDatabase = serviceManager.getClientService().countDatabaseRows();
        Assert.assertTrue("Client list is empty",
                numberOfClientsDisplayed > 0 && clientsInDatabase > 0);
        usersPage.deleteRemovableClient();
        Assert.assertTrue("Removal of first client was not successful!",
                usersPage.countListedClients() == numberOfClientsDisplayed - 1
                        && serviceManager.getClientService().countDatabaseRows() == clientsInDatabase - 1);
    }

    @Test
    public void removeDocketTest() throws Exception {
        int numberOfDocketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = serviceManager.getDocketService().countDatabaseRows();
        Assert.assertTrue("Docket list is empty",
                numberOfDocketsDisplayed > 0 && docketsInDatabase > 0);
        projectsPage.deleteDocket();
        Assert.assertTrue("Removal of first docket was not successful!",
                projectsPage.countListedDockets() == numberOfDocketsDisplayed - 1 &&
                        serviceManager.getDocketService().countDatabaseRows() == docketsInDatabase - 1);
    }

    @Test
    public void removeRulesetTest() throws Exception {
        int numberOfRulesetsDisplayed = projectsPage.countListedRulesets();
        long rulesetsInDatabase = serviceManager.getRulesetService().countDatabaseRows();
        Assert.assertTrue("Ruleset list is empty",
                numberOfRulesetsDisplayed > 0 && rulesetsInDatabase > 0);
        projectsPage.deleteRuleset();
        Assert.assertTrue("Removal of ruleset was not successfull!",
                projectsPage.countListedRulesets() == numberOfRulesetsDisplayed - 1 &&
                        serviceManager.getRulesetService().countDatabaseRows() == rulesetsInDatabase -1);
    }

}
