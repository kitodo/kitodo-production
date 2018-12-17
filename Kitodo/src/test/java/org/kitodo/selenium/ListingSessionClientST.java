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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.production.services.ServiceManager;

public class ListingSessionClientST extends BaseTestSelenium {

    private static ProjectsPage projectsPage;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
    }

    @Before
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getById(2));
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void listProjectsForUserWithFirstSessionClientTest() throws Exception {
        User user = ServiceManager.getUserService().getById(2);
        SecurityTestUtils.addUserDataToSecurityContext(user, 1);

        Pages.getTopNavigation().selectSessionClient(0);

        // user will see only projects page as this one right he has for First client
        projectsPage.goTo();
        int projectsInDatabase = ServiceManager.getProjectService()
                .getByQuery("FROM Project AS p INNER JOIN p.users AS u WITH u.id = 2 INNER JOIN p.client AS c WITH c.id = 1").size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        exception.expect(IndexOutOfBoundsException.class);
        projectsPage.countListedTemplates();

        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void listProjectsForUserWithSecondSessionClientTest() throws Exception {
        User user = ServiceManager.getUserService().getById(2);
        SecurityTestUtils.addUserDataToSecurityContext(user, 2);

        Pages.getTopNavigation().selectSessionClient(1);

        // user will see first four tabs as this rights he has for Second client
        projectsPage.goTo();
        int projectsInDatabase = ServiceManager.getProjectService()
                .getByQuery("FROM Project AS p INNER JOIN p.users AS u WITH u.id = 2 INNER JOIN p.client AS c WITH c.id = 2").size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        // TODO: rewrite query for active templates
        //int templatesInDatabase = ServiceManager.getTemplateService().getActiveTemplates().size();
        int templatesDisplayed = projectsPage.countListedTemplates();
        assertEquals("Displayed wrong number of templates", 2, templatesDisplayed);

        int workflowsInDatabase = ServiceManager.getWorkflowService().getAllForSelectedClient().size();
        int workflowsDisplayed = projectsPage.countListedWorkflows();
        assertEquals("Displayed wrong number of workflows", workflowsInDatabase, workflowsDisplayed);

        int docketsInDatabase = ServiceManager.getDocketService().getAllForSelectedClient().size();
        int docketsDisplayed = projectsPage.countListedDockets();
        assertEquals("Displayed wrong number of dockets", docketsInDatabase, docketsDisplayed);

        exception.expect(IndexOutOfBoundsException.class);
        projectsPage.countListedRulesets();

        SecurityTestUtils.cleanSecurityContext();
    }
}
