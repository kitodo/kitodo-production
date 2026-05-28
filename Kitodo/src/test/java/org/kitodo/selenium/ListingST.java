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

import java.net.URI;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
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
import org.kitodo.selenium.testframework.pages.CurrentTasksEditPage;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.ProcessEditPage;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.selenium.testframework.pages.TemplateEditPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ListingST extends BaseTestSelenium {

    private static DesktopPage desktopPage;
    private static ProcessesPage processesPage;
    private static ProcessEditPage processEditPage;
    private static ProjectsPage projectsPage;
    private static TasksPage tasksPage;
    private static CurrentTasksEditPage currentTaskEditPage;
    private static UsersPage usersPage;

    @BeforeAll
    public static void setup() throws Exception {
        desktopPage = Pages.getDesktopPage();
        processesPage = Pages.getProcessesPage();
        processEditPage = Pages.getProcessEditPage();
        projectsPage = Pages.getProjectsPage();
        tasksPage = Pages.getTasksPage();
        currentTaskEditPage = Pages.getCurrentTasksEditPage();
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

        projectsPage.goToTemplateTab();
        pollAssertTrue(() -> Browser.getDriver().findElement(By.id("templateTab")).isDisplayed());

        List<String> detailsTemplate =  projectsPage.getTemplateDetails();
        assertEquals(4, detailsTemplate.size(), "Displayed wrong number of template's details");
        assertEquals("second", detailsTemplate.get(0), "Displayed wrong template's docket");
        assertEquals("SUBHH", detailsTemplate.get(1), "Displayed wrong template's ruleset");
        assertEquals("", detailsTemplate.get(2), "Displayed wrong template's workflow");
        assertEquals("First project", detailsTemplate.get(3), "Displayed wrong template's project");

    int workflowsInDatabase = (int) ServiceManager.getWorkflowService().getAll().stream()
        .filter(workflow -> workflow.getClient().getId() == 1)
        .count();
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
    public void listProcessesWithInactiveProjectTest() throws Exception {
        projectsPage.goTo();
        ProjectEditPage projectEditPage = projectsPage.editProject();
        projectEditPage.toggleProjectActiveCheckbox();
        projectEditPage.save();

        processesPage.goTo();
        // expect "1" instead of "0" because "No records found" message of empty table also takes up one row
        assertEquals(1, processesPage.countListedProcesses(), "Processes of inactive projects should be hidden");

        projectsPage.goTo();
        projectsPage.editProject();
        projectEditPage.toggleProjectActiveCheckbox();
        projectEditPage.save();

        processesPage.goTo();
        long processesInDatabase = ServiceManager.getProcessService().countResults(null);
        assertEquals(processesInDatabase, processesPage.countListedProcesses(),
                "Processes should be visible again after project reactivation");
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

    /**
     * Verify that all details are shown on the 'current task' page.
     *
     * @throws Exception when thread is interrupted or tasks cannot be loaded.
     */
    @Test
    public void listCurrentTaskDetailsTest() throws Exception {
        tasksPage.goTo().takeOpenTask("Open", "First process");
        pollAssertTrue(() -> Browser.getDriver().findElement(By.id("tasksTabView")).isDisplayed());

        // first check table headers
        List<WebElement> taskDetailHeaders = Browser.getDriver().findElements(By.cssSelector("#tasksTabView\\:taskDetails_head th"));
        assertEquals(6, taskDetailHeaders.size(), "Wrong number of task details headers");
        assertEquals("Titel", taskDetailHeaders.get(0).getText(), "Wrong first task details header");
        assertEquals("Vorgangstitel", taskDetailHeaders.get(1).getText(), "Wrong second task details header");
        assertEquals("Vorgangs-ID", taskDetailHeaders.get(2).getText(), "Wrong third task details header");
        assertEquals("Reihenfolge", taskDetailHeaders.get(3).getText(), "Wrong fourth task details header");
        assertEquals("Korrektur", taskDetailHeaders.get(4).getText(), "Wrong fifth task details header");
        assertEquals("Status", taskDetailHeaders.get(5).getText(), "Wrong sixth task details header");

        // then check table contents
        List<WebElement> taskDetails = Browser.getDriver().findElements(By.cssSelector("#tasksTabView\\:taskDetails td[role='gridcell']"));
        assertEquals(6, taskDetails.size(), "Wrong number of task details");
        assertEquals("Open", taskDetails.get(0).getText(), "Wrong task title");
        assertEquals("First process", taskDetails.get(1).getText(), "Wrong process title");
        assertEquals("1", taskDetails.get(2).getText(), "Wrong process ID");
        assertEquals("4", taskDetails.get(3).getText(), "Wrong task order");
        assertEquals("", taskDetails.get(4).getText(), "Wrong task correction status");
        assertEquals("In Bearbeitung", taskDetails.get(5).getText(), "Wrong task status");
    }

    /**
     * Check that query URL parameters have an impact on the process list and apply the correct filter and sorting.
     */
    @Test
    public void listProcessesFromUrlParametersTest() throws Exception {
        processesPage.goTo();
        URI processesURI = new URI(Browser.getDriver().getCurrentUrl());
        
        // check that two processes are listed in descending order by default
        assertEquals(List.of("2", "1"), processesPage.getProcessIds());

        // check that sorting by title in ascending order works
        URIBuilder sortByTitleBuilder = new URIBuilder(processesURI);
        sortByTitleBuilder.addParameter("filter", "");
        sortByTitleBuilder.addParameter("sortField", "title");
        sortByTitleBuilder.addParameter("sortOrder", "asc");
        Browser.getDriver().get(sortByTitleBuilder.build().toString());
        pollAssertTrue(() -> List.of("1", "2").equals(processesPage.getProcessIds()));

        // check that filtering by id works
        URIBuilder filterByIdBuilder = new URIBuilder(processesURI);
        filterByIdBuilder.addParameter("filter", "id:1");
        Browser.getDriver().get(filterByIdBuilder.build().toString());
        pollAssertTrue(() -> List.of("1").equals(processesPage.getProcessIds()));

        // check that closed processes can be shown and sorting by status column works
        URIBuilder closedProcessesBuilder = new URIBuilder(processesURI);
        closedProcessesBuilder.addParameter("filter", "");
        closedProcessesBuilder.addParameter("showClosedProcesses", "true");
        closedProcessesBuilder.addParameter("sortField", "progressCombined");
        closedProcessesBuilder.addParameter("sortOrder", "desc");
        Browser.getDriver().get(closedProcessesBuilder.build().toString());
        pollAssertTrue(() -> List.of("2", "1", "3").equals(processesPage.getProcessIds()));

        // check that navigating to a process and returning to the list view preserves list settings
        processesPage.editProcess();
        pollAssertTrue(() -> processEditPage.isAt());
        processEditPage.cancel();
        pollAssertTrue(() -> List.of("2", "1", "3").equals(processesPage.getProcessIds()));
    }

    /**
     * Check that query URL parameters have an impact on the task list and apply correct filter and sorting.
     */
    @Test
    public void listTasksFromUrlParametersTest() throws Exception {
        tasksPage.goTo();
        URI tasksURI = new URI(Browser.getDriver().getCurrentUrl());

        // check that four tasks are listed by default
        assertEquals("Next Open", tasksPage.getFirstRowTaskTitle());
        assertEquals(4, tasksPage.countListedTasks());

        // check that sorting by title in descending order works
        URIBuilder sortByTitleBuilder = new URIBuilder(tasksURI);
        sortByTitleBuilder.addParameter("filter", "");
        sortByTitleBuilder.addParameter("sortField", "title");
        sortByTitleBuilder.addParameter("sortOrder", "desc");
        Browser.getDriver().get(sortByTitleBuilder.build().toString());
        pollAssertTrue(() -> "Progress".equals(tasksPage.getFirstRowTaskTitle()));
        assertEquals(4, tasksPage.countListedTasks());

        // check that filtering by process name works
        URIBuilder processFilterBuilder = new URIBuilder(tasksURI);
        processFilterBuilder.addParameter("filter", "process:First process");
        Browser.getDriver().get(processFilterBuilder.build().toString());
        pollAssertTrue(() -> "Open".equals(tasksPage.getFirstRowTaskTitle()));
        assertEquals(2, tasksPage.countListedTasks());

        // check that hiding tasks of other users work
        URIBuilder taskFilterBuilder = new URIBuilder(tasksURI);
        taskFilterBuilder.addParameter("filter", "");
        taskFilterBuilder.addParameter("taskFilter", "");
        Browser.getDriver().get(taskFilterBuilder.build().toString());
        pollAssertTrue(() -> "Processed and Some".equals(tasksPage.getFirstRowTaskTitle()));
        assertEquals(1, tasksPage.countListedTasks());

        // check that showing all tasks independent of status works
        URIBuilder taskStatusBuilder = new URIBuilder(tasksURI);
        taskStatusBuilder.addParameter("filter", "");
        taskStatusBuilder.addParameter("taskStatus", "");
        Browser.getDriver().get(taskStatusBuilder.build().toString());
        pollAssertTrue(() -> "Additional".equals(tasksPage.getFirstRowTaskTitle()));
        assertEquals(8, tasksPage.countListedTasks());

        // check that navigating to a task and returning to the list view preserves list settings
        tasksPage.takeOpenTask("Open", "First process");
        pollAssertTrue(() -> currentTaskEditPage.isAt());
        currentTaskEditPage.releaseTask();
        pollAssertTrue(() -> "Additional".equals(tasksPage.getFirstRowTaskTitle()));
        assertEquals(8, tasksPage.countListedTasks());
    }
}
