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

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.List;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ClientsPage {

    @SuppressWarnings("unused")
    @FindBy(id = "clientForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "clientForm:newClientButton")
    private WebElement newClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "clientsTabView:clientsTable_data")
    private WebElement clientsTable;

    /**
     * Goes to clients page.
     *
     * @return The clients page.
     */
    public ClientsPage goTo() throws Exception {
        Pages.getTopNavigation().gotoClients();
        return this;
    }

    /**
     * Checks if the browser is currently at clients page.
     *
     * @return True if browser is at clients page.
     */
    public boolean isAt() {
        return Browser.getCurrentUrl().contains("clients");
    }

    /**
     * Checks if the browser is currently not at clients page.
     *
     * @return True if browser is not at clients page.
     */
    public boolean isNotAt() {
        return !isAt();
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
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        newClientButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        return Pages.getClientEditPage();
    }

    /**
     * Returns a list of all client titles which were displayed on clients page.
     *
     * @return The list of client titles
     */
    public List<String> getListOfClientNames() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(clientsTable, 0);
    }

}
