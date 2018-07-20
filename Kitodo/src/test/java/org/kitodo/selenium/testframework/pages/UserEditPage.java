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

import org.kitodo.data.database.beans.User;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        Browser.clickAjaxSaveButton(saveUserButton);
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 30); //seconds
        wait.until(ExpectedConditions.urlContains(Pages.getUsersPage().getUrl()));
        return Pages.getUsersPage();
    }

}
