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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.ProjectEditPage;
import org.kitodo.selenium.testframework.pages.ProjectsPage;

public class UIInteractionsST extends BaseTestSelenium {

    private static ProjectsPage projectsPage;

    @BeforeAll
    public static void setup() throws Exception {
        projectsPage = Pages.getProjectsPage();
    }

    @BeforeEach
    public void login() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
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
}
