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
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ClientEditPage extends EditPage<ClientEditPage> {

    private static final String CLIENTS_TAB_VIEW = EDIT_FORM + ":clientsTabView";

    @SuppressWarnings("unused")
    @FindBy(id = CLIENTS_TAB_VIEW + ":nameInput")
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
        clickButtonAndWaitForRedirect(saveButton, Pages.getUsersPage().getUrl());
        return Pages.getUsersPage();
    }
}
