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

package org.kitodo.selenium.testframework.pages;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.hoverWebElement;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UserConfigurationPage extends Page<UserConfigurationPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "user-menu")
    private WebElement userMenuButton;

    @SuppressWarnings("unused")
    @FindBy(partialLinkText = "Benutzerdaten & Einstellungen")
    private WebElement userConfigButton;

    @SuppressWarnings("unused")
    @FindBy(id = "userConfigurationTabView:userConfigForm:table-size")
    private WebElement tableSizeInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userConfigurationTabView:userConfigForm:metadata-language")
    private WebElement metadataLanguageInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userConfigurationTabView:userConfigForm:submit")
    private WebElement saveConfigurationButton;

    public UserConfigurationPage() {
        super("pages/userConfiguration.jsf");
    }

    @Override
    public UserConfigurationPage goTo() {
        return null;
    }

    public void changeUserSettings() {
        openUserConfig();
        tableSizeInput.clear();
        tableSizeInput.sendKeys("50");
        metadataLanguageInput.clear();
        metadataLanguageInput.sendKeys("en");
        Browser.getDriver().findElements(By.cssSelector(".ui-selectonemenu-trigger")).get(1).click();
        Browser.getDriver().findElement(By.id("userConfigurationTabView:userConfigForm:languages_1")).click();
        saveConfigurationButton.click();
    }

    private void openUserConfig() {
        await("Wait for visible user menu button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .untilTrue(new AtomicBoolean(userMenuButton.isDisplayed()));

        hoverWebElement(userMenuButton);
        hoverWebElement(userConfigButton);
        userConfigButton.click();
    }
}
