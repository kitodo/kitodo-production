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

import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TopNavigationPage {

    @SuppressWarnings("unused")
    @FindBy(id = "user-menu")
    private WebElement userMenu;

    @SuppressWarnings("unused")
    @FindBy(id = "logout-form:logout")
    private WebElement logoutButton;

    @SuppressWarnings("unused")
    @FindBy(id = "dashboard-menu")
    private WebElement dashboardMenu;

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
    @FindBy(id = "linkIndexing")
    private WebElement linkIndexing;

    private void hoverWebElement(WebElement webElement) throws InterruptedException {
        Browser.getActions().moveToElement(webElement).pause(400).build().perform();
    }

    public void logout() throws InterruptedException {
        hoverWebElement(userMenu);
        logoutButton.click();
        Thread.sleep(1000);
    }

    public void gotoHelp() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkHelp.click();
    }

    public void gotoTasks() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkTasks.click();
    }

    public void gotoProcesses() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkProcesses.click();
    }

    public void gotoProjects() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkProjects.click();
    }

    public void gotoUsers() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkUsers.click();
    }

    public void gotoModules() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkModules.click();
    }

    public void gotoClients() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkClients.click();
    }

    public void gotoIndexing() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        linkIndexing.click();
    }
}
