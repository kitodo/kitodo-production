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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.CurrentTasksEditPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.selenium.testframework.pages.TasksPage;

@Disabled
public class WorkingST extends BaseTestSelenium {

    private static CurrentTasksEditPage currentTasksEditPage;
    private static ProcessesPage processesPage;
    private static TasksPage tasksPage;

    @BeforeAll
    public static void setup() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();

        currentTasksEditPage = Pages.getCurrentTasksEditPage();
        processesPage = Pages.getProcessesPage();
        tasksPage = Pages.getTasksPage();
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

    @Disabled
    @Test
    public void takeOpenTaskAndGiveItBackTest() throws Exception {
        Task task = ServiceManager.getTaskService().getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task cannot be taken by user!");

        tasksPage.goTo().takeOpenTask("Open", "First process");
        assertTrue(currentTasksEditPage.isAt(), "Redirection after click take task was not successful");

        task = ServiceManager.getTaskService().getById(9);
        assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task was not taken by user!");

        currentTasksEditPage.releaseTask();
        assertTrue(tasksPage.isAt(), "Redirection after click release task was not successful");

        task = ServiceManager.getTaskService().getById(9);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task was not released by user!");
    }

    @Disabled
    @Test
    public void editOwnedTaskTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        Task task = ServiceManager.getTaskService().getById(12);
        tasksPage.goTo().editOwnedTask(task.getTitle(), task.getProcess().getTitle());
        assertTrue(currentTasksEditPage.isAt(), "Redirection after click edit own task was not successful");

        currentTasksEditPage.closeTask();
        assertTrue(tasksPage.isAt(), "Redirection after click close task was not successful");

        task = ServiceManager.getTaskService().getById(12);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task was not closed!");
    }

    @Test
    public void editOwnedTaskAndTakeNextForParallelWorkflowTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        Task task = ServiceManager.getTaskService().getById(19);
        tasksPage.editOwnedTask(task.getTitle(), task.getProcess().getTitle());
        assertTrue(currentTasksEditPage.isAt(), "Redirection after click edit own task was not successful");

        currentTasksEditPage.closeTask();
        assertTrue(tasksPage.isAt(), "Redirection after click close task was not successful");

        task = ServiceManager.getTaskService().getById(19);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "'  was not closed!");

        task = ServiceManager.getTaskService().getById(20);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "' cannot be taken by user!");
        task = ServiceManager.getTaskService().getById(21);
        assertEquals(TaskStatus.DONE, task.getProcessingStatus(), "Task '" + task.getTitle() + "' can be taken by user!");
        task = ServiceManager.getTaskService().getById(22);
        assertEquals(TaskStatus.OPEN, task.getProcessingStatus(), "Task '" + task.getTitle() + "'  cannot be taken by user!");

        tasksPage.takeOpenTask("Task4", "Parallel");
        assertTrue(currentTasksEditPage.isAt(), "Redirection after click take task was not successful");

        task = ServiceManager.getTaskService().getById(22);
        assertEquals(TaskStatus.INWORK, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not taken by user!");

        task = ServiceManager.getTaskService().getById(20);
        assertEquals(TaskStatus.LOCKED, task.getProcessingStatus(), "Task '" + task.getTitle() + "' was not blocked after concurrent task was taken by user!");
    }

    @Test
    public void downloadDocketTest() throws Exception {
        processesPage.goTo().downloadDocket();
        assertTrue(new File(Browser.DOWNLOAD_DIR + "Second__process.pdf").exists(), "Docket file was not downloaded");
    }

    @Test
    public void downloadLogTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        processesPage.goTo().downloadLog();
        File logFile = new File("src/test/resources/users/kowal/Second__process_log.xml");
        assertTrue(logFile.exists(), "Log file was not downloaded");

        logFile.delete();
    }

    @Test
    public void editMetadataTest() throws Exception {
        processesPage.goTo().editSecondProcessMetadata();
        assertTrue(Pages.getMetadataEditorPage().isAt(), "Redirection after click edit metadata was not successful");
    }

    @Test
    public void downloadSearchResultAsExcelTest() throws Exception {
        processesPage.goTo().downloadSearchResultAsExcel();
        assertTrue(new File(Browser.DOWNLOAD_DIR + "search.xls").exists(), "Search result excel file was not downloaded");
    }

    @Test
    public void downloadSearchResultAsPdfTest() throws Exception {
        processesPage.goTo().downloadSearchResultAsPdf();
        assertTrue(new File(Browser.DOWNLOAD_DIR + "search.pdf").exists(), "Search result pdf file was not downloaded");
    }
}
