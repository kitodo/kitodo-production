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
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.ExtendedSearchPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.SearchResultPage;

public class SearchingST extends BaseTestSelenium {

    private static final String WAIT_FOR_FILTER_IS_APPLIED = "Wait until filter is applied";

    private static DesktopPage desktopPage;
    private static SearchResultPage searchResultPage;
    private static ExtendedSearchPage extendedSearchPage;
    private static ProcessesPage processesPage;

    @BeforeClass
    public static void setup() throws Exception {
        desktopPage = Pages.getDesktopPage();
        searchResultPage = Pages.getSearchResultPage();
        extendedSearchPage = Pages.getExtendedSearchPage();
        processesPage = Pages.getProcessesPage();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    /**
     * Logout after every test.
     *
     * @throws Exception
     *             if topNavigationElement is not found
     */
    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void searchForProcesses() throws Exception {
        desktopPage.searchInSearchField("process");
        int numberOfResults = searchResultPage.getNumberOfResults();
        assertEquals("There should be two processes found", 2, numberOfResults);

        searchResultPage.searchInSearchField("Second");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals("There should be two processes found", 2, searchResultPage.getNumberOfResults()));

        searchResultPage.searchInSearchField("möhö");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals("There should be no processes found", 0, searchResultPage.getNumberOfResults()));
    }

    @Test
    public void testExtendedSearch() throws Exception {
        processesPage.goTo();
        processesPage.navigateToExtendedSearch();
        SearchingST.extendedSearchPage.searchById("2");
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertEquals("There should be one processes found", 1, processesPage.countListedProcesses()));
        List<String> processTitles = processesPage.getProcessTitles();
        assertEquals("Wrong process found", "Second process", processTitles.get(0));

        processesPage.navigateToExtendedSearch();
        processesPage = SearchingST.extendedSearchPage.seachByTaskStatus();
        await("Wait for visible search results").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
                () -> assertEquals("There should be one process found", 1, processesPage.countListedProcesses()));
        processTitles = processesPage.getProcessTitles();
        assertEquals("Wrong process found", "Second process", processTitles.get(0));
    }

    /**
     * Checks that a case insensitive search for process titles works.
     */
    @Test
    public void caseInsensitiveSearchForProcesses() throws Exception {
        desktopPage.searchInSearchField("PrOCeSs");
        assertEquals("Two processes should match case-insensitive search", 2, searchResultPage.getNumberOfResults());
    }

    /**
     * Checks that a case insensitive filter for task status works.
     */
    @Test
    public void caseInsensitiveFilterTaskStatus() throws Exception {
        processesPage.goTo();
        processesPage.applyFilter("\"stepinwork:pRoGrEsS\"");

        await(WAIT_FOR_FILTER_IS_APPLIED)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .atMost(10, TimeUnit.SECONDS).ignoreExceptions()
            .untilAsserted(() -> {
                List<String> processTitles = processesPage.getProcessTitles();
                assertEquals("Case insensitive filter should match only one process", 1, processTitles.size());
                assertEquals("Case insensitive filter should match \"First process\"", "First process", 
                    processTitles.get(0));
            });
    }
}
