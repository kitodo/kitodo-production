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

public class RoleEditPage extends EditPage<RoleEditPage> {

    private static final String ROLE_TAB_VIEW = EDIT_FORM + ":roleTabView";

    @SuppressWarnings("unused")
    @FindBy(id = ROLE_TAB_VIEW)
    private WebElement roleTabView;

    @SuppressWarnings("unused")
    @FindBy(id = ROLE_TAB_VIEW + ":titleInput")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = ROLE_TAB_VIEW + ":authoritiesGlobalPick")
    private WebElement globalAuthoritiesPickList;

    @SuppressWarnings("unused")
    @FindBy(id = ROLE_TAB_VIEW + ":authoritiesClientPick")
    private WebElement clientAuthoritiesPickList;

    public RoleEditPage() {
        super("pages/roleEdit.jsf");
    }

    @Override
    public RoleEditPage goTo() {
        return null;
    }

    private WebElement getAddAllElementsButtonByPickList(WebElement pickList) {
        return pickList.findElement(By.className("ui-picklist-button-add-all"));
    }

    private WebElement getRemoveAllElementsButtonByPickList(WebElement pickList) {
        return pickList.findElement(By.className("ui-picklist-button-remove-all"));
    }

    private WebElement getAddElementButtonByPickList(WebElement pickList) {
        return pickList.findElement(By.className("ui-picklist-button-add"));
    }

    private WebElement getRemoveElementButtonByPickList(WebElement pickList) {
        return pickList.findElement(By.className("ui-picklist-button-remove"));
    }

    private List<WebElement> getSourceItemsFromPickList(WebElement picklist) {
        WebElement source = picklist.findElement(By.className("ui-picklist-source"));
        return source.findElements(By.className("ui-picklist-item"));
    }

    private List<WebElement> getTargetItemsFromPickList(WebElement pickList) {
        WebElement source = pickList.findElement(By.className("ui-picklist-target"));
        return source.findElements(By.className("ui-picklist-item"));
    }

    public RoleEditPage removeAllGlobalAuthorities() throws InterruptedException {
        getRemoveAllElementsButtonByPickList(globalAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }
    public RoleEditPage assignAllGlobalAuthorities() throws InterruptedException {
        getAddAllElementsButtonByPickList(globalAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public RoleEditPage assignAllClientAuthorities() throws InterruptedException {
        getAddAllElementsButtonByPickList(clientAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public RoleEditPage removeAllClientAuthorities() throws InterruptedException {
        getRemoveAllElementsButtonByPickList(clientAuthoritiesPickList).click();
        Thread.sleep(Browser.getDelayAfterPickListClick());
        return this;
    }

    public UsersPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getUsersPage().getUrl());
        return Pages.getUsersPage();
    }

    public int countAssignedGlobalAuthorities() {
        return getTargetItemsFromPickList(globalAuthoritiesPickList).size();
    }

    public int countAssignedClientAuthorities() {
        return getTargetItemsFromPickList(clientAuthoritiesPickList).size();
    }

    public int countAvailableGlobalAuthorities() {
        return getSourceItemsFromPickList(globalAuthoritiesPickList).size();
    }

    public int countAvailableClientAuthorities() {
        return getSourceItemsFromPickList(clientAuthoritiesPickList).size();
    }

    public RoleEditPage setRoleTitle(String newTitle) {
        titleInput.clear();
        titleInput.sendKeys(newTitle);
        return this;
    }

    public String getRoleTitle() {
        return titleInput.getAttribute("value");
    }
}
