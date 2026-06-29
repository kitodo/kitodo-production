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

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.pages.LoginPage;
import org.openqa.selenium.By;

public class LoginST extends BaseTestSelenium {
    
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
        assertFalse(csrfToken.isEmpty());

        // modify CSRF token to something invalid
        Browser.getDriver().executeScript("$('input[name=\"_csrf\"]').val('abc');");

        // fail at logging in
        loginPage.performLoginAsAdmin();

        // make sure we are still at the login page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getLoginPage().isAt());

        // retrieve CSRF token again 
        final String csrfTokeAfterFailure = (String) Browser.getDriver().executeScript("return $('input[name=\"_csrf\"]').val();");

        // check that CSRF token has changed (due to reload)
        assertNotEquals("abc", csrfTokeAfterFailure);
        assertNotEquals(csrfToken, csrfTokeAfterFailure);

        // do successful login 
        loginPage.performLoginAsAdmin();

        // make sure this is the desktop page
        await().ignoreExceptions().pollDelay(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .until(() -> Pages.getDesktopPage().isAt());

        // logout
        Pages.getTopNavigation().logout();
    }

}
