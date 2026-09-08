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

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends Page<LoginPage> {

    @SuppressWarnings(UNUSED)
    @FindBy(id = "login")
    private WebElement loginButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "username")
    private WebElement usernameInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "password")
    private WebElement passwordInput;

    public LoginPage() {
        super("pages/login");
    }

    /**
     * Goes to login page.
     *
     * @return The login page.
     */
    @Override
    public LoginPage goTo() {
        Browser.goTo(this.getUrl());
        return this;
    }

    /**
     * Enter user name and password into the login form and submit it for login.
     * 
     * @param user the user name
     * @param password the cleartext password 
     * @throws InterruptedException in case there is an interruption
     */
    public void performLogin(User user, String password) throws InterruptedException {
        usernameInput.clear();
        usernameInput.sendKeys(user.getLogin());

        passwordInput.clear();
        passwordInput.sendKeys(password);

        loginButton.click();
        Thread.sleep(Browser.getDelayAfterLogin());
    }

    /**
     * Login as admin user "kowal".
     * 
     * @throws InterruptedException in case there is an interruption
     * @throws DAOException in case user details can not be retrieved from the database
     */
    public void performLoginAsAdmin() throws InterruptedException, DAOException {
        performLogin(ServiceManager.getUserService().getById(1), MockDatabase.DEFAULT_USER_PASSWORD);
    }
}
