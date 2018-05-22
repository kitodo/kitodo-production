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

public class ClientEditPage {
    @SuppressWarnings("unused")
    @FindBy(id = "editForm:saveButton")
    private WebElement saveClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:clientsTabView:nameInput")
    private WebElement nameInput;

    public ClientEditPage insertClientData(Client client) {
        nameInput.sendKeys(client.getName());
        return this;
    }

    public ClientsPage save() throws IllegalAccessException, InstantiationException {
        Browser.clickAjaxSaveButton(saveClientButton);
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 30); //seconds
        wait.until(ExpectedConditions.urlContains(Pages.getClientsPage().getUrl()));
        return Pages.getClientsPage();
    }
}
