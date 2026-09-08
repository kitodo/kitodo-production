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

import java.time.Duration;
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

    @SuppressWarnings(UNUSED)
    @FindBy(id = "user-menu")
    private WebElement userMenuButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "logout-form:logout")
    private WebElement logoutButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "dashboard-menu")
    private WebElement dashboardMenuButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "dashboard-menu-header")
    private WebElement dashboardMenuHeader;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkTasks")
    private WebElement linkTasks;

    @SuppressWarnings(UNUSED)
    @FindBy(id = LINK_PROCESSES_ID)
    private WebElement linkProcesses;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkProjects")
    private WebElement linkProjects;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkUsers")
    private WebElement linkUsers;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkModules")
    private WebElement linkModules;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkClients")
    private WebElement linkClients;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkHelp")
    private WebElement linkHelp;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "linkSystem")
    private WebElement linkSystem;

    @SuppressWarnings(UNUSED)
    @FindBy(className = "ui-selectonemenu-trigger")
    private WebElement clientSelectTrigger;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "select-session-client-form:setSessionClientButton")
    private WebElement acceptClientSelectionButton;

    @SuppressWarnings(UNUSED)
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
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        webDriverWait.until(ExpectedConditions.urlContains(Pages.getLoginPage().getUrl()));
    }

    public String getSessionClient() throws InterruptedException{
        await("Wait for visible user menu button").atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> userMenuButton.isDisplayed());

        userMenuButton.click();
        WebElement element = Browser.getDriver().findElement(By.id("sessionClient")).findElement(By.tagName("b"));
        return element.getText();
    }

    public void acceptClientSelection() throws ReflectiveOperationException {
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
        clickNavigationLink("linkHelp");
    }

    /**
     * Hovers dashboard menu and clicks on link to tasks page.
     */
    void gotoTasks() throws InterruptedException {
        Thread.sleep(Browser.getDelayAfterDelete());
        clickNavigationLink("linkTasks");
    }

    /**
     * Hovers dashboard menu and clicks on link to processes page.
     */
    void gotoProcesses() {
        clickNavigationLink(LINK_PROCESSES_ID);
    }

    /**
     * Hovers dashboard menu and clicks on link to projects page.
     */
    void gotoProjects() {
        clickNavigationLink("linkProjects");
    }

    /**
     * Hovers dashboard menu and clicks on link to users page.
     */
    void gotoUsers() {
        clickNavigationLink("linkUsers");
    }

    /**
     * Hovers dashboard menu and clicks on link to system page.
     */
    void gotoSystem() {
        clickNavigationLink("linkSystem");
    }

    /**
     * Waits until the navigation link with the given ID is present in the DOM and clicks it.
     *
     * @param linkId ID of the navigation link
     */
    private void clickNavigationLink(String linkId) {
        RemoteWebDriver driver = Browser.getDriver();
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id(linkId)));
        driver.executeScript(ARGUMENTS_CLICK, driver.findElement(By.id(linkId)));
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
