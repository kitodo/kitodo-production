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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProcessesPage;


/**
 * Tests the processes list for various requirements related to sorting it.
 */
public class ProcessesSortingST extends BaseTestSelenium {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(ProcessesSortingST.class);

    private static ProcessesPage processesPage;

    @BeforeClass
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    /**
     * Verifies that clicking the header of the title column of the processes list 
     * will indeed trigger that processes are sorted by title.
     */
    @Test
    public void sortByProcessTitle() throws Exception {
        processesPage.goTo();

        // check that default sort order (descending by id) has second process as first element in the process list
        assertEquals("Second process", processesPage.getProcessTitles().get(0));

        // click on column header to trigger ascending order by process title
        processesPage.clickProcessesTitleColumnForSorting();

        // check that first process is now first element in list of processes
        assertEquals("First process", processesPage.getProcessTitles().get(0));

        // click again to trigger descending order for process title
        processesPage.clickProcessesTitleColumnForSorting();

        // check that second process is again first element in list of processes 
        assertEquals("Second process", processesPage.getProcessTitles().get(0));   
    }

    /**
     * Verifies that the current sorting of the processes list is remembered
     * even when reloading the processes page.
     */
    @Test
    public void sortOrderIsRememberedAfterReload() throws Exception {
        processesPage.goTo();

        // check that default sort order (descending by id) has second process as first element in the process list
        assertEquals("Second process", processesPage.getProcessTitles().get(0));

        // click on column header to trigger ascending order by process title
        processesPage.clickProcessesTitleColumnForSorting();

        // check that first process is now first element in list of processes
        assertEquals("First process", processesPage.getProcessTitles().get(0));

        // reload page
        processesPage.goTo();

        // check that first process is still first element in list of processes
        assertEquals("First process", processesPage.getProcessTitles().get(0));
    }
}
