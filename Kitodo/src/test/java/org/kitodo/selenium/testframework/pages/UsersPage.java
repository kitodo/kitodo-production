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

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

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

    /**
     * Goes to users page.
     * 
     * @return The users page.
     */
    public UsersPage goTo() throws Exception {
        Pages.getTopNavigation().gotoUsers();
        return this;
    }

    /**
     * Checks if the browser is currently at users page.
     * 
     * @return True if browser is at users page.
     */
    public boolean isAt() {
        return Browser.getCurrentUrl().contains("users");
    }

    /**
     * Checks if the browser is currently not at users page.
     *
     * @return True if browser is not at users page.
     */
    public boolean isNotAt() {
        return !isAt();
    }

    /**
     * Counts rows of users table.
     * 
     * @return The number of rows of users table.
     */
    public int countListedUsers() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        List<WebElement> listOfRows = getRowsOfTable(usersTableData);
        return listOfRows.size();
    }

    /**
     * Counts rows of user groups table.
     *
     * @return The number of rows of user groups table.
     */
    public int countListedUserGroups() throws Exception {
        if (isNotAt()) {
            goTo();
            switchToTabByIndex(1);
        }
        List<WebElement> listOfRows = getRowsOfTable(userGroupsTable);
        return listOfRows.size();
    }

    /**
     * Goes to edit page for creating a new user.
     * 
     * @return The user edit page.
     */
    public UserEditPage createNewUser() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        newUserButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());

        return Pages.getUserEditPage();
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     * 
     * @return The users page.
     */
    public UsersPage switchToTabByIndex(int index) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        List<WebElement> listTabs = usersTabView.findElements(By.tagName("li"));
        WebElement tab = listTabs.get(index);
        tab.click();
        return this;
    }

    /**
     * Goes to edit page for creating a new user group.
     *
     * @return The user group edit page.
     */
    public UserGroupEditPage createNewUserGroup() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        newUserGroupButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());

        return Pages.getUserGroupEditPage();
    }

    /**
     * Goes to edit page for editing a given user group.
     * 
     * @param userGroup
     *            The user group.
     * @return The user group edit page.
     */
    public UserGroupEditPage editUserGroup(UserGroup userGroup) throws Exception {
        if (isNotAt()) {
            goTo();
            switchToTabByIndex(1);
        }

        WebElement userGroupEditLink = Browser.getDriver()
                .findElementByXPath("//a[@href='/kitodo/pages/usergroupEdit.jsf?id=" + userGroup.getId() + "']");
        userGroupEditLink.click();
        return Pages.getUserGroupEditPage();
    }

    /**
     * Goes to edit page for editing a given user group, specified by title.
     *
     * @param userGroupTitle
     *            The user group title.
     * @return The user group edit page.
     */
    public UserGroupEditPage editUserGroup(String userGroupTitle) throws Exception {
        if (isNotAt()) {
            goTo();
            switchToTabByIndex(1);
        }

        List<WebElement> tableRows = getRowsOfTable(userGroupsTable);

        for (WebElement tableRow : tableRows) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(userGroupTitle)) {
                WebElement userGroupEditLink = userGroupEditLink = tableRow.findElement(By.tagName("a"));
                userGroupEditLink.click();
                Thread.sleep(Browser.getDelayAfterLinkClick());
                return Pages.getUserGroupEditPage();
            }
        }
        throw new NoSuchElementException("No user group with given title was found: " + userGroupTitle);
    }

    /**
     * Returns a list of all user group titles which were displayed on user groups
     * page.
     * 
     * @return The list of user group titles
     */
    public List<String> getListOfUserGroupTitles() throws Exception {
        if (isNotAt()) {
            goTo();
            switchToTabByIndex(1);
        }
        return getTableDataByColumn(userGroupsTable, 0);
    }
}
