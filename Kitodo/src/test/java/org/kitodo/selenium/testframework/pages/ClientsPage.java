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

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ClientsPage extends Page<ClientsPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "clientForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "clientForm:newClientButton")
    private WebElement newClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "clientsTabView:clientsTable_data")
    private WebElement clientsTable;

    public ClientsPage() {
        super("pages/clients.jsf");
    }

    /**
     * Goes to clients page.
     *
     * @return The clients page.
     */
    @Override
    public ClientsPage goTo() throws Exception {
        Pages.getTopNavigation().gotoClients();
        return this;
    }

    public int countListedClients() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(clientsTable).size();
    }

    /**
     * Goes to edit page for creating a new user.
     *
     * @return The user edit page.
     */
    public ClientEditPage createNewClient() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        await("Wait for create new client button").atMost(Browser.getDelayAfterNewItemClick(), TimeUnit.MILLISECONDS)
                .ignoreExceptions().until(() -> isButtonClicked.matches(newClientButton));

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60); // seconds
        wait.until(ExpectedConditions.urlContains(Pages.getClientEditPage().getUrl()));
        return Pages.getClientEditPage();
    }

    /**
     * Returns a list of all client titles which were displayed on clients page.
     *
     * @return The list of client titles
     */
    public List<String> getClientNames() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(clientsTable, 0);
    }

}
