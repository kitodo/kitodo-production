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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
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

    @BeforeAll
    public static void setup() throws Exception {
        desktopPage = Pages.getDesktopPage();
        processesPage = Pages.getProcessesPage();
        projectsPage = Pages.getProjectsPage();
        tasksPage = Pages.getTasksPage();
        usersPage = Pages.getUsersPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();

        User user = ServiceManager.getUserService().getByLogin("kowal");
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @AfterAll
    public static void cleanSecurityContext() {
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void securityAccessTest() throws Exception {
        boolean expectedTrue = Pages.getTopNavigation().isShowingAllLinks();
        assertTrue(expectedTrue, "Top navigation is not showing that current user is admin");
    }

    @Test
    public void listDesktopTest() throws Exception {
        desktopPage.goTo();

        long processesInDatabase = ServiceManager.getProcessService().countResults(null);
        long processesDisplayed = desktopPage.countListedProcesses();
        assertEquals(processesInDatabase, processesDisplayed, "Displayed wrong number of processes");

        int projectsInDatabase = ServiceManager.getProjectService()
                .getByQuery(
                    "FROM Project AS p INNER JOIN p.users AS u WITH u.id = 1 INNER JOIN p.client AS c WITH c.id = 1")
                .size();
        int projectsDisplayed = desktopPage.countListedProjects();
        assertEquals(projectsInDatabase, projectsDisplayed, "Displayed wrong number of projects");

        String query = "SELECT t FROM Task AS t INNER JOIN t.roles AS r WITH r.id = 1"
                + " INNER JOIN t.process AS p WITH p.id IS NOT NULL WHERE (t.processingUser.id = 1 OR r.id = 1)"
                + " AND (t.processingStatus = 1 OR t.processingStatus = 2) AND t.typeAutomatic = false";

        int tasksInDatabase = ServiceManager.getTaskService().getByQuery(query).size();
        int tasksDisplayed = desktopPage.countListedTasks();
        assertEquals(tasksInDatabase, tasksDisplayed, "Displayed wrong number of tasks");

        int statisticsDisplayed = desktopPage.countListedStatistics();
        assertEquals(9, statisticsDisplayed, "Displayed wrong number of statistics");

        List<String> statistics = desktopPage.getStatistics();

        long countInDatabase = ServiceManager.getTaskService().count();
        long countDisplayed = Long.parseLong(statistics.getFirst());
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for task statistics");

        countInDatabase = ServiceManager.getUserService().count();
        countDisplayed = Long.parseLong(statistics.get(1));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for user statistics");

        countInDatabase = ServiceManager.getProcessService().count();
        countDisplayed = Long.parseLong(statistics.get(2));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for process statistics");

        countInDatabase = ServiceManager.getDocketService().count();
        countDisplayed = Long.parseLong(statistics.get(3));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for docket statistics");

        countInDatabase = ServiceManager.getProjectService().count();
        countDisplayed = Long.parseLong(statistics.get(4));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for project statistics");

        countInDatabase = ServiceManager.getRulesetService().count();
        countDisplayed = Long.parseLong(statistics.get(5));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for ruleset statistics");

        countInDatabase = ServiceManager.getTemplateService().count();
        countDisplayed = Long.parseLong(statistics.get(6));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for template statistics");

        countInDatabase = ServiceManager.getRoleService().count();
        countDisplayed = Long.parseLong(statistics.get(7));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for role statistics");

        countInDatabase = ServiceManager.getWorkflowService().count();
        countDisplayed = Long.parseLong(statistics.get(8));
        assertEquals(countInDatabase, countDisplayed, "Displayed wrong count for workflow statistics");
    }

    @Test
    @Disabled("displaying task's edit type currently not available")
    public void listTasksTest() throws Exception {
        tasksPage.goTo();

        String query = "SELECT t FROM Task AS t INNER JOIN t.roles AS r WITH r.id = 1"
                + " INNER JOIN t.process AS p WITH p.id IS NOT NULL WHERE (t.processingUser.id = 1 OR r.id = 1)"
                + " AND (t.processingStatus = 1 OR t.processingStatus = 2) AND t.typeAutomatic = false";

        int tasksInDatabase = ServiceManager.getTaskService().getByQuery(query).size();
        int tasksDisplayed = tasksPage.countListedTasks();
        assertEquals(tasksInDatabase, tasksDisplayed, "Displayed wrong number of tasks");

        List<String> detailsTask = tasksPage.getTaskDetails();
        assertEquals(5, detailsTask.size(), "Displayed wrong number of task's details");
        assertEquals("false", detailsTask.get(0), "Displayed wrong task's priority");
        assertEquals("2017-01-25 00:00:00", detailsTask.get(1), "Displayed wrong task's processing begin");
        assertEquals("", detailsTask.get(2), "Displayed wrong task's processing update");
        assertEquals("Nowak, Adam", detailsTask.get(3), "Displayed wrong task's processing user");
        assertEquals("manuell, regulärer Workflow", detailsTask.get(4), "Displayed wrong task's edit type");

        tasksPage.applyFilterShowOnlyOpenTasks();

        query = "SELECT t FROM Task AS t INNER JOIN t.roles AS r WITH r.id = 1"
                + " INNER JOIN t.process AS p WITH p.id IS NOT NULL WHERE (t.processingUser.id = 1 OR r.id = 1) AND "
                + "t.processingStatus = 1 AND t.typeAutomatic = false";
        tasksInDatabase = ServiceManager.getTaskService().getByQuery(query).size();
        tasksDisplayed = tasksPage.countListedTasks();
        assertEquals(tasksInDatabase, tasksDisplayed, "Displayed wrong number of tasks with applied filter");
    }

    @Test
    public void listProjectsTest() throws Exception {
        projectsPage.goTo();
        int projectsInDatabase = ServiceManager.getProjectService()
                .getByQuery(
                    "FROM Project AS p INNER JOIN p.users AS u WITH u.id = 1 INNER JOIN p.client AS c WITH c.id = 1")
                .size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals(projectsInDatabase, projectsDisplayed, "Displayed wrong number of projects");

        List<String> detailsProject = projectsPage.getProjectDetails();
        assertEquals(1, detailsProject.size(), "Displayed wrong number of project's details");
        assertEquals("Test Owner", detailsProject.getFirst(), "Displayed wrong project's METS owner");

        List<String> templatesProject = projectsPage.getProjectTemplates();
        assertEquals(2, templatesProject.size(), "Displayed wrong number of project's templates");
        assertEquals("Fourth template", templatesProject.get(1), "Displayed wrong project's template");

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
        assertEquals(workflowsInDatabase, workflowsDisplayed, "Displayed wrong number of workflows");

        int docketsInDatabase = ServiceManager.getDocketService().getAllForSelectedClient().size();
        int docketsDisplayed = projectsPage.countListedDockets();
        assertEquals(docketsInDatabase, docketsDisplayed, "Displayed wrong number of dockets");

        int rulesetsInDatabase = ServiceManager.getRulesetService().getAllForSelectedClient().size();
        int rulesetsDisplayed = projectsPage.countListedRulesets();
        assertEquals(rulesetsInDatabase, rulesetsDisplayed, "Displayed wrong number of rulesets");
    }

    @Test
    public void listProcessesTest() throws Exception {
        processesPage.goTo();
        long processesInDatabase = ServiceManager.getProcessService().countResults(null);
        long processesDisplayed = processesPage.countListedProcesses();
        assertEquals(processesInDatabase, processesDisplayed, "Displayed wrong number of processes");

        int batchesInDatabase = ServiceManager.getBatchService().getAll().size();
        int batchesDisplayed = processesPage.countListedBatches();
        assertEquals(batchesInDatabase, batchesDisplayed, "Displayed wrong number of batches");
    }

    @Test
    public void listUsersTest() throws Exception {
        usersPage.goTo();
        int usersInDatabase = ServiceManager.getUserService().getAll().size();
        int usersDisplayed = usersPage.countListedUsers();
        assertEquals(usersInDatabase, usersDisplayed, "Displayed wrong number of users");

        int rolesInDatabase = ServiceManager.getRoleService().getAll().size();
        int rolesDisplayed = usersPage.countListedRoles();
        assertEquals(rolesInDatabase, rolesDisplayed, "Displayed wrong number of roles");

        int clientsInDatabase = ServiceManager.getClientService().getAll().size();
        int clientsDisplayed = usersPage.countListedClients();
        assertEquals(clientsInDatabase, clientsDisplayed, "Displayed wrong number of clients");

        int ldapGroupsInDatabase = ServiceManager.getLdapGroupService().getAll().size();
        int ldapGroupsDisplayed = usersPage.countListedLdapGroups();
        assertEquals(ldapGroupsInDatabase, ldapGroupsDisplayed, "Displayed wrong number of ldap groups!");
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
        assertEquals(2, projectsPage.getTemplateTitles().size(), "Wrong number of templates before hiding first template");
        TemplateEditPage editTemplatePage = projectsPage.editTemplate();
        editTemplatePage.hideTemplate();
        editTemplatePage.save();
        assertEquals(1, projectsPage.getTemplateTitles().size(), "Wrong number of templates after hiding first template");
        projectsPage.goToTemplateTab();
        projectsPage.toggleHiddenTemplates();
        assertEquals(2, projectsPage.getTemplateTitles().size(), "Wrong number of templates after toggling hidden templates");
    }
}
