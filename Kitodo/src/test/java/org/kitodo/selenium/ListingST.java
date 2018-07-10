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
import org.kitodo.selenium.testframework.enums.TabIndex;
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
        Pages.getUsersPage().goTo().switchToTabByIndex(TabIndex.CLIENTS.getIndex());
        int clientsInDatabase = serviceManager.getClientService().getAll().size();
        int clientsDisplayed = Pages.getUsersPage().countListedClients();
        Assert.assertEquals("Displayed wrong number of clients", clientsInDatabase, clientsDisplayed);
    }

    @Test
    public void listProjectsTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int projectsInDatabase = serviceManager.getProjectService().getAll().size();
        int projectsDisplayed = Pages.getProjectsPage().countListedProjects();
        Assert.assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);
    }

    @Test
    public void listTemplatesTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int templatesInDatabase = serviceManager.getTemplateService().getActiveTemplates().size();
        int templatesDisplayed = Pages.getProjectsPage().countListedTemplates();
        Assert.assertEquals("Displayed wrong number of templates", templatesInDatabase, templatesDisplayed);
    }

    @Test
    public void listDocketsTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int docketsTInDatabase = serviceManager.getDocketService().getAll().size();
        int docketsTDisplayed = Pages.getProjectsPage().countListedDockets();
        Assert.assertEquals("Displayed wrong number of dockets", docketsTInDatabase, docketsTDisplayed);
    }

    @Test
    public void listRulesetsTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int rulesetsInDatabase = serviceManager.getRulesetService().getAll().size();
        int rulesetsDisplayed = Pages.getProjectsPage().countListedRulesets();
        Assert.assertEquals("Displayed wrong number of rulesets", rulesetsInDatabase, rulesetsDisplayed);
    }

    @Test
    public void listProcessesTest() throws Exception {
        Pages.getProcessesPage().goTo();
        int processesInDatabase = serviceManager.getProcessService().getActiveProcesses().size();
        int processesDisplayed = Pages.getProcessesPage().countListedProcesses();
        Assert.assertEquals("Displayed wrong number of processes", processesInDatabase, processesDisplayed);
    }

    @Test
    public void listUsersTest() throws Exception {
        int usersInDatabase = serviceManager.getUserService().getAll().size();
        int usersDisplayed = Pages.getUsersPage().goTo().countListedUsers();
        Assert.assertEquals("Displayed wrong number of users", usersInDatabase, usersDisplayed);
    }

    @Test
    public void listUserGroupsTest() throws Exception {
        int userGroupsInDatabase = serviceManager.getUserGroupService().getAll().size();
        int userGroupsDisplayed = Pages.getUsersPage().goTo().countListedUserGroups();
        Assert.assertEquals("Displayed wrong number of user groups", userGroupsInDatabase, userGroupsDisplayed);
    }

    @Test
    public void listLdapGroupsTest() throws Exception {
        int ldapGroupsInDatabase = serviceManager.getLdapGroupService().getAll().size();
        int ldapGroupsDisplayed = Pages.getUsersPage().goTo().switchToTabByIndex(2).countListedLdapGroups();
        Assert.assertEquals("Displayed wrong number of ldap groups!", ldapGroupsInDatabase, ldapGroupsDisplayed);
    }
}
