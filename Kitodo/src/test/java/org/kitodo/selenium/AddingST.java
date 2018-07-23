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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.kitodo.selenium.testframework.generators.LdapGroupGenerator;
import org.kitodo.selenium.testframework.generators.ProjectGenerator;
import org.kitodo.selenium.testframework.generators.UserGenerator;
import org.kitodo.services.ServiceManager;
import org.openqa.selenium.By;

public class AddingST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void addProjectTest() throws Exception {
        Project project = ProjectGenerator.generateProject();
        Pages.getProjectsPage().createNewProject().insertProjectData(project).save();
        assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        boolean projectAvailable = Pages.getProjectsPage().getProjectsTitles().contains(project.getTitle());
        assertTrue("Created Project was not listed at projects table!", projectAvailable);
    }

    @Test
    public void addTemplateTest() throws Exception {
        Template template = new Template();
        template.setTitle("MockTemplate");
        Pages.getProjectsPage().createNewTemplate().insertTemplateData(template).save();
        boolean templateAvailable = Pages.getProjectsPage().getTemplateTitles().contains(template.getTitle());
        assertTrue("Created Template was not listed at templates table!", templateAvailable);
    }

    @Test
    public void addProcessTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        Pages.getProjectsPage().switchToTabByIndex(TabIndex.TEMPLATES.getIndex()).createNewProcess();
        String generatedTitle = Pages.getProcessFromTemplatePage().createProcess();
        boolean processAvailable = Pages.getProcessesPage().getProcessTitles().contains(generatedTitle);
        assertTrue("Created Process was not listed at processes table!", processAvailable);
    }

    @Ignore("for some unknown yet reason save doesn't work if executed automatically")
    @Test
    public void addWorkflowTest() throws Exception {
        Workflow workflow = new Workflow();
        workflow.setFileName("testWorkflow");
        Pages.getProjectsPage().createNewWorkflow().insertWorkflowData(workflow).save();
        assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> workflowTitles = Pages.getProjectsPage().getWorkflowTitles();
        boolean workflowAvailable = workflowTitles.contains("Process_1");
        assertTrue("Created Workflow was not listed at workflows table!", workflowAvailable);
    }

    @Test
    public void addDocketTest() throws Exception {
        Docket docket = new Docket();
        docket.setTitle("MockDocket");
        Pages.getProjectsPage().createNewDocket().insertDocketData(docket).save();
        assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> docketTitles = Pages.getProjectsPage().getDocketTitles();
        boolean docketAvailable = docketTitles.contains(docket.getTitle());
        assertTrue("Created Docket was not listed at dockets table!", docketAvailable);
    }

    @Test
    public void addRulesetTest() throws Exception {
        Ruleset ruleset = new Ruleset();
        ruleset.setTitle("MockRuleset");
        Pages.getProjectsPage().createNewRuleset().insertRulesetData(ruleset).save();
        assertTrue("Redirection after save was not successful", Pages.getProjectsPage().isAt());
        List<String> rulesetTitles = Pages.getProjectsPage().getRulesetTitles();
        boolean rulesetAvailable = rulesetTitles.contains(ruleset.getTitle());
        assertTrue("Created Ruleset was not listed at rulesets table!", rulesetAvailable);
    }

    @Test
    public void addUserTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().createNewUser().insertUserData(user).save();
        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);
        assertFalse("New generated user should not be able to select any client", Pages.getTopNavigation().isClientSelectionPossible());
        Pages.getTopNavigation().cancelClientSelection();
        Pages.getLoginPage().performLoginAsAdmin();
    }

    @Test
    public void addUserAndAssignUserGroupTest() throws Exception {
        User user = UserGenerator.generateUser();
        Pages.getUsersPage().createNewUser().insertUserData(user).switchToTabByIndex(TabIndex.USER_USER_GROUPS.getIndex());
        Pages.getUserEditPage().addUserToUserGroup(serviceManager.getUserGroupService().getById(2).getTitle()).save();
        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(user);
        Pages.getTopNavigation().acceptClientSelection();
        assertEquals(serviceManager.getClientService().getById(1).getName(), Pages.getTopNavigation().getSessionClient());
    }

    @Test
    public void addLdapGroupTest() throws Exception {
        LdapGroup ldapGroup = LdapGroupGenerator.generateLdapGroup();
        Pages.getUsersPage().createNewLdapGroup().insertLdapGroupData(ldapGroup);

        Pages.getLdapGroupEditPage().save();
        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());

        boolean ldapGroupAvailable = Pages.getUsersPage().getLdapGroupNames().contains(ldapGroup.getTitle());
        assertTrue("Created ldap group was not listed at ldap group table!", ldapGroupAvailable);

        LdapGroup actualLdapGroup = Pages.getUsersPage().editLdapGroup(ldapGroup.getTitle()).readLdapGroup();
        assertEquals("Saved ldap group is giving wrong data at edit page!", ldapGroup, actualLdapGroup);
    }

    @Test
    public void addClientTest() throws Exception {
        Client client = new Client();
        client.setName("MockClient");
        Pages.getUsersPage().switchToTabByIndex(TabIndex.CLIENTS.getIndex()).createNewClient()
                .insertClientData(client).save();
        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        boolean clientAvailable = Pages.getUsersPage().switchToTabByIndex(TabIndex.CLIENTS.getIndex())
                .getClientNames().contains(client.getName());
        assertTrue("Created Client was not listed at clients table!", clientAvailable);
    }

    @Test
    public void addUserGroupTest() throws Exception {
        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("MockUserGroup");

        Pages.getUsersPage().goTo().switchToTabByIndex(TabIndex.USER_GROUPS.getIndex()).createNewUserGroup()
                .setUserGroupTitle(userGroup.getTitle()).assignAllGlobalAuthorities().assignAllClientAuthorities()
                .assignAllProjectAuthorities();

        Pages.getUserGroupEditPage().save();
        assertTrue("Redirection after save was not successful", Pages.getUsersPage().isAt());
        List<String> userGroupTitles = Pages.getUsersPage().switchToTabByIndex(TabIndex.USER_GROUPS.getIndex())
                .getUserGroupTitles();
        assertTrue("New user group was not saved", userGroupTitles.contains(userGroup.getTitle()));

        int availableAuthorities = serviceManager.getAuthorityService().getAll().size();
        int assignedGlobalAuthorities = Pages.getUsersPage().switchToTabByIndex(TabIndex.USER_GROUPS.getIndex())
                .editUserGroup(userGroup.getTitle()).countAssignedGlobalAuthorities();
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

        await("Wait for visible user configuration link").atMost(20, TimeUnit.SECONDS).ignoreExceptions().untilAsserted(
            () -> assertTrue(Browser.getDriver().findElement(By.partialLinkText("User configuration")).isDisplayed()));
    }
}
