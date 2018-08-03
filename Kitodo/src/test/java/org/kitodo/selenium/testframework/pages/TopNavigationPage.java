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

public class TopNavigationPage {

    @SuppressWarnings("unused")
    @FindBy(id = "user-menu")
    private WebElement userMenuButton;

    @SuppressWarnings("unused")
    @FindBy(id = "logout-form:logout")
    private WebElement logoutButton;

    @SuppressWarnings("unused")
    @FindBy(id = "dashboard-menu")
    private WebElement dashboardMenuButton;

    @SuppressWarnings("unused")
    @FindBy(id = "dashboard-menu-header")
    private WebElement dashboardMenuHeader;

    @SuppressWarnings("unused")
    @FindBy(id = "linkTasks")
    private WebElement linkTasks;

    @SuppressWarnings("unused")
    @FindBy(id = "linkProcesses")
    private WebElement linkProcesses;

    @SuppressWarnings("unused")
    @FindBy(id = "linkProjects")
    private WebElement linkProjects;

    @SuppressWarnings("unused")
    @FindBy(id = "linkUsers")
    private WebElement linkUsers;

    @SuppressWarnings("unused")
    @FindBy(id = "linkModules")
    private WebElement linkModules;

    @SuppressWarnings("unused")
    @FindBy(id = "linkClients")
    private WebElement linkClients;

    @SuppressWarnings("unused")
    @FindBy(id = "linkHelp")
    private WebElement linkHelp;

    @SuppressWarnings("unused")
    @FindBy(id = "linkSystem")
    private WebElement linkSystem;

    @SuppressWarnings("unused")
    @FindBy(id = "select-session-client-form:setSessionClientButton")
    private WebElement acceptClientSelectionButton;

    @SuppressWarnings("unused")
    @FindBy(id = "select-session-client-form:cancelSessionClientSelectionButton")
    private WebElement cancelClientSelectionButton;

    /**
     * Hovers user menu and logs out.
     */
    public void logout() throws InterruptedException {
        await("Wait for visible user menu button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .untilTrue(new AtomicBoolean(userMenuButton.isDisplayed()));

        hoverWebElement(userMenuButton);
        hoverWebElement(logoutButton);
        logoutButton.click();
        Thread.sleep(Browser.getDelayAfterLogout());
    }

    public String getSessionClient() throws InterruptedException {
        await("Wait for visible user menu button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
            .untilTrue(new AtomicBoolean(userMenuButton.isDisplayed()));

        hoverWebElement(userMenuButton);
        if (!logoutButton.isDisplayed()) {
            userMenuButton.click();
            Thread.sleep(Browser.getDelayAfterHoverMenu());
        }
        WebElement element = Browser.getDriver().findElementById("sessionClient").findElement(By.tagName("b"));
        return element.getText();
    }

    public void acceptClientSelection() throws InterruptedException {
        acceptClientSelectionButton.click();
        Thread.sleep(5000);
    }

    public void cancelClientSelection() {
        cancelClientSelectionButton.click();
    }

    public boolean isClientSelectionPossible() {
        return acceptClientSelectionButton.isEnabled();
    }

    /**
     * Hovers dashboard menu and clicks on link to help page
     */
    void gotoHelp() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkHelp);
        linkHelp.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to tasks page
     */
     void gotoTasks() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkTasks);
        linkTasks.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to processes page
     */
    void gotoProcesses() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkProcesses);
        linkProcesses.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to projects page
     */
    void gotoProjects() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkProjects);
        linkProjects.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to users page
     */
    void gotoUsers() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkUsers);
        linkUsers.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to modules page
     */
    void gotoModules() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkModules);
        linkModules.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to clients page
     */
    void gotoClients() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkClients);
        linkClients.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and clicks on link to system page
     */
    void gotoSystem() throws InterruptedException {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkSystem);
        linkSystem.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    /**
     * Hovers dashboard menu and checks menu header if all buttons are displayed.
     * 
     * @return True if "Admin" is displayed.
     */
    public boolean isShowingAllLinks() {
        hoverWebElement(dashboardMenuButton);
        if (!linkHelp.isDisplayed()) {
            return false;
        }
        if (!linkSystem.isDisplayed()) {
            return false;
        }
        if (!linkProjects.isDisplayed()) {
            return false;
        }
        if (!linkTasks.isDisplayed()) {
            return false;
        }
        if (!linkUsers.isDisplayed()) {
            return false;
        }
        if (!linkProcesses.isDisplayed()) {
            return false;
        }
        return true;
    }
}
