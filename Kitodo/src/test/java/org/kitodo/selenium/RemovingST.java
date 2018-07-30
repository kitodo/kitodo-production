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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.services.ServiceManager;

public class RemovingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    private UsersPage usersPage;
    private ProjectsPage projectsPage;
    private ProcessesPage processesPage;

    public void setup() throws Exception {
        usersPage = Pages.getUsersPage();
        projectsPage = Pages.getProjectsPage();
        processesPage = Pages.getProcessesPage();
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
    public void removeProcessTest() throws Exception {
        int numberOfProcessesDisplayed = processesPage.countListedProcesses();
        long processesInDatabase = serviceManager.getProcessService().countDatabaseRows();
        Assert.assertTrue("Process list is empty",
                processesPage.isResultSetNotEmpty(processesPage.getProcessTitles()) && processesInDatabase > 0);
        processesPage.deleteFirstProcess();
        Assert.assertTrue("Removal of first process was not successful!",
                processesPage.countListedProcesses() < numberOfProcessesDisplayed &&
                        serviceManager.getProcessService().countDatabaseRows() < processesInDatabase);
    }

    @Test
    public void removeUserTest() throws Exception {
        int numberOfUsersDisplayed = usersPage.countListedUsers();
        long usersInDatabase = serviceManager.getUserService().countDatabaseRows();
        Assert.assertTrue("User list is empty",
                numberOfUsersDisplayed > 0 && usersInDatabase > 0);
        usersPage.deleteFirstUser();
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
        usersPage.deleteFirstUserGroup();
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
        usersPage.deleteFirstClient();
        Assert.assertTrue("Removal of first client was not successful!",
                usersPage.countListedClients() == numberOfClientsDisplayed - 1
                        && serviceManager.getClientService().countDatabaseRows() == clientsInDatabase - 1);
    }

    @Test
    public void removeLDAPGroupTest() throws Exception {
        int numberOfLDAPGroupsDisplayed = usersPage.countListedLdapGroups();
        long ldapGroupsInDatabase = serviceManager.getLdapGroupService().countDatabaseRows();
        Assert.assertTrue("LDAP group list is empty",
                numberOfLDAPGroupsDisplayed > 0 && ldapGroupsInDatabase > 0 );
        usersPage.deleteFirstLDAPGroup();
        Assert.assertTrue("Removal of first LDAP group was not successful!",
                usersPage.countListedLdapGroups() == numberOfLDAPGroupsDisplayed - 1 &&
                        serviceManager.getLdapGroupService().countDatabaseRows() == ldapGroupsInDatabase - 1);
    }

    @Test
    public void removeProjectTest() throws Exception {
        int numberOfProjectsDisplayed = projectsPage.countListedProjects();
        long projectsInDatabase = serviceManager.getProjectService().countDatabaseRows();
        Assert.assertTrue("Project list is empty",
                numberOfProjectsDisplayed > 0 && projectsInDatabase > 0);
        projectsPage.deleteFirstProject();
        Assert.assertTrue("Removal of first project was not successful!",
                projectsPage.countListedProjects() == numberOfProjectsDisplayed - 1 &&
                        serviceManager.getProjectService().countDatabaseRows() == projectsInDatabase - 1);
    }

    @Test
    public void removeDocketTest() throws Exception {
        int numberOfDocketsDisplayed = projectsPage.countListedDockets();
        long docketsInDatabase = serviceManager.getDocketService().countDatabaseRows();
        Assert.assertTrue("Docket list is empty",
                numberOfDocketsDisplayed > 0 && docketsInDatabase > 0);
        projectsPage.deleteFirstDocket();
        Assert.assertTrue("Removal of first docket was not successful!",
                projectsPage.countListedDockets() == numberOfDocketsDisplayed - 1 &&
                        serviceManager.getDocketService().countDatabaseRows() == docketsInDatabase - 1);
    }

}
