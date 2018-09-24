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
import static org.junit.Assume.assumeTrue;

import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;

public class EditingST extends BaseTestSelenium {

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
    public void editProcessTest() throws Exception {
        assumeTrue(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC);

        Pages.getProcessesPage().editProcess();
        assertEquals("Header for edit process is incorrect", "Vorgang bearbeiten (First process)",
                Pages.getProcessEditPage().getHeaderText());
    }

    @Test
    public void editProjectTest() throws Exception {
        Pages.getProjectsPage().editProject();
        assertEquals("Header for edit project is incorrect", "Projekt bearbeiten (First project)",
            Pages.getProjectEditPage().getHeaderText());
    }

    @Test
    public void addTemplateTest() throws Exception {
        Pages.getProjectsPage().editTemplate();
        assertEquals("Header for edit template is incorrect", "Produktionsvorlage bearbeiten (First template)",
            Pages.getTemplateEditPage().getHeaderText());
    }

    @Test
    public void editWorkflowTest() throws Exception {
        Pages.getProjectsPage().editWorkflow();
        assertEquals("Header for edit ruleset is incorrect", "Workflow bearbeiten (say-hello)",
                Pages.getWorkflowEditPage().getHeaderText());
    }

    @Test
    public void editDocketTest() throws Exception {
        Pages.getProjectsPage().editDocket();
        assertEquals("Header for edit docket is incorrect", "Laufzettel bearbeiten (default)",
                Pages.getDocketEditPage().getHeaderText());
    }

    @Test
    public void editRulesetTest() throws Exception {
        Pages.getProjectsPage().editRuleset();
        assertEquals("Header for edit ruleset is incorrect", "Regelsatz bearbeiten (SLUBDD)",
                Pages.getRulesetEditPage().getHeaderText());
    }

    @Test
    public void editUserTest() throws Exception {
        Pages.getUsersPage().editUser();
        assertEquals("Header for edit user is incorrect", "Benutzer bearbeiten (Kowalski, Jan)",
            Pages.getUserEditPage().getHeaderText());
    }

    @Test
    public void editUserGroupTest() throws Exception {
        Pages.getUsersPage().editUserGroup();
        assertEquals("Header for edit user group is incorrect", "Benutzergruppe bearbeiten (Admin)",
                Pages.getClientEditPage().getHeaderText());
    }

    @Test
    public void editLdapGroupTest() throws Exception {
        Pages.getUsersPage().editLdapGroup();
        assertEquals("Header for edit LDAP group is incorrect", "LDAP-Gruppe bearbeiten (LG)",
            Pages.getLdapGroupEditPage().getHeaderText());
    }

    @Test
    public void editClientTest() throws Exception {
        Pages.getUsersPage().editClient();
        assertEquals("Header for edit client is incorrect", "Mandant bearbeiten",
            Pages.getClientEditPage().getHeaderText());
    }
}
