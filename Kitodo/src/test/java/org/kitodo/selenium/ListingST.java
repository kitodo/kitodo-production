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
    public void listTasksTest() throws Exception {
        Pages.getTasksPage().goTo();
        String query = "SELECT t FROM Task AS t INNER JOIN t.users AS u WITH u.id = 1 INNER JOIN t.userGroups AS ug "
        + "INNER JOIN ug.users AS uug WITH u.id = 1 WHERE (t.processingUser = 1 OR u.id = 1 OR uug.id = 1) AND "
        + "(t.processingStatus = 1 OR t.processingStatus = 2) AND t.typeAutomatic = 0";
        int tasksInDatabase = serviceManager.getTaskService().getByQuery(query).size();
        int tasksDisplayed = Pages.getTasksPage().countListedTasks();
        Assert.assertEquals("Displayed wrong number of tasks", tasksInDatabase, tasksDisplayed);
    }

    @Test
    public void listProjectsTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int projectsInDatabase = serviceManager.getProjectService().getAll().size();
        int projectsDisplayed = Pages.getProjectsPage().countListedProjects();
        Assert.assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        int templatesInDatabase = serviceManager.getTemplateService().getActiveTemplates().size();
        int templatesDisplayed = Pages.getProjectsPage().countListedTemplates();
        Assert.assertEquals("Displayed wrong number of templates", templatesInDatabase, templatesDisplayed);

        int workflowsInDatabase = serviceManager.getWorkflowService().getAll().size();
        int workflowsDisplayed = Pages.getProjectsPage().countListedWorkflows();
        Assert.assertEquals("Displayed wrong number of workflows", workflowsInDatabase, workflowsDisplayed);

        int docketsInDatabase = serviceManager.getDocketService().getAll().size();
        int docketsDisplayed = Pages.getProjectsPage().countListedDockets();
        Assert.assertEquals("Displayed wrong number of dockets", docketsInDatabase, docketsDisplayed);

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
        Pages.getUsersPage().goTo();
        int usersInDatabase = serviceManager.getUserService().getAll().size();
        int usersDisplayed = Pages.getUsersPage().countListedUsers();
        Assert.assertEquals("Displayed wrong number of users", usersInDatabase, usersDisplayed);

        int userGroupsInDatabase = serviceManager.getUserGroupService().getAll().size();
        int userGroupsDisplayed = Pages.getUsersPage().countListedUserGroups();
        Assert.assertEquals("Displayed wrong number of user groups", userGroupsInDatabase, userGroupsDisplayed);

        int clientsInDatabase = serviceManager.getClientService().getAll().size();
        int clientsDisplayed = Pages.getUsersPage().countListedClients();
        Assert.assertEquals("Displayed wrong number of clients", clientsInDatabase, clientsDisplayed);

        int ldapGroupsInDatabase = serviceManager.getLdapGroupService().getAll().size();
        int ldapGroupsDisplayed = Pages.getUsersPage().countListedLdapGroups();
        Assert.assertEquals("Displayed wrong number of ldap groups!", ldapGroupsInDatabase, ldapGroupsDisplayed);
    }
}
