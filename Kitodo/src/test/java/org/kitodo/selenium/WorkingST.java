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
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.CurrentTasksEditPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.TasksPage;
import org.kitodo.production.services.ServiceManager;

@Ignore
public class WorkingST extends BaseTestSelenium {

    private static CurrentTasksEditPage currentTasksEditPage;
    private static ProcessesPage processesPage;
    private static TasksPage tasksPage;

    @BeforeClass
    public static void setup() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();

        currentTasksEditPage = Pages.getCurrentTasksEditPage();
        processesPage = Pages.getProcessesPage();
        tasksPage = Pages.getTasksPage();
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

    @Ignore
    @Test
    public void takeOpenTaskAndGiveItBackTest() throws Exception {
        Task task = ServiceManager.getTaskService().getById(9);
        assertEquals("Task cannot be taken by user!", TaskStatus.OPEN, task.getProcessingStatus());

        tasksPage.goTo().takeOpenTask("Open", "First process");
        assertTrue("Redirection after click take task was not successful", currentTasksEditPage.isAt());

        task = ServiceManager.getTaskService().getById(9);
        assertEquals("Task was not taken by user!", TaskStatus.INWORK, task.getProcessingStatus());

        currentTasksEditPage.releaseTask();
        assertTrue("Redirection after click release task was not successful", tasksPage.isAt());

        task = ServiceManager.getTaskService().getById(9);
        assertEquals("Task was not released by user!", TaskStatus.OPEN, task.getProcessingStatus());
    }

    @Ignore
    @Test
    public void editOwnedTaskTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        Task task = ServiceManager.getTaskService().getById(12);
        tasksPage.goTo().editOwnedTask(task.getTitle(), task.getProcess().getTitle());
        assertTrue("Redirection after click edit own task was not successful", currentTasksEditPage.isAt());

        currentTasksEditPage.closeTask();
        assertTrue("Redirection after click close task was not successful", tasksPage.isAt());

        task = ServiceManager.getTaskService().getById(12);
        assertEquals("Task was not closed!", TaskStatus.DONE, task.getProcessingStatus());
    }

    @Test
    public void editOwnedTaskAndTakeNextForParallelWorkflowTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        Task task = ServiceManager.getTaskService().getById(19);
        tasksPage.editOwnedTask(task.getTitle(), task.getProcess().getTitle());
        assertTrue("Redirection after click edit own task was not successful", currentTasksEditPage.isAt());

        currentTasksEditPage.closeTask();
        assertTrue("Redirection after click close task was not successful", tasksPage.isAt());

        task = ServiceManager.getTaskService().getById(19);
        assertEquals("Task '" + task.getTitle() + "'  was not closed!", TaskStatus.DONE, task.getProcessingStatus());

        task = ServiceManager.getTaskService().getById(20);
        assertEquals("Task '" + task.getTitle() + "' cannot be taken by user!", TaskStatus.OPEN,
            task.getProcessingStatus());
        task = ServiceManager.getTaskService().getById(21);
        assertEquals("Task '" + task.getTitle() + "' can be taken by user!", TaskStatus.DONE,
            task.getProcessingStatus());
        task = ServiceManager.getTaskService().getById(22);
        assertEquals("Task '" + task.getTitle() + "'  cannot be taken by user!", TaskStatus.OPEN,
            task.getProcessingStatus());

        tasksPage.takeOpenTask("Task4", "Parallel");
        assertTrue("Redirection after click take task was not successful", currentTasksEditPage.isAt());

        task = ServiceManager.getTaskService().getById(22);
        assertEquals("Task '" + task.getTitle() + "' was not taken by user!", TaskStatus.INWORK,
            task.getProcessingStatus());

        task = ServiceManager.getTaskService().getById(20);
        assertEquals("Task '" + task.getTitle() + "' was not blocked after concurrent task was taken by user!",
            TaskStatus.LOCKED, task.getProcessingStatus());
    }

    @Test
    public void downloadDocketTest() throws Exception {
        processesPage.goTo().downloadDocket();
        assertTrue("Docket file was not downloaded", new File(Browser.DOWNLOAD_DIR + "Second__process.pdf").exists());
    }

    @Test
    public void downloadLogTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        processesPage.goTo().downloadLog();
        File logFile = new File("src/test/resources/users/kowal/Second__process_log.xml");
        assertTrue("Log file was not downloaded", logFile.exists());

        logFile.delete();
    }

    @Test
    public void editMetadataTest() throws Exception {
        processesPage.goTo().editSecondProcessMetadata();
        assertTrue("Redirection after click edit metadata was not successful", Pages.getMetadataEditorPage().isAt());
    }

    @Test
    public void downloadSearchResultAsExcelTest() throws Exception {
        processesPage.goTo().downloadSearchResultAsExcel();
        assertTrue("Search result excel file was not downloaded",
            new File(Browser.DOWNLOAD_DIR + "search.xls").exists());
    }

    @Test
    public void downloadSearchResultAsPdfTest() throws Exception {
        processesPage.goTo().downloadSearchResultAsPdf();
        assertTrue("Search result pdf file was not downloaded", new File(Browser.DOWNLOAD_DIR + "search.pdf").exists());
    }
}
