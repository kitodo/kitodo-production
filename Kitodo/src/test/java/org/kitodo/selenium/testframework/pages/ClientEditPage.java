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

import org.kitodo.data.database.beans.Client;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ClientEditPage extends Page<ClientEditPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:saveButton")
    private WebElement saveClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:clientsTabView:nameInput")
    private WebElement nameInput;

    public ClientEditPage() {
        super("pages/clientEdit.jsf");
    }

    @Override
    public ClientEditPage goTo() {
        return null;
    }

    public ClientEditPage insertClientData(Client client) {
        nameInput.sendKeys(client.getName());
        return this;
    }

    public UsersPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveClientButton, Pages.getUsersPage().getUrl());
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 30); //seconds
        wait.until(ExpectedConditions.urlContains(Pages.getUsersPage().getUrl()));
        return Pages.getUsersPage();
    }
}
