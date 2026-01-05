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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.xebialabs.restito.server.StubServer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessFromTemplatePage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.test.utils.ProcessTestUtils;
import org.kitodo.test.utils.TestConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

@Disabled("Breaks ListingSessionClientST due to incomplete clean-up")
public class ImportingST extends BaseTestSelenium {

    private static StubServer server;
    private static final String TEST_VOLUME = "Test volume";
    private static final String TEST_MULTI_VOLUME_WORK_FILE = "testMultiVolumeWorkMeta.xml";
    private static ProcessFromTemplatePage importPage;
    private static ProjectsPage projectsPage;
    private static ProcessesPage processesPage;
    private static int multiVolumeWorkId = -1;

    @BeforeAll
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
        processesPage = Pages.getProcessesPage();
        importPage = Pages.getProcessFromTemplatePage();
        MockDatabase.insertPlaceholderProcesses(4, 10);
        multiVolumeWorkId = MockDatabase.insertMultiVolumeWork();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.addDefaultChildProcessImportConfigurationToFirstProject();
        MockDatabase.insertTestTemplateForCreatingProcesses();
        server = new StubServer(MockDatabase.PORT).run();
        setupServer();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @AfterAll
    public static void cleanup() throws Exception {
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
        assertEquals(TestConstants.K10PLUS, catalogSelectMenu.getFirstSelectedOption().getAttribute("label"), "Wrong default catalog selected");

        importPage.selectGBV();
        Select searchFieldSelectMenu = new Select(importPage.getSearchFieldMenu());
        assertEquals(TestConstants.PPN, searchFieldSelectMenu.getFirstSelectedOption().getAttribute("label"), "Wrong default search field selected");
    }

    /**
     * Checks whether 'Search' button is properly deactivated until import configuration, search field
     * and search term have been selected/entered.
     * @throws Exception when navigating to 'Create new process' page fails.
     */
    @Test
    public void checkSearchButtonActivatedText() throws Exception {
        projectsPage.createNewProcess();
        assertFalse(importPage.getSearchButton().isEnabled(), "'Search' button should be deactivated until import configuration, search field and "
                + "search term have been selected");
        importPage.enterTestSearchValue("12345");
        await("Wait for 'Search' button to be enabled").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> importPage.getSearchButton().isEnabled());
        assertTrue(importPage.getSearchButton().isEnabled(), "'Search' button should be activated when import configuration, search field and "
                + "search term have been selected");
    }

    /**
     * Test whether correct child process default import configuration is preselected or not.
     * @throws Exception when navigating to processes page or create process page fails
     */
    /*
     * Test exits at statement: processesPage.applyFilter(...)
     * there-in, at statement: headerText.click();
     * with exception:
     * org.openqa.selenium.ElementClickInterceptedException:
     * element click intercepted: Element <h3 id="headerText">...</h3> is not
     * clickable at point (321, 96). Other element would receive the click: <div
     * id="loadingScreen" style="">...</div>
     * It is not clear why, the loading screen is not visible at that moment.
     */
    @Disabled("faulty, randomly fails during CI builds; needs to be fixed")
    @Test
    public void checkDefaultChildProcessImportConfiguration() throws Exception {
        ProcessTestUtils.copyTestMetadataFile(multiVolumeWorkId, TEST_MULTI_VOLUME_WORK_FILE);
        processesPage.goTo();
        processesPage.applyFilter("id:" + multiVolumeWorkId); /* <-- PROBLEM HERE */
        await("Wait for filter to be applied")
                .pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS).ignoreExceptions()
                .untilAsserted(() -> assertEquals(1, processesPage.getProcessTitles().size(), "Wrong number of filtered processes"));
        processesPage.createChildProcess();
        Select templateProcessMenu = new Select(importPage.getTemplateProcessMenu());
        assertEquals(TEST_VOLUME, templateProcessMenu.getFirstSelectedOption().getAttribute("label"), "Wrong default child import configuration selected");
    }

    @Test
    public void checkOrderOfImportConfigurations() throws Exception {
        projectsPage.createNewProcess();
        List<String> importConfigurationNames = importPage.getImportConfigurationsTitles();
        assertEquals(TestConstants.GBV, importConfigurationNames.get(1), "Wrong title of first import configuration");
        assertEquals(TestConstants.K10PLUS, importConfigurationNames.get(2), "Wrong title of second import configuration");
    }

    /**
     * Checks whether checkboxes in group in the metadata table of the "create new process" form are preserved on
     * collapsing the group UI element or not.
     *
     * @throws Exception when opening the "create new process" form fails
     */
    @Test
    public void checkCollapsedCheckboxMetadataIsPreserved() throws Exception {
        projectsPage.createNewProcess("Book template");
        importPage.cancelCatalogSearch();
        importPage.insertTestTitle("Testvorgang");
        importPage.selectCheckBox(0);
        importPage.toggleTreeTable();
        importPage.clickSaveButton();
        await("Waiting to be redirected to processes page after saving process with selected mandatory checkbox "
                + "in collapsed metadata group")
                .pollDelay(500, TimeUnit.MILLISECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> processesPage.isAt());
    }

    /**
     * Checks whether deactivating validation during process creation works and allows the user to save a process
     * with invalid metadata according to the rules defined in the corresponding ruleset.
     *
     * @throws Exception when opening the "create new process" form fails
     */
    @Test
    public void checkOptionalMetadataValidationDuringProcessCreation() throws Exception {
        projectsPage.createNewProcess("Book template");
        importPage.cancelCatalogSearch();
        // check if option to deactivate validation is available
        pollAssertTrue(() -> Browser.getDriver().findElement(By.id("editForm:validate")).isDisplayed());
        importPage.insertTestTitle("Testvorgang_with_invalid_metadata");
        importPage.clickSaveButton();
        // verify that saving fails with metadata validation enabled
        pollAssertTrue(() -> Browser.getDriver().findElement(By.id("editForm:error-messages")).isDisplayed());
        // deactivate metadata validation
        Browser.getDriver().findElement(By.id("editForm:validate")).click();
        importPage.clickSaveButton();
        await("Waiting for redirection to process list after saving process with deactivated metadata validation")
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> processesPage.isAt());
    }

    /**
     * Tests whether import process hierarchies works correctly or not.
     */
    @Disabled("faulty, randomly fails during CI builds; needs to be fixed")
    @Test
    public void checkHierarchyImport() throws Exception {
        projectsPage.createNewProcess();
        Select catalogSelectMenu = new Select(importPage.getCatalogMenu());
        assertEquals(TestConstants.K10PLUS, catalogSelectMenu.getFirstSelectedOption().getAttribute("label"), "Wrong default catalog selected");
        importPage.selectKalliope();
        Select searchFieldSelectMenu = new Select(importPage.getSearchFieldMenu());
        assertEquals(TestConstants.IDENTIFIER, searchFieldSelectMenu.getFirstSelectedOption().getAttribute("label"), "Wrong default search field selected");
        importPage.enterTestSearchValue(TestConstants.KALLIOPE_PARENT_ID);
        importPage.activateChildProcessImport();
        importPage.decreaseImportDepth();
        importPage.getSearchButton().click();
        assertTrue(importPage.isHierarchyPanelVisible(), "Hierarchy panel should be visible");
        importPage.addPpnAndTitle();
        String parentTitle = importPage.getProcessTitle();
        Pages.getProcessFromTemplatePage().save();
        processesPage.applyFilter(parentTitle);
        assertEquals(1, processesPage.countListedProcesses(), "Exactly one imported parent process should be displayed");
        List<String> processIds = processesPage.getProcessIds();
        assertEquals(1, processIds.size(), "Exactly one process ID should be visible");
        int processId = Integer.parseInt(processIds.getFirst());
        processesPage.filterByChildren();
        List<String> childProcessIds = processesPage.getProcessIds();
        assertEquals(3, childProcessIds.size(), "Wrong number of child processes");
        ProcessTestUtils.removeTestProcess(processId);
    }
}
