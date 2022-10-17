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

import java.util.List;

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

    @Test
    public void checkOrderOfImportConfigurations() throws Exception {
        projectsPage.createNewProcess();
        List<String> importConfigurationNames = importPage.getImportConfigurationsTitles();
        assertEquals("Wrong first import configuration", GBV, importConfigurationNames.get(1));
        assertEquals("Wrong first import configuration", K10PLUS, importConfigurationNames.get(2));
        assertEquals("Wrong first import configuration", KALLIOPE, importConfigurationNames.get(3));
    }
}
