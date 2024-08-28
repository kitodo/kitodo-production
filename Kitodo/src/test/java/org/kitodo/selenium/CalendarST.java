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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
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

    @BeforeClass
    public static void setup() throws Exception {
        processesPage = Pages.getProcessesPage();
        calendarPage = Pages.getCalendarPage();
        int rulesetId = MockDatabase.insertRuleset("Newspaper", "newspaper.xml", 1);
        newspaperTestProcessId = MockDatabase.insertTestProcess(NEWSPAPER_TEST_PROCESS_TITLE, 1, 1, rulesetId);
        ProcessTestUtils.copyTestMetadataFile(newspaperTestProcessId, NEWSPAPER_TEST_METADATA_FILE);
    }

    @Before
    public void login() throws Exception {
        User calendarUser = ServiceManager.getUserService().getByLogin("kowal");
        Pages.getLoginPage().goTo().performLogin(calendarUser);
    }

    @After
    public void logout() throws Exception {
        calendarPage.closePage();
        Pages.getTopNavigation().logout();
    }

    @AfterClass
    public static void cleanup() throws CustomResponseException, DAOException, DataException, IOException {
        ProcessTestUtils.removeTestProcess(newspaperTestProcessId);
    }

    @Test
    @Ignore("currently not implemented")
    /* 'baseType' not available (only available from index), is null.
     * goToCalendar() fails because button is gray, because 'baseType' is not
     * "Newspaper". */
    public void createProcessFromCalendar() throws Exception {
        processesPage.goTo();
        processesPage.goToCalendar(newspaperTestProcessId);
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
}
