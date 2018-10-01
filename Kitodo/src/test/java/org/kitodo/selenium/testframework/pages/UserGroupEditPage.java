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

public class UserGroupEditPage extends Page<UserGroupEditPage> {

    private static final String EDIT_FORM = "editForm";
    private static final String USER_GROUP_TAB_VIEW = EDIT_FORM + ":usergroupTabView";

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":save")
    private WebElement saveUserGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = USER_GROUP_TAB_VIEW + ":titleInput")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = USER_GROUP_TAB_VIEW + ":authoritiesClientPick")
    private WebElement clientAuthoritiesPickList;

    @SuppressWarnings("unused")
    @FindBy(id = USER_GROUP_TAB_VIEW + ":clientSelect")
    private WebElement clientSelector;

    @SuppressWarnings("unused")
    @FindBy(id = USER_GROUP_TAB_VIEW + ":projectSelect")
    private WebElement projectSelector;

    public UserGroupEditPage() {
        super("pages/usergroupEdit.jsf");
    }

    @Override
    public UserGroupEditPage goTo() {
        return null;
    }

    private WebElement getAddAllElementsButtonByPicklist(WebElement picklist) {
        return picklist.findElement(By.className("ui-picklist-button-add-all"));
    }

    private WebElement getRemoveAllElementsButtonByPicklist(WebElement picklist) {
        return picklist.findElement(By.className("ui-picklist-button-remove-all"));
    }

    private WebElement getAddElementButtonByPicklist(WebElement picklist) {
        return picklist.findElement(By.className("ui-picklist-button-add"));
    }

    private WebElement getRemoveElementButtonByPicklist(WebElement picklist) {
        return picklist.findElement(By.className("ui-picklist-button-remove"));
    }

    private List<WebElement> getSourceItemsFromPickList(WebElement picklist) {
        WebElement source = picklist.findElement(By.className("ui-picklist-source"));
        return source.findElements(By.className("ui-picklist-item"));
    }

    private List<WebElement> getTargetItemsFromPickList(WebElement picklist) {
        WebElement source = picklist.findElement(By.className("ui-picklist-target"));
        return source.findElements(By.className("ui-picklist-item"));
    }

    public UserGroupEditPage assignAllClientAuthorities() throws InterruptedException {
        getAddAllElementsButtonByPicklist(clientAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public UserGroupEditPage removeAllClientAuthorities() throws InterruptedException {
        getRemoveAllElementsButtonByPicklist(clientAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public UsersPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveUserGroupButton, Pages.getUsersPage().getUrl());
        return Pages.getUsersPage();
    }

    public int countAssignedClientAuthorities() {
        return getTargetItemsFromPickList(clientAuthoritiesPickList).size();
    }

    public int countAvailableClientAuthorities() {
        return getSourceItemsFromPickList(clientAuthoritiesPickList).size();
    }

    public UserGroupEditPage setUserGroupTitle(String newTitle) {
        titleInput.clear();
        titleInput.sendKeys(newTitle);
        return this;
    }

    public String getUserGroupTitle() {
        return titleInput.getAttribute("value");
    }

}
