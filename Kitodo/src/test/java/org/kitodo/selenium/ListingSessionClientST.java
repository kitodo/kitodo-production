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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProjectsPage;

public class ListingSessionClientST extends BaseTestSelenium {

    private static ProjectsPage projectsPage;

    @BeforeAll
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getById(2));
    }

    @AfterEach
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
        assertEquals(projectsInDatabase, projectsDisplayed, "Displayed wrong number of projects");

        assertThrows(IndexOutOfBoundsException.class, () -> projectsPage.countListedTemplates());

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
        assertEquals(projectsInDatabase, projectsDisplayed, "Displayed wrong number of projects");

        // TODO: rewrite query for active templates
        //int templatesInDatabase = ServiceManager.getTemplateService().getActiveTemplates().size();
        int templatesDisplayed = projectsPage.countListedTemplates();
        assertEquals(2, templatesDisplayed, "Displayed wrong number of templates");

        int workflowsInDatabase = ServiceManager.getWorkflowService().countResults(null).intValue();
        int workflowsDisplayed = projectsPage.countListedWorkflows();
        assertEquals(workflowsInDatabase, workflowsDisplayed, "Displayed wrong number of workflows");

        int docketsInDatabase = ServiceManager.getDocketService().getAllForSelectedClient().size();
        int docketsDisplayed = projectsPage.countListedDockets();
        assertEquals(docketsInDatabase, docketsDisplayed, "Displayed wrong number of dockets");

        assertThrows(IndexOutOfBoundsException.class, () -> projectsPage.countListedRulesets());

        SecurityTestUtils.cleanSecurityContext();
    }
}
