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

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProcessEditPage extends EditPage<ProcessEditPage> {

    private static final String PROCESS_EDIT_TAB_VIEW = EDIT_FORM + ":processTabView";
    private static final String TEMPLATE_PROPERTY_FORM = "templatePropertyForm";

    @SuppressWarnings("unused")
    @FindBy(id = PROCESS_EDIT_TAB_VIEW)
    private WebElement processEditTabView;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESS_EDIT_TAB_VIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESS_EDIT_TAB_VIEW + ":taskTable:0:deleteTask")
    private WebElement deleteFirstTaskLink;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESS_EDIT_TAB_VIEW + ":addTemplateProperty")
    private WebElement addTemplatePropertyButton;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_PROPERTY_FORM + ":templatePropertyTitle")
    private WebElement templatePropertyTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_PROPERTY_FORM + ":templatePropertyValue")
    private WebElement templatePropertyValueInput;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_PROPERTY_FORM + ":save")
    private WebElement templatePropertySaveButton;

    @SuppressWarnings("unused")
    @FindBy(id = "yesButton")
    private WebElement confirmRemoveButton;

    public ProcessEditPage() {
        super("pages/processEdit.jsf");
    }

    @Override
    public ProcessEditPage goTo() {
        return null;
    }

    public void changeProcessData() throws Exception {
        titleInput.clear();
        titleInput.sendKeys("ChangedTitle");

        switchToTabByIndex(TabIndex.PROCESS_TEMPLATES.getIndex());
        addTemplateProperty("First new", "1");
        addTemplateProperty("Second new", "2");
    }

    public ProcessesPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProcessesPage().getUrl());
        return Pages.getProcessesPage();
    }

    private void addTemplateProperty(String title, String value) throws InterruptedException {
        addTemplatePropertyButton.click();
        Thread.sleep(Browser.getDelayAfterNewItemClick());
        templatePropertyTitleInput.click();
        templatePropertyTitleInput.sendKeys(title);
        templatePropertyValueInput.click();
        templatePropertyValueInput.sendKeys(value);
        templatePropertySaveButton.click();
        Thread.sleep(Browser.getDelayMaxAfterLinkClick());
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     */
    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, processEditTabView);
    }
}
