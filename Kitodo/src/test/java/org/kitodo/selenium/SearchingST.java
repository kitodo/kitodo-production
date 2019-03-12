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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.DesktopPage;
import org.kitodo.selenium.testframework.pages.SearchResultPage;

public class SearchingST extends BaseTestSelenium {

    private static DesktopPage desktopPage;
    private static SearchResultPage searchResultPage;


    @BeforeClass
    public static void setup() throws Exception {
        desktopPage = Pages.getDesktopPage();
        searchResultPage = Pages.getSearchResultPage();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

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
        assertEquals("There should be two processes found",2,numberOfResults);

        searchResultPage.searchInSearchField("proc");
        numberOfResults = searchResultPage.getNumberOfResults();
        assertEquals("There should be two processes found",2,numberOfResults);


        searchResultPage.searchInSearchField("möhö");
        numberOfResults = searchResultPage.getNumberOfResults();
        assertEquals("There should be no process found",0,numberOfResults);

    }

    @Test
    public void searchAndFilter() throws Exception {
        desktopPage.searchInSearchField("es");
        int numberOfResults = searchResultPage.getNumberOfResults();
        assertEquals("There should be three processes found",3,numberOfResults);

        String projectsForFilter = searchResultPage.getProjectsForFilter();
        assertTrue("Wrong Project name. Expected: First project",projectsForFilter.contains("First project"));

    }
}
