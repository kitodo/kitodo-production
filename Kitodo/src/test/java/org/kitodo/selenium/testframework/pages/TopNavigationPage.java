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

import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TopNavigationPage extends Page<TopNavigationPage> {

    private static final String ARGUMENTS_CLICK = "arguments[0].click()";
    private static final String LINK_PROCESSES_ID = "linkProcessesNavigationForm:linkProcesses";

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
    @FindBy(id = LINK_PROCESSES_ID)
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
                .until(() -> userMenuButton.isDisplayed());
        RemoteWebDriver driver = Browser.getDriver();
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id("logout-form:logout")));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 60);
        webDriverWait.until(ExpectedConditions.urlContains(Pages.getLoginPage().getUrl()));
    }

    public String getSessionClient() throws InterruptedException {
        await("Wait for visible user menu button").atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> userMenuButton.isDisplayed());

        userMenuButton.click();
        WebElement element = Browser.getDriver().findElementById("sessionClient").findElement(By.tagName("b"));
        return element.getText();
    }

    public void acceptClientSelection() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(acceptClientSelectionButton, Pages.getDesktopPage().getUrl());
    }

    public void selectSessionClient(int id)throws Exception {
        chooseClient(id);
        acceptClientSelection();
        Thread.sleep(Browser.getDelayAfterLogin());
    }

    private void chooseClient(int id) throws InterruptedException {
        clientSelectTrigger.click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        Browser.getDriver().findElement(By.id("select-session-client-form:client_" + id)).click();
    }

    public void cancelClientSelection() {
        cancelClientSelectionButton.click();
    }

    /**
     * Hovers dashboard menu and clicks on link to help page.
     */
    void gotoHelp() {
        RemoteWebDriver driver = Browser.getDriver();
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id("linkHelp")));
    }

    /**
     * Hovers dashboard menu and clicks on link to tasks page.
     */
    void gotoTasks() throws InterruptedException {
        RemoteWebDriver driver = Browser.getDriver();
        Thread.sleep(Browser.getDelayAfterDelete());
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id("linkTasks")));
    }

    /**
     * Hovers dashboard menu and clicks on link to processes page.
     */
    void gotoProcesses() {
        RemoteWebDriver driver = Browser.getDriver();
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id(LINK_PROCESSES_ID)));
    }

    /**
     * Hovers dashboard menu and clicks on link to projects page.
     */
    void gotoProjects() {
        RemoteWebDriver driver = Browser.getDriver();
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id("linkProjects")));
    }

    /**
     * Hovers dashboard menu and clicks on link to users page.
     */
    void gotoUsers() {
        RemoteWebDriver driver = Browser.getDriver();
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id("linkUsers")));
    }

    /**
     * Hovers dashboard menu and clicks on link to system page.
     */
    void gotoSystem() {
        RemoteWebDriver driver = Browser.getDriver();
        ((JavascriptExecutor) driver).executeScript(ARGUMENTS_CLICK, driver.findElement(By.id("linkSystem")));
    }

    /**
     * Clicks dashboard menu and checks menu header if all buttons are displayed.
     *
     * @return True if "Admin" is displayed.
     */
    public boolean isShowingAllLinks() {
        dashboardMenuButton.click();
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
        return linkProcesses.isDisplayed();
    }
}
