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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.ExtendedSearchPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.openqa.selenium.WebElement;

public class SearchingST extends BaseTestSelenium {

    private static final String WAIT_FOR_FILTER_IS_APPLIED = "Wait until filter is applied";

    private static DesktopPage desktopPage;
    private static ExtendedSearchPage extendedSearchPage;
    private static ProcessesPage processesPage;
    private static TasksPage tasksPage;

    @BeforeAll
    public static void setup() throws Exception {
        desktopPage = Pages.getDesktopPage();
        extendedSearchPage = Pages.getExtendedSearchPage();
        processesPage = Pages.getProcessesPage();
        tasksPage = Pages.getTasksPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    /**
     * Logout after every test.
     *
     * @throws Exception
     *             if topNavigationElement is not found
     */
    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void searchForProcesses() throws Exception {
        desktopPage.searchInSearchField("process");
        int numberOfResults = processesPage.getProcessIds().size();
        assertEquals(2, numberOfResults, "There should be two processes found");

        processesPage.searchInSearchField("Second");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals(1, processesPage.getProcessIds().size(), "There should be one process found"));

        processesPage.searchInSearchField("möhö");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals("", processesPage.getProcessTitles().getFirst(), "There should be no processes found"));
    }


    @Test
    public void testExtendedSearch() throws Exception {
        processesPage.goTo();
        processesPage.navigateToExtendedSearch();
        SearchingST.extendedSearchPage.searchById("2");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals(1, processesPage.countListedProcesses(), "There should be one processes found"));
        List<String> processTitles = processesPage.getProcessTitles();
        assertEquals("Second process", processTitles.getFirst(), "Wrong process found");

        processesPage.navigateToExtendedSearch();
        processesPage = SearchingST.extendedSearchPage.seachByTaskStatus();
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals(1, processesPage.countListedProcesses(), "There should be one process found"));
        processTitles = processesPage.getProcessTitles();
        assertEquals("Second process", processTitles.getFirst(), "Wrong process found");
    }

    /**
     * Checks that a case insensitive search for process titles works.
     */
    @Test
    public void caseInsensitiveSearchForProcesses() throws Exception {
        desktopPage.searchInSearchField("PrOCeSs");
        assertEquals(2, processesPage.getProcessIds().size(), "Two processes should match case-insensitive search");
    }

    /**
     * Checks that a case insensitive filter for task status works.
     */
    @Test
    @Disabled("Fails frequently although is error-free")
    public void caseInsensitiveFilterTaskStatus() throws Exception {
        processesPage.goTo();
        processesPage.applyFilter("\"stepinwork:pRoGrEsS\"");

        await(WAIT_FOR_FILTER_IS_APPLIED).pollDelay(100, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions().untilAsserted(() -> {
                    List<String> processTitles = processesPage.getProcessTitles();
                    assertEquals(1, processTitles.size(), "Case insensitive filter should match only one process");
                    assertEquals("First process", processTitles.getFirst(),
                        "Case insensitive filter should match \"First process\"");
                });
    }

    /**
     * Checks whether adding and removing filters work on the processes page.
     */
    @Test
    @Disabled("GUI behaves randomly completely differently")
    public void addAndRemoveFilters() throws Exception {
        processesPage.goTo();
        processesPage.applyFilter("\"id:to be removed\"");
        processesPage.applyFilter("\"project:Example Project\"");
        assertEquals(2, processesPage.getParsedFilters().size(), "Number of parsed filters does not match");

        processesPage.removeParsedFilter(0);
        assertEquals(1, processesPage.getParsedFilters().size(),
            "Number of parsed filters does not match after removing filter");
    }

    /**
     * Checks whether suggestions and filtering work on the tasks page.
     */
    @Test
    public void filterTasksTest() throws Exception {
        tasksPage.goTo();

        tasksPage.typeCharactersIntoFilter("i");
        List<WebElement> suggestions = tasksPage.getSuggestions();
        assertEquals(1, suggestions.size(), "Displayed wrong number of suggestions for input \"i\"");
        assertEquals("id:", suggestions.getFirst().getText(), "Displayed wrong suggestion for input \"i\"");

        tasksPage.selectSuggestion(0);
        assertEquals("id:", tasksPage.getFilterInputValue(), "Filter input value is wrong");

        tasksPage.typeCharactersIntoFilter("1");
        tasksPage.submitFilter();
        assertEquals(2, tasksPage.countListedTasks(), "Task list does not match filter");
    }
}
