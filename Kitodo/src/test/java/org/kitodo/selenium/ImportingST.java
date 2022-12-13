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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessFromTemplatePage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.openqa.selenium.support.ui.Select;

public class ImportingST extends BaseTestSelenium {

    private static final String PPN = "PPN";
    private static final String GBV = "GBV";
    private static final String K10PLUS = "K10Plus";
    private static final String KALLIOPE = "Kalliope";
    private static ProcessFromTemplatePage importPage;
    private static ProjectsPage projectsPage;

    @BeforeClass
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
        importPage = Pages.getProcessFromTemplatePage();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
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
    public void checkDefaultValuesTest() throws Exception {
        projectsPage.createNewProcess();
        Select catalogSelectMenu = new Select(importPage.getCatalogMenu());
        assertEquals("Wrong default catalog selected", K10PLUS,
                catalogSelectMenu.getFirstSelectedOption().getAttribute("label"));

        importPage.selectGBV();
        Select searchFieldSelectMenu = new Select(importPage.getSearchFieldMenu());
        assertEquals("Wrong default search field selected", PPN,
                searchFieldSelectMenu.getFirstSelectedOption().getAttribute("label"));
    }

    /**
     * Checks whether 'Search' button is properly deactivated until import configuration, search field
     * and search term have been selected/entered.
     * @throws Exception when navigating to 'Create new process' page fails.
     */
    @Test
    public void checkSearchButtonActivatedText() throws Exception {
        projectsPage.createNewProcess();
        assertFalse("'Search' button should be deactivated until import configuration, search field and " +
                "search term have been selected", importPage.getSearchButton().isEnabled());
        importPage.enterTestSearchValue();
        await("Wait for 'Search' button to be enabled").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> importPage.getSearchButton().isEnabled());
        assertTrue("'Search' button should be activated when import configuration, search field and " +
                "search term have been selected", importPage.getSearchButton().isEnabled());
    }

    @Test
    public void checkOrderOfImportConfigurations() throws Exception {
        projectsPage.createNewProcess();
        List<String> importConfigurationNames = importPage.getImportConfigurationsTitles();
        assertEquals("Wrong first import configuration title", GBV, importConfigurationNames.get(1));
        assertEquals("Wrong first import configuration title", K10PLUS, importConfigurationNames.get(2));
        assertEquals("Wrong first import configuration title", KALLIOPE, importConfigurationNames.get(3));
    }
}
