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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WorkingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

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
    public void takeOpenTaskAndGiveItBackTest() throws Exception {
        Task task = serviceManager.getTaskService().getById(2);
        assertEquals("Task can not be taken by user!", task.getProcessingStatusEnum(), TaskStatus.OPEN);

        Pages.getTasksPage().goTo().takeOpenTask();
        assertTrue("Redirection after click take task was not successful", Pages.getCurrentTasksEditPage().isAt());

        task = serviceManager.getTaskService().getById(2);
        assertEquals("Task was not taken by user!", task.getProcessingStatusEnum(), TaskStatus.INWORK);

        Pages.getCurrentTasksEditPage().releaseTask();
        assertTrue("Redirection after click release task was not successful", Pages.getTasksPage().isAt());

        task = serviceManager.getTaskService().getById(2);
        assertEquals("Task was not released by user!", task.getProcessingStatusEnum(), TaskStatus.OPEN);
    }

    @Test
    public void editOwnedTaskTest() throws Exception {
        Pages.getTasksPage().goTo().editOwnedTask();
        assertTrue("Redirection after click edit own task was not successful", Pages.getCurrentTasksEditPage().isAt());

        Pages.getCurrentTasksEditPage().closeTask();
        assertTrue("Redirection after click close task was not successful", Pages.getTasksPage().isAt());

        Task task = serviceManager.getTaskService().getById(8);
        assertEquals("Task was not closed!", task.getProcessingStatusEnum(), TaskStatus.DONE);
    }
}
