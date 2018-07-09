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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.generators.LdapGroupGenerator;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.services.ServiceManager;
import org.openqa.selenium.By;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AddingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void addClientTest() throws Exception {
        Client client = new Client();
        client.setName("MockClient");
        Pages.getClientsPage().goTo().createNewClient().insertClientData(client).save();
        assertTrue("Redirection after save was not successful", Pages.getClientsPage().isAt());
        boolean clientAvailable = Pages.getClientsPage().goTo().getClientNames().contains(client.getName());
        assertTrue("Created Client was not listed at clients table!", clientAvailable);
    }

    @Test
    public void addDocketTest() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("MockDocket");
        docket.setFile("MetsModsGoobi_to_MetsKitodo.xsl");
        Pages.getProjectsPage().goTo().createNewDocket().insertDocketData(docket).save();
        assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> docketTitles = Pages.getProjectsPage().goTo().switchToTabByIndex(3).getDocketTitles();
        boolean docketAvailable = docketTitles.contains(docket.getTitle());
        assertTrue("Created Docket was not listed at dockets table!", docketAvailable);
    }

    @Test
    public void addRulesetTest() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("MockRuleset");
        ruleset.setFile("ruleset_test.xml");
        Pages.getProjectsPage().goTo().createNewRuleset().insertRulesetData(ruleset).save();
        assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> rulesetTitles = Pages.getProjectsPage().goTo().switchToTabByIndex(4).getRulesetTitles();
        boolean rulesetAvailable = rulesetTitles.contains(ruleset.getTitle());
        assertTrue("Created Ruleset was not listed at rulesets table!", rulesetAvailable);
    }

    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().goTo().createNewUser().insertUserData(user).save();
        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);
        assertTrue("Login with new generated user was not possible", Pages.getStartPage().isAt());
    }

    @Test
    public void addLdapGroupTest() throws Exception {
        LdapGroup ldapGroup = LdapGroupGenerator.generateLdapGroup();
        Pages.getUsersPage().goTo().createNewLdapGroup().insertLdapGroupData(ldapGroup);

        Pages.getLdapGroupEditPage().save();

        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());

        boolean ldapGroupAvailable = Pages.getUsersPage().goTo().switchToTabByIndex(2).getLdapGroupNames()
                .contains(ldapGroup.getTitle());

        assertTrue("Created ldap group was not listed at ldap group table!", ldapGroupAvailable);

        LdapGroup actualLdapGroup = Pages.getUsersPage().editLdapGroup(ldapGroup.getTitle()).readLdapGroup();
        assertEquals("Saved ldap group is giving wrong data at edit page!", ldapGroup, actualLdapGroup);
    }

    @Test
    public void addUserGroupTest() throws Exception {
        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("MockUserGroup");

        Pages.getUsersPage().goTo().switchToTabByIndex(1).createNewUserGroup().setUserGroupTitle(userGroup.getTitle())
                .assignAllGlobalAuthorities().assignAllClientAuthorities().assignAllProjectAuthorities();

        Pages.getUserGroupEditPage().save();

        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());

        List<String> userGroupTitles = Pages.getUsersPage().goTo().switchToTabByIndex(1).getUserGroupTitles();
        assertTrue("New user group was not saved", userGroupTitles.contains(userGroup.getTitle()));

        int availableAuthorities = serviceManager.getAuthorityService().getAll().size();
        int assignedGlobalAuthorities = Pages.getUsersPage().switchToTabByIndex(1).editUserGroup(userGroup.getTitle())
                .countAssignedGlobalAuthorities();
        assertEquals("Assigned authorities of the new user group were not saved!", availableAuthorities,
            assignedGlobalAuthorities);

        String actualTitle = Pages.getUserGroupEditPage().getUserGroupTitle();
        assertEquals("New Name of user group was not saved", userGroup.getTitle(), actualTitle);

        int availableClientAuthorities = serviceManager.getAuthorityService().getAllAssignableToClients().size();
        int assignedClientAuthorities = Pages.getUserGroupEditPage().countAssignedClientAuthorities();
        assertEquals("Assigned client authorities of the new user group were not saved!", availableClientAuthorities,
            assignedClientAuthorities);

        int availableProjectAuthorities = serviceManager.getAuthorityService().getAllAssignableToProjects().size();
        int assignedProjectAuthorities = Pages.getUserGroupEditPage().countAssignedProjectAuthorities();
        assertEquals("Assigned project authorities of the new user group were not saved!", availableProjectAuthorities,
            assignedProjectAuthorities);
    }

    @Test
    public void editUserConfigurationTest() throws Exception {
        Pages.getUserConfigurationPage().changeUserSettings();

        await("Wait for visible user configuration link").atMost(20, TimeUnit.SECONDS)
                .ignoreExceptions().untilAsserted(() -> assertTrue(
                Browser.getDriver().findElement(By.partialLinkText("User configuration")).isDisplayed()));
    }
}
