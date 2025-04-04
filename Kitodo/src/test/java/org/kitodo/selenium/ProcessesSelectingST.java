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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;

public class ProcessesSelectingST extends BaseTestSelenium {

    private static ProcessesPage processesPage;

    /**
     * Set up process selecting tests.
     * @throws Exception as exception
     */
    @BeforeAll
    public static void setup() throws Exception {

        User user = ServiceManager.getUserService().getById(1);
        user.setTableSize(1);
        ServiceManager.getUserService().save(user);
        processesPage = Pages.getProcessesPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    /**
     * Tests selection of all processes on one page in the processes data table.
     * @throws Exception as exception
     */
    @Test
    public void selectAllProcessesOnPageTest() throws Exception {
        processesPage.goTo();

        processesPage.selectAllRowsOnPage();
        assertEquals(processesPage.countListedSelectedProcesses(), 1);

        processesPage.goToNextPage();
        assertEquals(processesPage.countListedSelectedProcesses(), 0);
    }

    /**
     *Tests selection of all processes in the processes data table.
     * @throws Exception as exception
     */
    @Test
    public void selectAllProcessesTest() throws Exception {
        processesPage.goTo();

        processesPage.selectAllRows();
        assertEquals(processesPage.countListedSelectedProcesses(), 1);

        processesPage.goToNextPage();
        assertEquals(processesPage.countListedSelectedProcesses(), 1);
    }
}
