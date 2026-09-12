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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.security.password.KitodoDelegatingPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.UserService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.LoginPage;
import org.openqa.selenium.By;

public class LoginST extends BaseTestSelenium {

    private static final String LEGACY_TEST_PASSWORD_ENCODED = "OvEJ00yyYZQ=";
    private static final UserService userService = ServiceManager.getUserService();
    
    /**
     * Tests that login as admin is successful.
     */
    @Test
    public void testSuccessfulLogin() throws Exception {
        LoginPage loginPage = Pages.getLoginPage();
        loginPage.goTo();

        // make sure login button is visible
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Browser.getDriver().findElement(By.id("login")).isDisplayed());
        
        loginPage.performLoginAsAdmin();

        // make sure this is the desktop page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getDesktopPage().isAt());

        // logout
        Pages.getTopNavigation().logout();
    }

    /**
     * Tests that login with modified CSRF token is not successful.
     */
    @Test
    public void testFailedCsrfLogin() throws Exception {
        LoginPage loginPage = Pages.getLoginPage();
        loginPage.goTo();

        // make sure login button is visible
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Browser.getDriver().findElement(By.id("login")).isDisplayed());

        // retrieve CSRF token
        final String csrfToken = (String) Browser.getDriver().executeScript("return $('input[name=\"_csrf\"]').val();");

        // check that CSRF token is not empty
        assertNotNull(csrfToken);
        assertFalse(csrfToken.isBlank());

        // modify CSRF token to something invalid
        Browser.getDriver().executeScript("$('input[name=\"_csrf\"]').val('abc');");

        // fail at logging in
        loginPage.performLoginAsAdmin();

        // make sure we are still at the login page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getLoginPage().isAt());

        // retrieve CSRF token again 
        final String csrfTokenAfterFailure = (String) Browser.getDriver().executeScript("return $('input[name=\"_csrf\"]').val();");

        // check that CSRF token has changed (due to reload)
        assertNotEquals("abc", csrfTokenAfterFailure);
        assertNotEquals(csrfToken, csrfTokenAfterFailure);

        // do successful login 
        loginPage.performLoginAsAdmin();

        // make sure this is the desktop page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getDesktopPage().isAt());

        // logout
        Pages.getTopNavigation().logout();
    }

    /**
     * Checks that a legacy password is upgraded to a new password upon user login.
     */
    @Test
    public void userPasswordShouldUpgradeOnLoginTest() throws Exception {
        // overwrite user password with legacy encoding
        User user = userService.getById(1);
        user.setPassword(LEGACY_TEST_PASSWORD_ENCODED);
        userService.save(user);

        // check login with legacy encoding is possible
        Pages.getLoginPage().goTo().performLoginAsAdmin();

        // wait for desktop page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getDesktopPage().isAt());

        // check password was upgraded and is still valid
        KitodoDelegatingPasswordEncoder encoder = new KitodoDelegatingPasswordEncoder();
        user = userService.getById(1);
        assertTrue(user.getPassword().startsWith("{argon2}"));
        assertTrue(encoder.matches(MockDatabase.DEFAULT_USER_PASSWORD, user.getPassword()));

        // logout and login again to verify
        Pages.getTopNavigation().logout();
        Pages.getLoginPage().goTo().performLoginAsAdmin();

        // wait for desktop page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getDesktopPage().isAt());

        // final logout
        Pages.getTopNavigation().logout();
    }

}
