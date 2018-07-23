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

import org.kitodo.data.database.beans.User;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UserEditPage extends Page<UserEditPage> {
    @SuppressWarnings("unused")
    @FindBy(id = "editForm:save")
    private WebElement saveUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:firstName")
    private WebElement firstNameInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:surname")
    private WebElement lastNameInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:login")
    private WebElement loginInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:password")
    private WebElement passwordInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:location")
    private WebElement locationInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:metaDataLanguage")
    private WebElement metaDataLanguageInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView")
    private WebElement userEditTabView;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:userTabView:addUserGroupButton")
    private WebElement addUserToGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = "userGroupForm:selectUserGroupTable_data")
    private WebElement selectUserGroupTable;

    @SuppressWarnings("unused")
    @FindBy(id = "addUserGroupDialog")
    private WebElement addToUserGroupDialog;

    public UserEditPage() {
        super("pages/userEdit.jsf");
    }

    @Override
    public UserEditPage goTo() {
        return null;
    }

    public UserEditPage insertUserData(User user) {
        firstNameInput.sendKeys(user.getName());
        lastNameInput.sendKeys(user.getSurname());
        loginInput.sendKeys(user.getLogin());
        passwordInput.sendKeys(user.getPassword());
        locationInput.sendKeys(user.getLocation());
        metaDataLanguageInput.sendKeys(user.getMetadataLanguage());
        return this;
    }

    public UsersPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveUserButton, Pages.getUsersPage().getUrl());
        return Pages.getUsersPage();
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @return The users page.
     */
    public UserEditPage switchToTabByIndex(int index) throws Exception {
        return switchToTabByIndex(index, userEditTabView);
    }

    public UserEditPage addUserToUserGroup(String userGroupTitle) {
        addUserToGroupButton.click();
        List<WebElement> tableRows = Browser.getRowsOfTable(selectUserGroupTable);
        for (WebElement tableRow : tableRows) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(userGroupTitle)) {
                clickLinkOfTableRow(tableRow);
                Browser.closeDialog(addToUserGroupDialog);
                return this;
            }
        }
        throw new NoSuchElementException("No user group with given title was found: " + userGroupTitle);
    }

    private void clickLinkOfTableRow(WebElement tableRow) {
        WebElement link = tableRow.findElement(By.tagName("a"));
        link.click();
    }

}
