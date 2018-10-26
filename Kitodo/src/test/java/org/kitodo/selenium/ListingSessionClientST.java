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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.services.ServiceManager;

public class ListingSessionClientST extends BaseTestSelenium {

    private ServiceManager serviceManager = new ServiceManager();

    private static ProjectsPage projectsPage;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
    }

    @BeforeClass
    public static void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    @Test
    public void listProjectsForUserWithFirstSessionClientTest() throws Exception {
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(serviceManager.getUserService().getById(2));
        Pages.getTopNavigation().selectSessionClient(0);

        // user will see only projects page as this one is global
        projectsPage.goTo();
        int projectsInDatabase = serviceManager.getProjectService()
                .getByQuery("FROM Project AS p INNER JOIN p.users AS u WITH u.id = 2 INNER JOIN p.client AS c WITH c.id = 1").size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        exception.expect(IndexOutOfBoundsException.class);
        projectsPage.countListedTemplates();
    }

    @Test
    public void listProjectsForUserWithSecondSessionClientTest() throws Exception {
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().performLogin(serviceManager.getUserService().getById(2));
        Pages.getTopNavigation().selectSessionClient(1);

        // user will see first four tabs
        projectsPage.goTo();
        int projectsInDatabase = serviceManager.getProjectService()
                .getByQuery("FROM Project AS p INNER JOIN p.users AS u WITH u.id = 2 INNER JOIN p.client AS c WITH c.id = 2").size();
        int projectsDisplayed = projectsPage.countListedProjects();
        assertEquals("Displayed wrong number of projects", projectsInDatabase, projectsDisplayed);

        int templatesInDatabase = serviceManager.getTemplateService().getActiveTemplates().size();
        int templatesDisplayed = projectsPage.countListedTemplates();
        assertEquals("Displayed wrong number of templates", templatesInDatabase, templatesDisplayed);

        int workflowsInDatabase = serviceManager.getWorkflowService().getAll().size();
        int workflowsDisplayed = projectsPage.countListedWorkflows();
        assertEquals("Displayed wrong number of workflows", workflowsInDatabase, workflowsDisplayed);

        int docketsInDatabase = serviceManager.getDocketService().getAllForSelectedClient(2).size();
        int docketsDisplayed = projectsPage.countListedDockets();
        assertEquals("Displayed wrong number of dockets", docketsInDatabase, docketsDisplayed);

        exception.expect(IndexOutOfBoundsException.class);
        projectsPage.countListedRulesets();
    }
}
