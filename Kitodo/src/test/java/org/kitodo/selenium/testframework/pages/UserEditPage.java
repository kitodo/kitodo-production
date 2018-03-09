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

public class UserEditPage {
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

    public UserEditPage insertUserData(User user) throws InterruptedException {
        firstNameInput.sendKeys(user.getName());
        lastNameInput.sendKeys(user.getSurname());
        loginInput.sendKeys(user.getLogin());
        passwordInput.sendKeys(user.getPassword());
        locationInput.sendKeys(user.getLocation());
        metaDataLanguageInput.sendKeys(user.getMetadataLanguage());
        return this;
    }

    public UsersPage save() throws InterruptedException, IllegalAccessException, InstantiationException {
        Browser.clickAjaxSaveButton(saveUserButton);
        Thread.sleep(Browser.getDelayAfterSave());
        return Pages.getUsersPage();
    }

}
