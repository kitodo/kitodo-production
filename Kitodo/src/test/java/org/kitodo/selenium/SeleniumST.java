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

import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.selenium.testframework.helper.Timer;
import org.kitodo.services.ServiceManager;

public class SeleniumST extends BaseTestSelenium {

    private static final Logger logger = LogManager.getLogger(SeleniumST.class);
    private ServiceManager serviceManager = new ServiceManager();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();

        Browser.Initialize();
    }

    @AfterClass
    public static void tearDown() throws Exception {

        Browser.Close();
        MockDatabase.stopDatabaseServer();
        MockDatabase.stopNode();

        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    @Before
    public void login() throws Exception {
        User user = serviceManager.getUserService().getById(1);
        user.setPassword("test");

        Pages.getLoginPage().goTo();
        Pages.getLoginPage().performLogin(user);
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    @Test
    public void gotoHelpPageTest() throws Exception {
        Pages.getHelpPage().goTo();
        Assert.assertTrue("Browser has not directed to help page", Pages.getHelpPage().isAt());
    }

    @Test
    public void gotoProcessesPageTest() throws Exception {
        Pages.getProcessesPage().goTo();
        Assert.assertTrue("Browser has not directed to processes page", Pages.getProcessesPage().isAt());
    }

    @Test
    public void gotoTasksPageTest() throws Exception {
        Pages.getTasksPage().goTo();
        Assert.assertTrue("Browser has not directed to tasks page", Pages.getTasksPage().isAt());
    }

    @Test
    public void listClientsTest() throws Exception {
        Pages.getClientsPage().goTo();
        int numberOfClientsInDatabase = serviceManager.getClientService().getAll().size();
        int numberOfClientsDisplayed = Pages.getClientsPage().countListedClients();
        Assert.assertEquals("Displayed wrong number of clients", numberOfClientsInDatabase, numberOfClientsDisplayed);
    }

    @Test
    public void listUsersTest() throws Exception {
        Pages.getUsersPage().goTo();
        int numberOfUsersInDatabase = serviceManager.getUserService().getAll().size();
        int numberOfUsersDisplayed = Pages.getUsersPage().countListedUsers();
        Assert.assertEquals("Displayed wrong number of users", numberOfUsersInDatabase, numberOfUsersDisplayed);
    }

    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().goTo();
        Pages.getUsersPage().goToAddUser().addUser(user);
        // Pages.getUsersPage().goTo();
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);

        Assert.assertTrue("Login with new generated user was not possible", Pages.getStartPage().isAt());
    }

    @Test
    public void reindexingTest() throws Exception {
        final float maximumIndexingTimeSec = 40;
        Pages.getIndexingPage().goTo();
        Pages.getIndexingPage().startReindexingAll();

        Timer timer = new Timer();
        timer.start();
        while (!Pages.getIndexingPage().isIndexingComplete()
                && timer.getElapsedTimeAfterStartSec() < maximumIndexingTimeSec) {
            logger.debug("Indexing at: " + Pages.getIndexingPage().getIndexingProgress() + "%");
            Thread.sleep(1000);
        }
        timer.stop();
        logger.info("Reindexing took: " + timer.getElapsedTimeSec() + " s");

        Assert.assertTrue("Reindexing took to long", timer.getElapsedTimeSec() < maximumIndexingTimeSec);
    }
}
