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

public class UsersPage {

    @SuppressWarnings("unused")
    @FindBy(id = "clientsTable")
    private WebElement clientsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "usersTabView:usersTable_data")
    private WebElement usersTableData;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newUserButton")
    private WebElement newUserButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newUserGroupButton")
    private WebElement newUserGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = "newElementForm:newLdapGroupButton")
    private WebElement newLdapGroupButton;

    public void goTo() throws Exception {
        Pages.getTopNavigation().gotoUsers();
    }

    public boolean isAt() throws InterruptedException {
        return Browser.getCurrentUrl().contains("users");
    }

    public int countListedUsers() throws Exception {
        if (!isAt()) {
            goTo();
        }
        List<WebElement> listOfRows = usersTableData.findElements(By.tagName("tr"));
        return listOfRows.size();
    }

    public UserEditPage goToAddUser() throws Exception {
        if (!isAt()) {
            goTo();
        }
        newElementButton.click();
        Thread.sleep(400);
        newUserButton.click();
        Thread.sleep(400);

        return Pages.getUserEditPage();
    }

}
