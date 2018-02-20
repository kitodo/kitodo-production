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

import java.util.ArrayList;
import java.util.List;

import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
    @FindBy(id = "usersTabView:userGroupsTable_data")
    private WebElement userGroupsTable;

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

    public int countListedUserGroups() throws Exception {
        if (!isAt()) {
            goTo();
            switchToUserGroupsTab();
        }
        List<WebElement> listOfRows = userGroupsTable.findElements(By.tagName("tr"));
        return listOfRows.size();
    }

    public UserEditPage createNewUser() throws Exception {
        if (!isAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        newUserButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());

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

    public UsersPage switchToLdapGroupsTab() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listTabs = usersTabView.findElements(By.tagName("li"));
        WebElement userGroupTab = listTabs.get(2);
        userGroupTab.click();
        return this;
    }

    public UserGroupEditPage createNewUserGroup() throws Exception {
        if (!isAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        newUserGroupButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());

        return Pages.getUserGroupEditPage();
    }

    public UserGroupEditPage editUserGroup(UserGroup userGroup) throws Exception {
        if (!isAt()) {
            goTo();
            switchToUserGroupsTab();
        }

        WebElement userGroupEditLink = Browser.getDriver()
                .findElementByXPath("//a[@href='/kitodo/pages/usergroupEdit.jsf?id=" + userGroup.getId() + "']");
        userGroupEditLink.click();
        return Pages.getUserGroupEditPage();
    }

    public UserGroupEditPage editUserGroup(String userGroupTitle) throws Exception {
        if (!isAt()) {
            goTo();
            switchToUserGroupsTab();
        }

        WebElement userGroupEditLink = null;

        List<WebElement> tableRows = getRowsOfTable(userGroupsTable);

        for (WebElement tableRow : tableRows) {
            if (getCellDataByRow(tableRow, 0).equals(userGroupTitle)) {
                userGroupEditLink = tableRow.findElement(By.tagName("a"));
                userGroupEditLink.click();
                Thread.sleep(Browser.getDelayAfterLinkClick());
                return Pages.getUserGroupEditPage();
            }
        }
        throw new NoSuchElementException("No user group with given title was found: " + userGroupTitle);
    }

    public List<String> getListOfUserGroupTitles() throws Exception {
        if (!isAt()) {
            goTo();
            switchToUserGroupsTab();
        }
        return getTableDataByColumn(userGroupsTable, 0);
    }

    private List<WebElement> getRowsOfTable(WebElement table) {
        return table.findElements(By.tagName("tr"));
    }

    private List<WebElement> getCellsOfRow(WebElement row) {
        return row.findElements(By.tagName("td"));
    }

    private List<String> getTableDataByColumn(WebElement table, int columnIndex) {
        List<WebElement> rows = getRowsOfTable(table);
        List<String> data = new ArrayList<>();
        for (WebElement row : rows) {
            data.add(getCellDataByRow(row, columnIndex));
        }
        return data;
    }

    private String getCellDataByRow(WebElement row, int columnIndex) {
        List<WebElement> cells = getCellsOfRow(row);
        return cells.get(columnIndex).getText();
    }
}
