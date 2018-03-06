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

public class UserGroupEditPage {

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:saveUserGroupButton")
    private WebElement saveUserGroupButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:usergroupTabView:titleInput")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:usergroupTabView:authoritiesGlobalPick")
    private WebElement globalAuthoritiesPickList;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:usergroupTabView:authoritiesClientPick")
    private WebElement clientAuthoritiesPickList;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:usergroupTabView:authoritiesProjectPick")
    private WebElement projectAuthoritiesPickList;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:usergroupTabView:clientSelect")
    private WebElement clientSelector;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:usergroupTabView:projectSelect")
    private WebElement projectSelector;

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

    public UserGroupEditPage removeAllGlobalAuthorities() throws InterruptedException {
        getRemoveAllElementsButtonByPicklist(globalAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public UserGroupEditPage assignAllGlobalAuthorities() throws InterruptedException {
        getAddAllElementsButtonByPicklist(globalAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
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

    public UserGroupEditPage assignAllProjectAuthorities() throws InterruptedException {
        getAddAllElementsButtonByPicklist(projectAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public UserGroupEditPage removeAllProjectAuthorities() throws InterruptedException {
        getRemoveAllElementsButtonByPicklist(projectAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public UsersPage save() throws InterruptedException, IllegalAccessException, InstantiationException {
        saveUserGroupButton.click();
        Thread.sleep(Browser.getDelayAfterSave());
        return Pages.getUsersPage();
    }

    /**
     * Checks if the browser is currently at user group edit page.
     *
     * @return True if browser is at user group edit page.
     */
    public boolean isAt() {
        return Browser.getCurrentUrl().contains("usergroupEdit");
    }

    /**
     * Checks if the browser is currently not at user group edit page.
     *
     * @return True if browser is not at user group edit page.
     */
    public boolean isNotAt() {
        return !isAt();
    }

    public int countAssignedGlobalAuthorities() {
        return getTargetItemsFromPickList(globalAuthoritiesPickList).size();
    }

    public int countAssignedClientAuthorities() {
        return getTargetItemsFromPickList(clientAuthoritiesPickList).size();
    }

    public int countAssignedProjectAuthorities() {
        return getTargetItemsFromPickList(projectAuthoritiesPickList).size();
    }

    public int countAvailableGlobalAuthorities() {
        return getSourceItemsFromPickList(globalAuthoritiesPickList).size();
    }

    public int countAvailableClientAuthorities() {
        return getSourceItemsFromPickList(clientAuthoritiesPickList).size();
    }

    public int countAvailablePrjectAuthorities() {
        return getSourceItemsFromPickList(projectAuthoritiesPickList).size();
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
