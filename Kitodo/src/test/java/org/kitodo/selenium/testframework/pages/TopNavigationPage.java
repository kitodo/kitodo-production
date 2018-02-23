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

import static org.kitodo.selenium.testframework.Browser.hoverWebElement;

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

    public void logout() throws InterruptedException {
        hoverWebElement(userMenu);
        hoverWebElement(logoutButton);
        logoutButton.click();
        Thread.sleep(Browser.getDelayAfterLogout());
    }

    public void gotoHelp() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkHelp);
        linkHelp.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoTasks() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkHelp);
        linkTasks.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoProcesses() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkProcesses);
        linkProcesses.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoProjects() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkProjects);
        linkProjects.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoUsers() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkUsers);
        linkUsers.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoModules() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkModules);
        linkModules.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoClients() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkClients);
        linkClients.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }

    public void gotoIndexing() throws InterruptedException {
        hoverWebElement(dashboardMenu);
        hoverWebElement(linkIndexing);
        linkIndexing.click();
        Thread.sleep(Browser.getDelayAfterLinkClick());
    }
}
