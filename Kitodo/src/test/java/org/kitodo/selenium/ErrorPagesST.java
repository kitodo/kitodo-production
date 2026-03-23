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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;

/**
 * Selenium tests for verifying error page handling in the Kitodo application.
 */
public class ErrorPagesST extends BaseTestSelenium {

    /**
     * Login before every test.
     *
     * @throws Exception
     *             when page navigation fails
     */
    @BeforeEach
    public void doLogin() throws Exception {
        Pages.getLoginPage().goTo().performLoginAsAdmin();
    }

    /**
     * Logout after every test.
     *
     * @throws Exception
     *             when page navigation fails
     */
    @AfterEach
    public void doLogout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    /**
     * Verifies that navigating to a non-existing page forwards to the 404 error page.
     */
    @Test
    public void shouldShow404PageForUnknownUrl() {
        Browser.goTo("foobar");
        await("Wait for visible 404 image").untilAsserted(
                () -> assertTrue(Browser.getDriver().findElement(By.id("pageNotFoundImage")).isDisplayed()));
        assertTrue(Browser.getCurrentUrl().endsWith("foobar"),
                "The URL should remain at the invalid address after forwarding to the 404 error page");
    }

}
