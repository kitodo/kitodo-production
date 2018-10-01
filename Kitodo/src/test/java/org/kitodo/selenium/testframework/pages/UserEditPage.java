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
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UserEditPage extends Page<UserEditPage> {

    private static final String EDIT_FORM = "editForm";
    private static final String USER_TAB_VIEW = EDIT_FORM + ":userTabView";

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":save")
    private WebElement saveUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW)
    private WebElement userEditTabView;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":firstName")
    private WebElement firstNameInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":surname")
    private WebElement lastNameInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":login")
    private WebElement loginInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":password")
    private WebElement passwordInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":location")
    private WebElement locationInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":metaDataLanguage")
    private WebElement metaDataLanguageInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + "addUserGroupButton")
    private WebElement addUserToGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = USER_TAB_VIEW + ":addClientButton")
    private WebElement addUserToClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "userGroupForm:selectUserGroupTable_data")
    private WebElement selectUserGroupTable;

    @SuppressWarnings("unused")
    @FindBy(id = "userClientForm:selectClientTable_data")
    private WebElement selectClientTable;

    @SuppressWarnings("unused")
    @FindBy(id = "addUserGroupDialog")
    private WebElement addToUserGroupDialog;

    @SuppressWarnings("unused")
    @FindBy(id = "addClientDialog")
    private WebElement addToClientDialog;

    public UserEditPage() {
        super("pages/userEdit.jsf");
    }

    @Override
    public UserEditPage goTo() {
        return null;
    }

    public UserEditPage insertUserData(User user) {
        passwordInput.sendKeys(user.getPassword());
        firstNameInput.sendKeys(user.getName());
        lastNameInput.sendKeys(user.getSurname());
        loginInput.sendKeys(user.getLogin());
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

    public UserEditPage addUserToClient(String clientName) {
        addUserToClientButton.click();
        List<WebElement> tableRows = Browser.getRowsOfTable(selectClientTable);
        for (WebElement tableRow : tableRows) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(clientName)) {
                clickLinkOfTableRow(tableRow);
                Browser.closeDialog(addToClientDialog);
                return this;
            }
        }
        throw new NoSuchElementException("No client with given title was found: " + clientName);
    }

    private void clickLinkOfTableRow(WebElement tableRow) {
        WebElement link = tableRow.findElement(By.tagName("a"));
        link.click();
    }

}
