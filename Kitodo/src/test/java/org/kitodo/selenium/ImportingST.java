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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.xebialabs.restito.server.StubServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessFromTemplatePage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.test.utils.ProcessTestUtils;
import org.kitodo.test.utils.TestConstants;
import org.openqa.selenium.support.ui.Select;

public class ImportingST extends BaseTestSelenium {

    private static StubServer server;
    private static final String TEST_VOLUME = "Test volume";
    private static final String TEST_MULTI_VOLUME_WORK_FILE = "testMultiVolumeWorkMeta.xml";
    private static ProcessFromTemplatePage importPage;
    private static ProjectsPage projectsPage;
    private static ProcessesPage processesPage;
    private static int multiVolumeWorkId = -1;

    @BeforeClass
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
        processesPage = Pages.getProcessesPage();
        importPage = Pages.getProcessFromTemplatePage();
        MockDatabase.insertPlaceholderProcesses(4, 10);
        multiVolumeWorkId = MockDatabase.insertMultiVolumeWork();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.addDefaultChildProcessImportConfigurationToFirstProject();
        server = new StubServer(MockDatabase.PORT).run();
        setupServer();
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

    @AfterClass
    public static void cleanup() throws DAOException, DataException, IOException {
        ProcessService.deleteProcess(multiVolumeWorkId);
        server.stop();
    }

    private static void setupServer() throws IOException {
        // endpoint for retrieving number of child records of process with given ID
        MockDatabase.addRestEndPointForSru(server, TestConstants.EAD_PARENT_ID + "=" + TestConstants.KALLIOPE_PARENT_ID,
                TestConstants.NUMBER_OF_CHILD_RECORDS_PATH, TestConstants.MODS, 0);
        // endpoint for retrieving parent process with given ID
        MockDatabase.addRestEndPointForSru(server, TestConstants.EAD_ID + "=" +  TestConstants.KALLIOPE_PARENT_ID,
                TestConstants.PARENT_RECORD_PATH, TestConstants.MODS, 1);
        // endpoint for retrieving child records of process with given ID
        MockDatabase.addRestEndPointForSru(server, TestConstants.EAD_PARENT_ID + "=" + TestConstants.KALLIOPE_PARENT_ID,
                TestConstants.MULTIPLE_CHILD_RECORDS_PATH, TestConstants.MODS, 1, 3);
    }

    @Test
    public void checkDefaultValuesTest() throws Exception {
        projectsPage.createNewProcess();
        Select catalogSelectMenu = new Select(importPage.getCatalogMenu());
        assertEquals("Wrong default catalog selected", TestConstants.K10PLUS,
                catalogSelectMenu.getFirstSelectedOption().getAttribute("label"));

        importPage.selectGBV();
        Select searchFieldSelectMenu = new Select(importPage.getSearchFieldMenu());
        assertEquals("Wrong default search field selected", TestConstants.PPN,
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
        assertFalse("'Search' button should be deactivated until import configuration, search field and "
                + "search term have been selected", importPage.getSearchButton().isEnabled());
        importPage.enterTestSearchValue("12345");
        await("Wait for 'Search' button to be enabled").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> importPage.getSearchButton().isEnabled());
        assertTrue("'Search' button should be activated when import configuration, search field and "
                + "search term have been selected", importPage.getSearchButton().isEnabled());
    }

    /**
     * Test whether correct child process default import configuration is preselected or not.
     * @throws Exception when navigating to processes page or create process page fails
     */
    @Test
    public void checkDefaultChildProcessImportConfiguration() throws Exception {
        ProcessTestUtils.copyTestMetadataFile(multiVolumeWorkId, TEST_MULTI_VOLUME_WORK_FILE);
        processesPage.goTo();
        processesPage.applyFilter("id:" + multiVolumeWorkId);
        await("Wait for filter to be applied")
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS).ignoreExceptions()
                .untilAsserted(() -> assertEquals("Wrong number of filtered processes", 1,
                        processesPage.getProcessTitles().size()));
        processesPage.createChildProcess();
        Select templateProcessMenu = new Select(importPage.getTemplateProcessMenu());
        assertEquals("Wrong default child import configuration selected", TEST_VOLUME,
                templateProcessMenu.getFirstSelectedOption().getAttribute("label"));
    }

    @Test
    public void checkOrderOfImportConfigurations() throws Exception {
        projectsPage.createNewProcess();
        List<String> importConfigurationNames = importPage.getImportConfigurationsTitles();
        assertEquals("Wrong title of first import configuration", TestConstants.GBV, importConfigurationNames.get(1));
        assertEquals("Wrong title of second import configuration", TestConstants.K10PLUS, importConfigurationNames.get(2));
        assertEquals("Wrong title of third import configuration", TestConstants.KALLIOPE, importConfigurationNames.get(3));
    }

    /**
     * Tests whether import process hierarchies works correctly or not.
     */
    @Test
    public void checkHierarchyImport() throws Exception {
        projectsPage.createNewProcess();
        Select catalogSelectMenu = new Select(importPage.getCatalogMenu());
        assertEquals("Wrong default catalog selected", TestConstants.K10PLUS,
                catalogSelectMenu.getFirstSelectedOption().getAttribute("label"));
        importPage.selectKalliope();
        Select searchFieldSelectMenu = new Select(importPage.getSearchFieldMenu());
        assertEquals("Wrong default search field selected", TestConstants.IDENTIFIER,
                searchFieldSelectMenu.getFirstSelectedOption().getAttribute("label"));
        importPage.enterTestSearchValue(TestConstants.KALLIOPE_PARENT_ID);
        importPage.activateChildProcessImport();
        importPage.decreaseImportDepth();
        importPage.getSearchButton().click();
        assertTrue("Hierarchy panel should be visible", importPage.isHierarchyPanelVisible());
        String parentTitle = importPage.getProcessTitle();
        Pages.getProcessFromTemplatePage().save();
        processesPage.applyFilter(parentTitle);
        assertEquals("Exactly one imported parent process should be displayed", 1,
                processesPage.countListedProcesses());
        List<String> processIds = processesPage.getProcessIds();
        assertEquals("Exactly one process ID should be visible", 1, processIds.size());
        int processId = Integer.parseInt(processIds.get(0));
        processesPage.filterByChildren();
        List<String> childProcessIds = processesPage.getProcessIds();
        assertEquals("Wrong number of child processes", 3, childProcessIds.size());
        ProcessTestUtils.removeTestProcess(processId);
    }
}
