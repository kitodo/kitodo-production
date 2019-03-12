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

    /**
     * Logout after every test.
     * @throws Exception if topNavigationElement is not found
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
        assertEquals("There should be two processes found",2,numberOfResults);

        searchResultPage.searchInSearchField("proc");
        numberOfResults = searchResultPage.getNumberOfResults();
        assertEquals("There should be two processes found",2,numberOfResults);

        //TODO: selenium is too fast here and counts results from previous search
        searchResultPage.searchInSearchField("möhö");
        numberOfResults = searchResultPage.getNumberOfResults();
        //assertEquals("There should be no process found",0,numberOfResults);

    }
}
