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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UserEditPage {
    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:saveUserButton")
    private WebElement saveUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:userTabView:firstName")
    private WebElement firstNameInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:userTabView:surname")
    private WebElement lastNameInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:userTabView:login")
    private WebElement loginInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:userTabView:password")
    private WebElement passwordInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:userTabView:location")
    private WebElement locationInput;

    @SuppressWarnings("unused")
    @FindBy(id = "userEditForm:userTabView:metaDataLanguage")
    private WebElement metaDataLanguageInput;

    /**
     * Goes to users page.
     *
     * @return The users page.
     */
    public void goTo() throws Exception {
        Pages.getUsersPage().createNewUser();
    }

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
        saveUserButton.click();
        int attempt = 1;
        while (Pages.getUsersPage().isNotAt() && attempt <= 5) {
            Thread.sleep(Browser.getDelayAfterSave());
            attempt++;
        }
        if (attempt > 5) {
            throw new TimeoutException("Could not save user!");
        }
        return Pages.getUsersPage();
    }

}
