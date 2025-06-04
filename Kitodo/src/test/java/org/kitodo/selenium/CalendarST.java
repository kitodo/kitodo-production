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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.CalendarPage;
import org.kitodo.selenium.testframework.pages.ProcessesPage;
import org.kitodo.test.utils.ProcessTestUtils;

public class CalendarST extends BaseTestSelenium {

    private static ProcessesPage processesPage;
    private static CalendarPage calendarPage;
    private static int newspaperTestProcessId = -1;
    private static final String NEWSPAPER_TEST_METADATA_FILE = "testmetaNewspaper.xml";
    private static final String NEWSPAPER_TEST_PROCESS_TITLE = "NewspaperOverallProcess";

    @BeforeAll
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        calendarPage = Pages.getCalendarPage();
        int rulesetId = MockDatabase.insertRuleset("Newspaper", "newspaper.xml", 1);
        newspaperTestProcessId = MockDatabase.insertTestProcess(NEWSPAPER_TEST_PROCESS_TITLE, 1, 1, rulesetId);
        ProcessTestUtils.copyTestMetadataFile(newspaperTestProcessId, NEWSPAPER_TEST_METADATA_FILE);
    }

    @BeforeEach
    public void login() throws Exception {
        User calendarUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(calendarUser);
    }

    @AfterEach
    public void logout() throws Exception {
        calendarPage.closePage();
        Pages.getTopNavigation().logout();
    }

    @AfterAll
    public static void cleanup() throws DAOException, IOException {
        ProcessTestUtils.removeTestProcess(newspaperTestProcessId);
    }

    @Disabled("faulty, randomly fails during CI builds; needs to be fixed")
    @Test
    public void createProcessFromCalendar() throws Exception {
        processesPage.goTo();
        processesPage.goToCalendar(newspaperTestProcessId);
        calendarPage.addBlock();
        calendarPage.addIssue("Morning issue");
        calendarPage.addIssue("Evening issue");
        assertEquals(4, calendarPage.countIssues(), "Number of issues in the calendar does not match");
        calendarPage.addMetadataToThis();
        calendarPage.addMetadataToAll();
        List<String> morningIssueMetadata = calendarPage.getMetadata("Morning issue");
        List<String> eveningIssueMetadata = calendarPage.getMetadata("Evening issue");
        assertEquals(Arrays.asList("Signatur", "Process title"), morningIssueMetadata, "Metadata for morning issue is incorrect");
        assertEquals(List.of("Signatur"), eveningIssueMetadata, "Metadata for evening issue is incorrect");
    }
}
