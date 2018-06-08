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
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.kitodo.selenium.testframework.generators.LdapGroupGenerator;
import org.kitodo.selenium.testframework.generators.ProjectGenerator;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.services.ServiceManager;

public class AddingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void addProjectTest() throws Exception {
        Project project = ProjectGenerator.generateProject();
        Pages.getProjectsPage().createNewProject().insertProjectData(project).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        boolean projectAvailable = Pages.getProjectsPage().getProjectsTitles().contains(project.getTitle());
        Assert.assertTrue("Created Project was not listed at projects table!", projectAvailable);
    }

    @Test
    public void addDocketTest() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("MockDocket");
        docket.setFile("MetsModsGoobi_to_MetsKitodo.xsl");
        Pages.getProjectsPage().createNewDocket().insertDocketData(docket).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> docketTitles = Pages.getProjectsPage().switchToTabByIndex(TabIndex.DOCKETS.getIndex()).getDocketTitles();
        boolean docketAvailable = docketTitles.contains(docket.getTitle());
        Assert.assertTrue("Created Docket was not listed at dockets table!", docketAvailable);
    }

    @Test
    public void addRulesetTest() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("MockRuleset");
        ruleset.setFile("ruleset_test.xml");
        Pages.getProjectsPage().createNewRuleset().insertRulesetData(ruleset).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> rulesetTitles = Pages.getProjectsPage().switchToTabByIndex(TabIndex.RULESETS.getIndex()).getRulesetTitles();
        boolean rulesetAvailable = rulesetTitles.contains(ruleset.getTitle());
        Assert.assertTrue("Created Ruleset was not listed at rulesets table!", rulesetAvailable);
    }

    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().createNewUser().insertUserData(user).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);
        Assert.assertTrue("Login with new generated user was not possible", Pages.getStartPage().isAt());
    }

    @Test
    public void addLdapGroupTest() throws Exception {
        LdapGroup ldapGroup = LdapGroupGenerator.generateLdapGroup();
        Pages.getUsersPage().createNewLdapGroup().insertLdapGroupData(ldapGroup);

        Pages.getLdapGroupEditPage().save();

        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());

        boolean ldapGroupAvailable = Pages.getUsersPage().switchToTabByIndex(TabIndex.LDAP_GROUPS.getIndex()).getLdapGroupNames()
                .contains(ldapGroup.getTitle());

        Assert.assertTrue("Created ldap group was not listed at ldap group table!", ldapGroupAvailable);

        LdapGroup actualLdapGroup = Pages.getUsersPage().editLdapGroup(ldapGroup.getTitle()).readLdapGroup();
        Assert.assertEquals("Saved ldap group is giving wrong data at edit page!", ldapGroup, actualLdapGroup);
    }

    @Test
    public void addClientTest() throws Exception {
        Client client = new Client();
        client.setName("MockClient");
        Pages.getUsersPage().switchToTabByIndex(TabIndex.CLIENTS.getIndex()).createNewClient().insertClientData(client).save();
        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        boolean clientAvailable = Pages.getUsersPage().switchToTabByIndex(TabIndex.CLIENTS.getIndex()).getClientNames().contains(client.getName());
        Assert.assertTrue("Created Client was not listed at clients table!", clientAvailable);
    }
    
    @Test
    public void addUserGroupTest() throws Exception {
        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("MockUserGroup");

        Pages.getUsersPage().switchToTabByIndex(TabIndex.USER_GROUPS.getIndex()).createNewUserGroup().setUserGroupTitle(userGroup.getTitle())
                .assignAllGlobalAuthorities().assignAllClientAuthorities().assignAllProjectAuthorities();

        Pages.getUserGroupEditPage().save();

        Assert.assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());

        List<String> userGroupTitles = Pages.getUsersPage().switchToTabByIndex(TabIndex.USER_GROUPS.getIndex())
                .getUserGroupTitles();
        Assert.assertTrue("New user group was not saved", userGroupTitles.contains(userGroup.getTitle()));

        int availableAuthorities = serviceManager.getAuthorityService().getAll().size();
        int assignedGlobalAuthorities = Pages.getUsersPage().switchToTabByIndex(TabIndex.USER_GROUPS.getIndex()).editUserGroup(userGroup.getTitle())
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
