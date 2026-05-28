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

import java.net.URI;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.CurrentTasksEditPage;
import org.kitodo.selenium.testframework.pages.ProcessEditPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.TasksPage;

/**
 * Checks that URL query parameters are correctly applied to list views.
 */
public class ListingQueryParameterST extends BaseTestSelenium {

    private static ProcessesPage processesPage;
    private static ProcessEditPage processEditPage;
    private static TasksPage tasksPage;
    private static CurrentTasksEditPage currentTaskEditPage;

    @BeforeAll
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        processEditPage = Pages.getProcessEditPage();
        tasksPage = Pages.getTasksPage();
        currentTaskEditPage = Pages.getCurrentTasksEditPage();
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
