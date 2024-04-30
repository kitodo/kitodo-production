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

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.CalendarPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;

public class CalendarST extends BaseTestSelenium {

    private static ProcessesPage processesPage;
    private static CalendarPage calendarPage;

    @BeforeClass
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        calendarPage = Pages.getCalendarPage();
    }

    @After
    public void logout() throws Exception {
        calendarPage.closePage();
        Pages.getTopNavigation().logout();
    }

    @Test
    public void createProcessFromCalendar() throws Exception {
        // add process to access calendar
        MockDatabase.insertProcessForCalendarHierarchyTests();

        login();
        processesPage.goTo();
        processesPage.goToCalendar();
        calendarPage.addBlock();
        calendarPage.addIssue("Morning issue");
        calendarPage.addIssue("Evening issue");
        calendarPage.addMetadataToThis();
        calendarPage.addMetadataToAll();
        List<String> morningIssueMetadata = calendarPage.getMetadata("Morning issue");
        List<String> eveningIssueMetadata = calendarPage.getMetadata("Evening issue");
        assertEquals("Metadata for morning issue is incorrect", Arrays.asList("Signatur", "Process title"), morningIssueMetadata);
        assertEquals("Metadata for evening issue is incorrect", List.of("Signatur"), eveningIssueMetadata);
    }

    private void login() throws Exception {
        User calendarUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(calendarUser);
    }
}
