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
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.generators.LdapGroupGenerator;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.services.ServiceManager;

public class ListingAddingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void securityAccessTest() throws Exception {
        boolean expectedTrue = Pages.getTopNavigation().isShowingAllLinks();
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
    public void listProjectsTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int numberOfProjectsInDatabase = serviceManager.getProjectService().getAll().size();
        int numberOfProjectsDisplayed = Pages.getProjectsPage().countListedProjects();
        Assert.assertEquals("Displayed wrong number of projects", numberOfProjectsInDatabase,
            numberOfProjectsDisplayed);
    }

    @Test
    public void listTemplatesTest() throws Exception {
        Pages.getProjectsPage().goTo();
        int numberOfTemplatesInDatabase = serviceManager.getTemplateService().getActiveTemplates().size();
        int numberOfTemplatesDisplayed = Pages.getProjectsPage().countListedTemplates();
        Assert.assertEquals("Displayed wrong number of templates", numberOfTemplatesInDatabase,
            numberOfTemplatesDisplayed);
    }

    @Test
    public void listProcessesTest() throws Exception {
        Pages.getProcessesPage().goTo();
        int numberOfProcessesInDatabase = serviceManager.getProcessService().getActiveProcesses().size();
        int numberOfProcessesDisplayed = Pages.getProcessesPage().countListedProcesses();
        Assert.assertEquals("Displayed wrong number of processes", numberOfProcessesInDatabase,
            numberOfProcessesDisplayed);
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
    public void listLdapGroupsTest() throws Exception {
        int numberOfLdapGroupsInDatabase = serviceManager.getLdapGroupService().getAll().size();
        int numberOfLdapGroupsDisplayed = Pages.getUsersPage().goTo().switchToTabByIndex(2).countListedLdapGroups();
        Assert.assertEquals("Displayed wrong number of ldap groups!", numberOfLdapGroupsInDatabase,
            numberOfLdapGroupsDisplayed);
    }

    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().goTo().createNewUser().insertUserData(user).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);
        Assert.assertTrue("Login with new generated user was not possible", Pages.getStartPage().isAt());
    }

    @Test
    public void addLdapGroupTest() throws Exception {
        LdapGroup ldapGroup = LdapGroupGenerator.generateLdapGroup();
        Pages.getUsersPage().goTo().createNewLdapGroup().insertLdapGroupData(ldapGroup);
        // We need to wait longer here because the many inputs produces a lot of toggling
        // on the save button which makes it stale and throws a StaleElementException
        Thread.sleep(9000);
        Pages.getLdapGroupEditPage().save();

        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLoginAsAdmin();
        boolean ldapGroupAvailable = Pages.getUsersPage().goTo().switchToTabByIndex(2).getListOfLdapGroupNames()
                .contains(ldapGroup.getTitle());

        Assert.assertTrue("Created ldap group was not listed at ldap group table!", ldapGroupAvailable);

        LdapGroup actualLdapGroup = Pages.getUsersPage().editLdapGroup(ldapGroup.getTitle()).readLdapGroup();
        Assert.assertEquals("Saved ldap group is giving wrong data at edit page!", ldapGroup, actualLdapGroup);
    }

    @Test
    public void addClientTest() throws Exception {
        Client client = new Client();
        client.setName("MockClient");
        Pages.getClientsPage().goTo().createNewClient().insertClientData(client).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getClientsPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLoginAsAdmin();
        boolean clientAvailable = Pages.getClientsPage().goTo().getListOfClientNames().contains(client.getName());
        Assert.assertTrue("Created Client was not listed at clients table!", clientAvailable);
    }

    @Test
    public void addUserGroupTest() throws Exception {
        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("MockUserGroup");

        Pages.getUsersPage().goTo().switchToTabByIndex(1).createNewUserGroup().setUserGroupTitle(userGroup.getTitle())
                .assignAllGlobalAuthorities().assignAllClientAuthorities().assignAllProjectAuthorities();

        Thread.sleep(2000);
        Pages.getUserGroupEditPage().save();

        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
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
