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

import java.util.List;

import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UsersPage {

    @SuppressWarnings("unused")
    @FindBy(id = "usersTabView")
    private WebElement usersTabView;

    @SuppressWarnings("unused")
    @FindBy(id = "usersTabView:usersTable_data")
    private WebElement usersTableData;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newUserButton")
    private WebElement newUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newUserGroupButton")
    private WebElement newUserGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newLdapGroupButton")
    private WebElement newLdapGroupButton;

    public UsersPage goTo() throws Exception {
        Pages.getTopNavigation().gotoUsers();
        Thread.sleep(200);
        return this;
    }

    public boolean isAt() throws InterruptedException {
        return Browser.getCurrentUrl().contains("users");
    }

    public int countListedUsers() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listOfRows = usersTableData.findElements(By.tagName("tr"));
        return listOfRows.size();
    }

    public UserEditPage goToUserEditPage() throws Exception {
        if (!isAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(400);
        newUserButton.click();
        Thread.sleep(400);

        return Pages.getUserEditPage();
    }

    public UsersPage switchToUsersTab() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listTabs = usersTabView.findElements(By.tagName("li"));
        WebElement userGroupTab = listTabs.get(0);
        userGroupTab.click();
        return this;
    }

    public UsersPage switchToUserGroupsTab() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listTabs = usersTabView.findElements(By.tagName("li"));
        WebElement userGroupTab = listTabs.get(1);
        userGroupTab.click();
        return this;
    }

    public UsersPage switchToLdapGrousTab() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listTabs = usersTabView.findElements(By.tagName("li"));
        WebElement userGroupTab = listTabs.get(2);
        userGroupTab.click();
        return this;
    }

    public UserGroupEditPage goToUserGroupEditPage() throws Exception {
        if (!isAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(400);
        newUserGroupButton.click();
        Thread.sleep(400);

        return Pages.getUserGroupEditPage();
    }

    public UserGroupEditPage goToUserGroupEditPage(UserGroup userGroup) throws Exception {
        if (!isAt()) {
            goTo();
            switchToUserGroupsTab();
        }

        WebElement userGroupEditLink = Browser.getDriver()
                .findElementByXPath("//a[@href='/kitodo/pages/usergroupEdit.jsf?id=" + userGroup.getId() + "']");
        userGroupEditLink.click();
        return Pages.getUserGroupEditPage();
    }

}
