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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.TasksPage;


/**
 * Tests the task list for various requirements related to sorting it.
 */
public class TasksSortingST extends BaseTestSelenium {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(TasksSortingST.class);

    private static TasksPage tasksPage;

    @BeforeClass
    public static void setup() throws Exception {
        tasksPage = Pages.getTasksPage();
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
     * Verifies that clicking the header of the title column of the task list 
     * will indeed trigger that tasks are sorted by title.
     */
    @Test
    public void sortByTaskTitle() throws Exception {
        tasksPage.goTo();

        // default first task title is "Next Open"
        assertEquals("Default task sort should be ascending by title", "Next Open", tasksPage.getFirstRowTaskTitle());

        // first click on title column triggers reverse order such that top task is "Progress"
        tasksPage.clickTaskTableColumnHeaderForSorting(2);
        assertEquals("Reverse order by task title not correct", "Progress", tasksPage.getFirstRowTaskTitle());
    }

    /**
     * Verifies that clicking the header of the process column of the task list 
     * will indeed trigger that tasks are sorted by process title.
     */
    @Test
    public void sortByTaskProcessTitle() throws Exception {
        tasksPage.goTo();

        // first click process top task "Progress" or "Open"
        tasksPage.clickTaskTableColumnHeaderForSorting(3);
        assertTrue("Sorting tasks by process title not correct", tasksPage.getFirstRowTaskTitle().matches("Progress|Open"));

        tasksPage.clickTaskTableColumnHeaderForSorting(3);
        // second click process header top task "Processed and Some" or "Next Open"
        assertTrue("Reverse-sorting tasks by process title not correct", 
            tasksPage.getFirstRowTaskTitle().matches("Processed and Some|Next Open"));
    }

    /**
     * Verifies that clicking the header of the status column of the task list 
     * will indeed trigger that tasks are sorted by status.
     */
    @Test
    public void sortByTaskStatus() throws Exception {
        tasksPage.goTo();

        // first click on status header should have top task "Progress" or "Processed Some" (both having the same status)
        tasksPage.clickTaskTableColumnHeaderForSorting(4);
        assertTrue("Sorting tasks by status not correct", tasksPage.getFirstRowTaskTitle().matches("Progress|Processed Some"));

        // second click on status header should have top task "Open" or "Next Open" (both having the same status)
        tasksPage.clickTaskTableColumnHeaderForSorting(4);
        assertTrue("Sorting tasks by status not correct", tasksPage.getFirstRowTaskTitle().matches("Open|Next Open"));
    }
}
