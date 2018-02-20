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

import java.util.List;

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

    private ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(SeleniumST.class);

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
        Pages.getUsersPage().goTo().createNewUser().insertUserData(user).save();
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
    public void addUserGroupTest() throws Exception {
        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("MockUserGroup");

        Pages.getUsersPage().goTo().switchToUserGroupsTab().createNewUserGroup().setUserGroupTitle(userGroup.getTitle())
                .assignAllGlobalAuthorities().save();

        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLoginAsAdmin();
        List<String> listOfUserGroupTitles = Pages.getUsersPage().goTo().switchToUserGroupsTab()
                .getListOfUserGroupTitles();
        Assert.assertTrue("New user group was not saved", listOfUserGroupTitles.contains(userGroup.getTitle()));

        int availableAuthorities = serviceManager.getAuthorityService().getAll().size();
        int assignedGlobalAuthorities = Pages.getUsersPage().switchToUserGroupsTab().editUserGroup(userGroup.getTitle())
                .countAssignedGlobalAuthorities();
        Assert.assertEquals("Assigned authorities of the new user group was not saved!", availableAuthorities,
            assignedGlobalAuthorities);

        String actualTitle = Pages.getUserGroupEditPage().getUserGroupTitle();
        Assert.assertEquals("New Name of user group was not saved", userGroup.getTitle(), actualTitle);
    }
}
