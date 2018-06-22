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

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.services.ServiceManager;

public class ListingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void securityAccessTest() throws Exception {
        boolean expectedTrue = Pages.getTopNavigation().isShowingAllLinks();
        Assert.assertTrue("Top navigation is not showing that current user is admin", expectedTrue);
    }

    @Test
    public void listClientsTest() throws Exception {
        Pages.getClientsPage().goTo();
        int numberOfClientsInDatabase = serviceManager.getClientService().getAll().size();
        int numberOfClientsDisplayed = Pages.getClientsPage().countListedClients();
        Assert.assertEquals("Displayed wrong number of clients", numberOfClientsInDatabase, numberOfClientsDisplayed);
    }

    @Test
    public void listProjectsTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int numberOfProjectsInDatabase = serviceManager.getProjectService().getAll().size();
        int numberOfProjectsDisplayed = Pages.getProjectsPage().countListedProjects();
        Assert.assertEquals("Displayed wrong number of projects", numberOfProjectsInDatabase,
            numberOfProjectsDisplayed);
    }

    @Test
    public void listTemplatesTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int numberOfTemplatesInDatabase = serviceManager.getTemplateService().getActiveTemplates().size();
        int numberOfTemplatesDisplayed = Pages.getProjectsPage().countListedTemplates();
        Assert.assertEquals("Displayed wrong number of templates", numberOfTemplatesInDatabase,
            numberOfTemplatesDisplayed);
    }

    @Test
    public void listProcessesTest() throws Exception {
        Pages.getProcessesPage().goTo();
        int numberOfProcessesInDatabase = serviceManager.getProcessService().getActiveProcesses().size();
        int numberOfProcessesDisplayed = Pages.getProcessesPage().countListedProcesses();
        Assert.assertEquals("Displayed wrong number of processes", numberOfProcessesInDatabase,
            numberOfProcessesDisplayed);
    }

    @Test
    public void listUsersTest() throws Exception {
        int numberOfUsersInDatabase = serviceManager.getUserService().getAll().size();
        int numberOfUsersDisplayed = Pages.getUsersPage().goTo().countListedUsers();
        Assert.assertEquals("Displayed wrong number of users", numberOfUsersInDatabase, numberOfUsersDisplayed);
    }

    @Test
    public void listUserGroupsTest() throws Exception {
        int numberOfUserGroupsInDatabase = serviceManager.getUserGroupService().getAll().size();
        int numberOfUserGroupsDisplayed = Pages.getUsersPage().goTo().countListedUserGroups();
        Assert.assertEquals("Displayed wrong number of user groups", numberOfUserGroupsInDatabase,
            numberOfUserGroupsDisplayed);
    }

    @Test
    public void listLdapGroupsTest() throws Exception {
        int numberOfLdapGroupsInDatabase = serviceManager.getLdapGroupService().getAll().size();
        int numberOfLdapGroupsDisplayed = Pages.getUsersPage().goTo().switchToTabByIndex(2).countListedLdapGroups();
        Assert.assertEquals("Displayed wrong number of ldap groups!", numberOfLdapGroupsInDatabase,
            numberOfLdapGroupsDisplayed);
    }
}
