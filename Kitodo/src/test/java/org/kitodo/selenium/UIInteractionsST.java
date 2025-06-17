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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;
import org.kitodo.selenium.testframework.pages.UsersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class UIInteractionsST extends BaseTestSelenium {

    private static ProjectsPage projectsPage;
    private static UsersPage usersPage;
    private static final String FILTER_ROLES_SWITCH_SELECTOR = "#allClientsRolesForm\\:showAllClientsRoles > .ui-chkbox-box";

    @BeforeAll
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
        usersPage = Pages.getUsersPage();
    }

    @AfterEach
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
        if (Browser.isAlertPresent()) {
            Browser.getDriver().switchTo().alert().accept();
        }
    }

    @Test
    public void editProjectInputSaveButtonTest () throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
        final String newProjectTitle = "newTitle";

        ProjectEditPage projectEditPage = projectsPage.editProject();

        // Check if save button is initially disabled
        assertFalse(projectEditPage.isSaveButtonEnabled(), "Save button should be disabled");

        // Check if save button is enabled after changing input
        projectEditPage.changeTitleKeepFocus(newProjectTitle);
        await("Wait for save button to be enabled").pollDelay(700, TimeUnit.MILLISECONDS).atMost(10, TimeUnit.SECONDS)
                .until(projectEditPage::isSaveButtonEnabled);

        // Check if save button can be clicked on first attempt
        projectEditPage.saveOnce();
        assertTrue(projectsPage.isAt(), "Browser should be at projects page");
    }

    /**
     * Verifies that roles are unfiltered by default and that deactivating the roles filter switch
     * filters the roles to the ones assigned to the current client.
     *
     * @throws Exception when navigating to the roles tab of the users pages fails.
     */
    @Test
    public void filterRolesByCurrentClientTest() throws Exception {
        Pages.getLoginPage().performLoginAsAdmin();
        usersPage.goTo();

        int rolesInDatabase = ServiceManager.getRoleService().getAll().size();

        usersPage.switchToTabByIndex(TabIndex.ROLES.getIndex());

        // verify that switch to show other clients roles is on initially
        WebElement roleSwitch = Browser.getDriver().findElement(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR));
        assertTrue(roleSwitch.getAttribute("class").contains("ui-state-active"));

        int rolesDisplayedFiltered = usersPage.countListedRoles();
        assertEquals(rolesInDatabase, rolesDisplayedFiltered, "Displayed wrong number of roles filtered");

        Browser.getDriver().findElement(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR)).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());

        // verify that switch to show other clients roles is off after clicking
        WebElement roleSwitchClicked = Browser.getDriver().findElement(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR));
        pollAssertTrue(() -> !roleSwitchClicked.getAttribute("class").contains("ui-state-active"));

        int rolesDisplayedUnfiltered = usersPage.countListedRoles();
        assertEquals(9, rolesDisplayedUnfiltered, "Displayed wrong number of roles unfiltered");
    }

    /**
     * Verifies that switch to show other clients roles is not available for users with just one client.
     *
     * @throws Exception when navigating to the roles tab of the users page fails.
     */
    @Test
    public void roleSwitchUnavailableTest() throws Exception {
        String setClientId = "select-session-client-form:setSessionClientButton";
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getByLogin("nowak"));
        pollAssertTrue(() -> Browser.getDriver().findElement(By.id(setClientId)).isDisplayed());
        Browser.getDriver().findElement(By.id(setClientId)).click();
        usersPage.goTo();
        usersPage.switchToTabByIndex(TabIndex.ROLES.getIndex());
        List<WebElement> roleSwitches = Browser.getDriver().findElements(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR));
        assertTrue(roleSwitches.isEmpty(), "Role filter switch should be unavailable for users without the "
                + "corresponding global permission to see roles of all clients");
    }

    private void pollAssertTrue(Callable<Boolean> conditionEvaluator) {
        await().ignoreExceptions()
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(conditionEvaluator);
    }
}
