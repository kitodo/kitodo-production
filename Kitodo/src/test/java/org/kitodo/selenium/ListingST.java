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
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.selenium.testframework.pages.TemplateEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;

public class ListingST extends BaseTestSelenium {

    private static DesktopPage desktopPage;
    private static ProcessesPage processesPage;
    private static ProjectsPage projectsPage;
    private static TasksPage tasksPage;
    private static UsersPage usersPage;

    @BeforeClass
    public static void setup() throws Exception {
        desktopPage = Pages.getDesktopPage();
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
        tasksPage = Pages.getTasksPage();
        usersPage = Pages.getUsersPage();
    }

    @BeforeClass
    public static void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();

        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
    }

    @AfterClass
    public static void cleanSecurityContext() {
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void securityAccessTest() throws Exception {
        boolean expectedTrue = Pages.getTopNavigation().isShowingAllLinks();
        assertTrue("Top navigation is not showing that current user is admin", expectedTrue);
    }

    @Test
    public void listDesktopTest() throws Exception {
        desktopPage.goTo();

        long processesInDatabase = ServiceManager.getProcessService().countResults(null);
        long processesDisplayed = desktopPage.countListedProcesses();
        assertEquals("Displayed wrong number of processes", processesInDatabase, processesDisplayed);

        int projectsInDatabase = ServiceManager.getProjectService()
                .getByQuery(
                    "FROM Project AS p INNER JOIN p.users AS u WITH u.id = 1 INNER JOIN p.client AS c WITH c.id = 1")
                .size();
        int projectsDisplayed = desktopPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        String query = "SELECT t FROM Task AS t INNER JOIN t.roles AS r WITH r.id = 1"
                + " INNER JOIN t.process AS p WITH p.id IS NOT NULL WHERE (t.processingUser = 1 OR r.id = 1)"
                + " AND (t.processingStatus = 1 OR t.processingStatus = 2) AND t.typeAutomatic = 0";

        int tasksInDatabase = ServiceManager.getTaskService().getByQuery(query).size();
        int tasksDisplayed = desktopPage.countListedTasks();
        assertEquals("Displayed wrong number of tasks", tasksInDatabase, tasksDisplayed);

        int statisticsDisplayed = desktopPage.countListedStatistics();
        assertEquals("Displayed wrong number of statistics", 9, statisticsDisplayed);

        List<String> statistics = desktopPage.getStatistics();

        long countInDatabase = ServiceManager.getTaskService().countDatabaseRows();
        long countDisplayed = Long.parseLong(statistics.get(0));
        assertEquals("Displayed wrong count for task statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getUserService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(1));
        assertEquals("Displayed wrong count for user statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getProcessService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(2));
        assertEquals("Displayed wrong count for process statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getDocketService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(3));
        assertEquals("Displayed wrong count for docket statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getProjectService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(4));
        assertEquals("Displayed wrong count for project statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getRulesetService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(5));
        assertEquals("Displayed wrong count for ruleset statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getTemplateService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(6));
        assertEquals("Displayed wrong count for template statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getRoleService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(7));
        assertEquals("Displayed wrong count for role statistics", countInDatabase, countDisplayed);

        countInDatabase = ServiceManager.getWorkflowService().countDatabaseRows();
        countDisplayed = Long.parseLong(statistics.get(8));
        assertEquals("Displayed wrong count for workflow statistics", countInDatabase, countDisplayed);
    }

    @Test
    @Ignore("displaying task's edit type currently not available")
    public void listTasksTest() throws Exception {
        tasksPage.goTo();

        String query = "SELECT t FROM Task AS t INNER JOIN t.roles AS r WITH r.id = 1"
                + " INNER JOIN t.process AS p WITH p.id IS NOT NULL WHERE (t.processingUser = 1 OR r.id = 1)"
                + " AND (t.processingStatus = 1 OR t.processingStatus = 2) AND t.typeAutomatic = 0";

        int tasksInDatabase = ServiceManager.getTaskService().getByQuery(query).size();
        int tasksDisplayed = tasksPage.countListedTasks();
        assertEquals("Displayed wrong number of tasks", tasksInDatabase, tasksDisplayed);

        List<String> detailsTask = tasksPage.getTaskDetails();
        assertEquals("Displayed wrong number of task's details", 5, detailsTask.size());
        assertEquals("Displayed wrong task's priority", "false", detailsTask.get(0));
        assertEquals("Displayed wrong task's processing begin", "2017-01-25 00:00:00", detailsTask.get(1));
        assertEquals("Displayed wrong task's processing update", "", detailsTask.get(2));
        assertEquals("Displayed wrong task's processing user", "Nowak, Adam", detailsTask.get(3));
        assertEquals("Displayed wrong task's edit type", "manuell, regulärer Worklflow", detailsTask.get(4));

        tasksPage.applyFilterShowOnlyOpenTasks();

        query = "SELECT t FROM Task AS t INNER JOIN t.roles AS r WITH r.id = 1"
                + " INNER JOIN t.process AS p WITH p.id IS NOT NULL WHERE (t.processingUser = 1 OR r.id = 1) AND "
                + "t.processingStatus = 1 AND t.typeAutomatic = 0";
        tasksInDatabase = ServiceManager.getTaskService().getByQuery(query).size();
        tasksDisplayed = tasksPage.countListedTasks();
        assertEquals("Displayed wrong number of tasks with applied filter", tasksInDatabase, tasksDisplayed);
    }

    @Test
    public void listProjectsTest() throws Exception {
        projectsPage.goTo();
        int projectsInDatabase = ServiceManager.getProjectService()
                .getByQuery(
                    "FROM Project AS p INNER JOIN p.users AS u WITH u.id = 1 INNER JOIN p.client AS c WITH c.id = 1")
                .size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        List<String> detailsProject = projectsPage.getProjectDetails();
        assertEquals("Displayed wrong number of project's details", 1, detailsProject.size());
        assertEquals("Displayed wrong project's METS owner", "Test Owner", detailsProject.get(0));

        List<String> templatesProject = projectsPage.getProjectTemplates();
        assertEquals("Displayed wrong number of project's templates", 2, templatesProject.size());
        assertEquals("Displayed wrong project's template", "Fourth template", templatesProject.get(1));

        int templatesInDatabase = ServiceManager.getTemplateService().getAll().stream()
                .filter(template -> template.getClient().getId() == 1).collect(Collectors.counting()).intValue();
        int templatesDisplayed = projectsPage.countListedTemplates();

        List<String> detailsTemplate =  projectsPage.getTemplateDetails();
        //TODO: find way to read this table without exception
        //assertEquals("Displayed wrong number of template's details", 4, detailsTemplate.size());
        //assertEquals("Displayed wrong template's workflow", "", detailsTemplate.get(0));
        //assertEquals("Displayed wrong template's ruleset", "SLUBHH", detailsTemplate.get(1));
        //assertEquals("Displayed wrong template's docket", "second", detailsTemplate.get(2));
        //assertEquals("Displayed wrong template's project", "First project", detailsTemplate.get(2));

        int workflowsInDatabase = ServiceManager.getWorkflowService().getAll().stream()
                .filter(workflow -> workflow.getClient().getId() == 1).collect(Collectors.counting()).intValue();
        int workflowsDisplayed = projectsPage.countListedWorkflows();
        assertEquals("Displayed wrong number of workflows", workflowsInDatabase, workflowsDisplayed);

        int docketsInDatabase = ServiceManager.getDocketService().getAllForSelectedClient().size();
        int docketsDisplayed = projectsPage.countListedDockets();
        assertEquals("Displayed wrong number of dockets", docketsInDatabase, docketsDisplayed);

        int rulesetsInDatabase = ServiceManager.getRulesetService().getAllForSelectedClient().size();
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        assertEquals("Displayed wrong number of rulesets", rulesetsInDatabase, rulesetsDisplayed);
    }

    @Test
    public void listProcessesTest() throws Exception {
        processesPage.goTo();
        long processesInDatabase = ServiceManager.getProcessService().countResults(null);
        long processesDisplayed = processesPage.countListedProcesses();
        assertEquals("Displayed wrong number of processes", processesInDatabase, processesDisplayed);

        int batchesInDatabase = ServiceManager.getBatchService().getAll().size();
        int batchesDisplayed = processesPage.countListedBatches();
        assertEquals("Displayed wrong number of batches", batchesInDatabase, batchesDisplayed);
    }

    @Test
    public void listUsersTest() throws Exception {
        usersPage.goTo();
        int usersInDatabase = ServiceManager.getUserService().getAll().size();
        int usersDisplayed = usersPage.countListedUsers();
        assertEquals("Displayed wrong number of users", usersInDatabase, usersDisplayed);

        int rolesInDatabase = ServiceManager.getRoleService().getAll().size();
        int rolesDisplayed = usersPage.countListedRoles();
        assertEquals("Displayed wrong number of roles", rolesInDatabase, rolesDisplayed);

        int clientsInDatabase = ServiceManager.getClientService().getAll().size();
        int clientsDisplayed = usersPage.countListedClients();
        assertEquals("Displayed wrong number of clients", clientsInDatabase, clientsDisplayed);

        int ldapGroupsInDatabase = ServiceManager.getLdapGroupService().getAll().size();
        int ldapGroupsDisplayed = usersPage.countListedLdapGroups();
        assertEquals("Displayed wrong number of ldap groups!", ldapGroupsInDatabase, ldapGroupsDisplayed);
    }

    /**
     * Test number of displayed templates. Assert deactivated templates are hidden by default and only visible
     * when activating the corresponding switch on the projects/templates page.
     *
     * @throws Exception when thread is interrupted or templates cannot be loaded.
     */
    @Test
    public void listTemplatesTest() throws Exception {
        projectsPage.goToTemplateTab();
        assertEquals("Wrong number of templates before hiding first template", 2,
                projectsPage.getTemplateTitles().size());
        TemplateEditPage editTemplatePage = projectsPage.editTemplate();
        editTemplatePage.hideTemplate();
        editTemplatePage.save();
        assertEquals("Wrong number of templates after hiding first template", 1,
                projectsPage.getTemplateTitles().size());
        projectsPage.goToTemplateTab();
        projectsPage.toggleHiddenTemplates();
        assertEquals("Wrong number of templates after toggling hidden templates", 2,
                projectsPage.getTemplateTitles().size());
    }
}
