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
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TopNavigationPage extends Page<TopNavigationPage> {

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
    @FindBy(className = "ui-selectonemenu-trigger")
    private WebElement clientSelectTrigger;

    @SuppressWarnings("unused")
    @FindBy(id = "select-session-client-form:setSessionClientButton")
    private WebElement acceptClientSelectionButton;

    @SuppressWarnings("unused")
    @FindBy(id = "select-session-client-form:cancelSessionClientSelectionButton")
    private WebElement cancelClientSelectionButton;

    public TopNavigationPage() {
        super(null);
    }

    @Override
    public TopNavigationPage goTo() {
        return null;
    }

    /**
     * Hovers user menu and logs out.
     */
    public void logout() throws Exception {
        await("Wait for visible user menu button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .untilTrue(new AtomicBoolean(userMenuButton.isDisplayed()));

        hoverWebElement(userMenuButton);
        hoverWebElement(logoutButton);

        clickButtonAndWaitForRedirect(logoutButton, Pages.getLoginPage().getUrl());
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

    public void acceptClientSelection() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(acceptClientSelectionButton, Pages.getDesktopPage().getUrl());
    }

    public void selectSessionClient()throws Exception {
        chooseFirstClient();
        acceptClientSelection();
        Thread.sleep(Browser.getDelayAfterLogin());
    }

    private void chooseFirstClient() throws InterruptedException {
        clientSelectTrigger.click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        Browser.getDriver().findElement(By.id("select-session-client-form:client_0")).click();
    }

    public void cancelClientSelection() {
        cancelClientSelectionButton.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to help page.
     */
    void gotoHelp() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkHelp);
        linkHelp.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to tasks page.
     */
     void gotoTasks() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkTasks);
        linkTasks.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to processes page.
     */
    void gotoProcesses() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkProcesses);
        linkProcesses.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to projects page.
     */
    void gotoProjects() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkProjects);
        linkProjects.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to users page.
     */
    void gotoUsers() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkUsers);
        linkUsers.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to modules page.
     */
    void gotoModules() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkModules);
        linkModules.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to system page.
     */
    void gotoSystem() {
        hoverWebElement(dashboardMenuButton);
        hoverWebElement(linkSystem);
        linkSystem.click();
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
