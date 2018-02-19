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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.selenium.testframework.helper.Timer;
import org.kitodo.services.ServiceManager;

public class SeleniumST extends BaseTestSelenium {

    private static final Logger logger = LogManager.getLogger(SeleniumST.class);
    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void listClientsTest() throws Exception {
        Pages.getClientsPage().goTo();
        int numberOfClientsInDatabase = serviceManager.getClientService().getAll().size();
        int numberOfClientsDisplayed = Pages.getClientsPage().countListedClients();
        Assert.assertEquals("Displayed wrong number of clients", numberOfClientsInDatabase, numberOfClientsDisplayed);
    }

    @Test
    public void listUsersTest() throws Exception {
        int numberOfUsersInDatabase = serviceManager.getUserService().getAll().size();
        int numberOfUsersDisplayed = Pages.getUsersPage().goTo().countListedUsers();
        Assert.assertEquals("Displayed wrong number of users", numberOfUsersInDatabase, numberOfUsersDisplayed);
    }

    @Test
    public void listUserGroupsTest() throws Exception {
        int numberOfUserGroupsInDatabase = serviceManager.getUserGroupService().getAll().size();
        int numberOfUserGroupsDisplayed = Pages.getUsersPage().goTo().countListedUserGroups();
        Assert.assertEquals("Displayed wrong number of user groups", numberOfUserGroupsInDatabase,
            numberOfUserGroupsDisplayed);
    }

    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().goTo().goToUserEditPage().insertUserData(user).save();
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);

        Assert.assertTrue("Login with new generated user was not possible", Pages.getStartPage().isAt());
    }

    @Test
    public void reindexingTest() throws Exception {
        final float MAXIMUM_TIME_SEC = 40;
        Pages.getIndexingPage().goTo().startReindexingAll();

        Timer timer = new Timer();
        timer.start();
        while (!Pages.getIndexingPage().isIndexingComplete()
                && timer.getElapsedTimeAfterStartSec() < MAXIMUM_TIME_SEC) {
            logger.debug("Indexing at: " + Pages.getIndexingPage().getIndexingProgress() + "%");
            Thread.sleep(Browser.getDelayIndexing());
        }
        timer.stop();
        Thread.sleep(Browser.getDelayIndexing());

        logger.info("Reindexing took: " + timer.getElapsedTimeSec() + " s");
        Assert.assertTrue("Reindexing took to long", timer.getElapsedTimeSec() < MAXIMUM_TIME_SEC);
    }

    @Test
    public void removeAllGlobalAuthoritiesOffUserGroupTest() throws Exception {
        UserGroup userGroup = serviceManager.getUserGroupService().getById(1);
        Pages.getUsersPage().goTo().switchToUserGroupsTab().goToUserGroupEditPage(userGroup)
                .removeAllGlobalAuthorities().save();

        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLoginAsAdmin();
        Pages.getUsersPage().goTo().switchToUserGroupsTab().goToUserGroupEditPage(userGroup);

        int availableGlobalAuthorities = Pages.getUserGroupEditPage().countAvailableGlobalAuthorities();
        int authoritiesInDatabase = serviceManager.getUserGroupService().getById(1).getGlobalAuthorities().size();

        Assert.assertEquals(
            "Removing off all global authorities was not saved! Number of Authorities in Database is: "
                    + authoritiesInDatabase,
            3, availableGlobalAuthorities);
    }

    @Test
    public void renameUserGroupTest() throws Exception {
        String expectedTitle = "SeleniumGroup";

        UserGroup userGroup = serviceManager.getUserGroupService().getById(1);
        Pages.getUsersPage().goTo().switchToUserGroupsTab().goToUserGroupEditPage(userGroup)
                .setUserGroupTitle(expectedTitle).save();

        System.out.println(serviceManager.getUserGroupService().getById(1).getTitle());

        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLoginAsAdmin();
        String actualTitle = Pages.getUsersPage().goTo().switchToUserGroupsTab().goToUserGroupEditPage(userGroup)
                .getUserGroupTitle();

        System.out.println(serviceManager.getUserGroupService().getById(1).getTitle());

        Assert.assertEquals("New Name of user group was not saved", expectedTitle, actualTitle);
    }
}
