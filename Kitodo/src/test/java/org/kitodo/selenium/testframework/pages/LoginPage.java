/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.selenium.testframework.pages;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.production.services.ServiceManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends Page<LoginPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "login")
    private WebElement loginButton;

    @SuppressWarnings("unused")
    @FindBy(id = "username")
    private WebElement usernameInput;

    @SuppressWarnings("unused")
    @FindBy(id = "password")
    private WebElement passwordInput;

    public LoginPage() {
        super("pages/login.jsf");
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

    public void performLogin(User user) throws InterruptedException {
        SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
        String password = passwordEncoder.decrypt(user.getPassword());

        usernameInput.clear();
        usernameInput.sendKeys(user.getLogin());

        passwordInput.clear();
        passwordInput.sendKeys(password);

        loginButton.click();
        Thread.sleep(Browser.getDelayAfterLogin());
    }

    public void performLoginAsAdmin() throws InterruptedException, DAOException {
        performLogin(ServiceManager.getUserService().getById(1));
    }
}
