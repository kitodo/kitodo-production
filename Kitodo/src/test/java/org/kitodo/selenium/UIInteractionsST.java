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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
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
     * Verifies that roles are filtered by current client by default and that activating the roles filter switch
     * shows
     *
     * @throws Exception when navigating to the roles tab of the users pages fails.
     */
    @Test
    public void filterRolesByCurrentClientTest() throws Exception {
        User nowak = ServiceManager.getUserService().getByLogin("nowak");
        List<Role> existingRoles = augmentUserWithAllExistingRolesOfAssignedClients(nowak);

        Pages.getLoginPage().goTo().performLogin(nowak);
        usersPage.goTo();

        int rolesInDatabase = ServiceManager.getRoleService().getAll().size();

        usersPage.switchToTabByIndex(TabIndex.ROLES.getIndex());

        // verify that switch to show other clients roles is off initially
        WebElement roleSwitch = Browser.getDriver().findElement(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR));
        assertFalse(roleSwitch.getAttribute("class").contains("ui-state-active"));

        int rolesDisplayedUnfiltered = usersPage.countListedRoles();
        assertEquals(8, rolesDisplayedUnfiltered, "Displayed wrong number of roles unfiltered");

        Browser.getDriver().findElement(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR)).click();

        // verify that switch to show other clients roles is on after clicking
        WebElement roleSwitchClicked = Browser.getDriver().findElement(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR));
        assertTrue(roleSwitchClicked.getAttribute("class").contains("ui-state-active"));

        int rolesDisplayedFiltered = usersPage.countListedRoles();
        assertEquals(rolesInDatabase, rolesDisplayedFiltered, "Displayed wrong number of roles filtered");

        // reset test user "nowak" to original state
        nowak.setRoles(existingRoles);
        ServiceManager.getUserService().save(nowak);
    }

    /**
     * Verifies that switch to show other clients roles is not available for users with just one client.
     *
     * @throws Exception when navigating to the roles tab of the users page fails.
     */
    @Test
    public void roleSwitchUnavailableTest() throws Exception {
        Pages.getLoginPage().goTo().performLogin(ServiceManager.getUserService().getByLogin("kowal"));
        usersPage.goTo();
        usersPage.switchToTabByIndex(TabIndex.ROLES.getIndex());
        List<WebElement> roleSwitches = Browser.getDriver().findElements(By.cssSelector(FILTER_ROLES_SWITCH_SELECTOR));
        assertTrue(roleSwitches.isEmpty(), "Role filter switch should be unavailable for users with just one client");
    }

    private List<Role> augmentUserWithAllExistingRolesOfAssignedClients(User user) throws Exception {
        List<Role> existingRoles = new ArrayList<>(user.getRoles());
        user.setRoles(new ArrayList<>());
        for (Role role : ServiceManager.getRoleService().getAll()) {
            if (user.getClients().contains(role.getClient())) {
                user.getRoles().add(role);
            }
        }
        user.setDefaultClient(user.getClients().get(0));
        ServiceManager.getUserService().save(user);
        return existingRoles;
    }
}
