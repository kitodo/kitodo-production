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

import java.util.List;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ClientsPage {

    @SuppressWarnings("unused")
    @FindBy(id = "newClientButton")
    private WebElement newClientButton;

    @SuppressWarnings("unused")
    @FindBy(id = "clientsTable_data")
    private WebElement clientsTableData;

    public void goTo() throws Exception {
        Pages.getTopNavigation().gotoClients();
    }

    public boolean isAt() throws InterruptedException {
        return Browser.getCurrentUrl().contains("clients");
    }

    public int countListedClients() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listOfRows = clientsTableData.findElements(By.tagName("tr"));
        return listOfRows.size();
    }

}
