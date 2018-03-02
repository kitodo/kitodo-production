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

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.services.ServiceManager;

public class ListingAddingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void securityAccessTest() throws Exception {
        boolean expectedTrue = Pages.getTopNavigation().isShowingAdmin();
        Assert.assertTrue("Top navigation is not showing that current user is admin", expectedTrue);
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
    public void addUserGroupTest() throws Exception {
        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("MockUserGroup");

        Pages.getUsersPage().goTo().switchToTabByIndex(1).createNewUserGroup().setUserGroupTitle(userGroup.getTitle())
                .assignAllGlobalAuthorities().assignAllClientAuthorities().assignAllProjectAuthorities().save();

        Pages.getStartPage().goTo();
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLoginAsAdmin();

        List<String> listOfUserGroupTitles = Pages.getUsersPage().goTo().switchToTabByIndex(1)
                .getListOfUserGroupTitles();
        Assert.assertTrue("New user group was not saved", listOfUserGroupTitles.contains(userGroup.getTitle()));

        int availableAuthorities = serviceManager.getAuthorityService().getAll().size();
        int assignedGlobalAuthorities = Pages.getUsersPage().switchToTabByIndex(1).editUserGroup(userGroup.getTitle())
                .countAssignedGlobalAuthorities();
        Assert.assertEquals("Assigned authorities of the new user group were not saved!", availableAuthorities,
            assignedGlobalAuthorities);

        String actualTitle = Pages.getUserGroupEditPage().getUserGroupTitle();
        Assert.assertEquals("New Name of user group was not saved", userGroup.getTitle(), actualTitle);

        int availableClientAuthorities = serviceManager.getAuthorityService().getAllAssignableToClients().size();
        int assignedClientAuthorities = Pages.getUserGroupEditPage().countAssignedClientAuthorities();
        Assert.assertEquals("Assigned client authorities of the new user group were not saved!",
            availableClientAuthorities, assignedClientAuthorities);

        int availableProjectAuthorities = serviceManager.getAuthorityService().getAllAssignableToProjects().size();
        int assignedProjectAuthorities = Pages.getUserGroupEditPage().countAssignedProjectAuthorities();
        Assert.assertEquals("Assigned project authorities of the new user group were not saved!",
            availableProjectAuthorities, assignedProjectAuthorities);

    }
}
