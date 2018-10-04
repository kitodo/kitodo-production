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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.kitodo.services.ServiceManager;

public class ListingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;
    private static TasksPage tasksPage;
    private static UsersPage usersPage;

    @BeforeClass
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
        tasksPage = Pages.getTasksPage();
        usersPage = Pages.getUsersPage();
    }

    @BeforeClass
    public static void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @Test
    public void securityAccessTest() throws Exception {
        boolean expectedTrue = Pages.getTopNavigation().isShowingAllLinks();
        assertTrue("Top navigation is not showing that current user is admin", expectedTrue);
    }

    @Test
    public void listTasksTest() throws Exception {
        tasksPage.goTo();
        String query = "SELECT t FROM Task AS t INNER JOIN t.users AS u WITH u.id = 1 INNER JOIN t.userGroups AS ug "
        + "INNER JOIN ug.users AS uug WITH u.id = 1 WHERE (t.processingUser = 1 OR u.id = 1 OR uug.id = 1) AND "
        + "(t.processingStatus = 1 OR t.processingStatus = 2) AND t.typeAutomatic = 0";
        int tasksInDatabase = serviceManager.getTaskService().getByQuery(query).size();
        int tasksDisplayed = tasksPage.countListedTasks();
        assertEquals("Displayed wrong number of tasks", tasksInDatabase, tasksDisplayed);

        List<String> detailsTask =  tasksPage.getTaskDetails();
        assertEquals("Displayed wrong number of task's details", 5, detailsTask.size());
        assertEquals("Displayed wrong task's priority", "0", detailsTask.get(0));
        assertEquals("Displayed wrong task's processing begin", "2016-10-25 00:00:00", detailsTask.get(1));
        assertEquals("Displayed wrong task's processing update", "", detailsTask.get(2));
        assertEquals("Displayed wrong task's processing user", "Kowalski, Jan", detailsTask.get(3));
        assertEquals("Displayed wrong task's edit type", "manuell, regulärer Worklflow", detailsTask.get(4));

        tasksPage.applyFilterShowOnlyOpenTasks();

        query = "SELECT t FROM Task AS t INNER JOIN t.users AS u WITH u.id = 1 INNER JOIN t.userGroups AS ug "
                + "INNER JOIN ug.users AS uug WITH u.id = 1 WHERE (t.processingUser = 1 OR u.id = 1 OR uug.id = 1) AND "
                + "t.processingStatus = 1 AND t.typeAutomatic = 0";
        tasksInDatabase = serviceManager.getTaskService().getByQuery(query).size();
        tasksDisplayed = tasksPage.countListedTasks();
        assertEquals("Displayed wrong number of tasks with applied filter", tasksInDatabase, tasksDisplayed);
    }

    @Test
    public void listProjectsTest() throws Exception {
        projectsPage.goTo();
        int projectsInDatabase = serviceManager.getProjectService()
                .getByQuery("FROM Project AS p INNER JOIN p.users AS u WITH u.id = 1").size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        List<String> detailsProject =  projectsPage.getProjectDetails();
        //TODO : check out how exactly columns and rows are calculated
        assertEquals("Displayed wrong number of project's details", 5, detailsProject.size());
        assertEquals("Displayed wrong project's save format", "Mets", detailsProject.get(0));
        assertEquals("Displayed wrong project's DMS export format", "Mets", detailsProject.get(1));
        assertEquals("Displayed wrong project's METS owner", "Test Owner", detailsProject.get(2));
        assertEquals("Displayed wrong project's template", "First template", detailsProject.get(3));

        int templatesInDatabase = serviceManager.getTemplateService().getActiveTemplates().size();
        int templatesDisplayed = projectsPage.countListedTemplates();
        assertEquals("Displayed wrong number of templates", templatesInDatabase, templatesDisplayed);

        List<String> detailsTemplate =  projectsPage.getTemplateDetails();
        //TODO: find way to read this table without exception
        //assertEquals("Displayed wrong number of template's details", 4, detailsTemplate.size());
        //assertEquals("Displayed wrong template's workflow", "", detailsTemplate.get(0));
        //assertEquals("Displayed wrong template's ruleset", "SLUBHH", detailsTemplate.get(1));
        //assertEquals("Displayed wrong template's docket", "second", detailsTemplate.get(2));
        //assertEquals("Displayed wrong template's project", "First project", detailsTemplate.get(2));

        int workflowsInDatabase = serviceManager.getWorkflowService().getAll().size();
        int workflowsDisplayed = projectsPage.countListedWorkflows();
        assertEquals("Displayed wrong number of workflows", workflowsInDatabase, workflowsDisplayed);

        int docketsInDatabase = serviceManager.getDocketService().getAll().size();
        int docketsDisplayed = projectsPage.countListedDockets();
        assertEquals("Displayed wrong number of dockets", docketsInDatabase, docketsDisplayed);

        int rulesetsInDatabase = serviceManager.getRulesetService().getAll().size();
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        assertEquals("Displayed wrong number of rulesets", rulesetsInDatabase, rulesetsDisplayed);
    }

    @Test
    public void listProcessesTest() throws Exception {
        processesPage.goTo();
        int processesInDatabase = serviceManager.getProcessService().getActiveProcesses().size();
        int processesDisplayed = processesPage.countListedProcesses();
        assertEquals("Displayed wrong number of processes", processesInDatabase, processesDisplayed);

        int batchesInDatabase = serviceManager.getBatchService().getAll().size();
        int batchesDisplayed = processesPage.countListedBatches();
        assertEquals("Displayed wrong number of batches", batchesInDatabase, batchesDisplayed);
    }

    @Test
    public void listUsersTest() throws Exception {
        usersPage.goTo();
        int usersInDatabase = serviceManager.getUserService().getAll().size();
        int usersDisplayed = usersPage.countListedUsers();
        assertEquals("Displayed wrong number of users", usersInDatabase, usersDisplayed);

        int userGroupsInDatabase = serviceManager.getUserGroupService().getAll().size();
        int userGroupsDisplayed = usersPage.countListedUserGroups();
        assertEquals("Displayed wrong number of user groups", userGroupsInDatabase, userGroupsDisplayed);

        int clientsInDatabase = serviceManager.getClientService().getAll().size();
        int clientsDisplayed = usersPage.countListedClients();
        assertEquals("Displayed wrong number of clients", clientsInDatabase, clientsDisplayed);

        int ldapGroupsInDatabase = serviceManager.getLdapGroupService().getAll().size();
        int ldapGroupsDisplayed = usersPage.countListedLdapGroups();
        assertEquals("Displayed wrong number of ldap groups!", ldapGroupsInDatabase, ldapGroupsDisplayed);
    }
}
