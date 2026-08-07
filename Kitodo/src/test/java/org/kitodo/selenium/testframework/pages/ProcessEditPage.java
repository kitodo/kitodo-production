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

    @SuppressWarnings(UNUSED)
    @FindBy(id = PROCESS_EDIT_TAB_VIEW)
    private WebElement processEditTabView;

    @SuppressWarnings(UNUSED)
    @FindBy(id = PROCESS_EDIT_TAB_VIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = PROCESS_EDIT_TAB_VIEW + ":taskTable:0:deleteTask")
    private WebElement deleteFirstTaskLink;

    @SuppressWarnings(UNUSED)
    @FindBy(id = PROCESS_EDIT_TAB_VIEW + ":addTemplateProperty")
    private WebElement addTemplatePropertyButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = TEMPLATE_PROPERTY_FORM + ":templatePropertyTitle")
    private WebElement templatePropertyTitleInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = TEMPLATE_PROPERTY_FORM + ":templatePropertyValue")
    private WebElement templatePropertyValueInput;

    @SuppressWarnings(UNUSED)
    @FindBy(id = TEMPLATE_PROPERTY_FORM + ":save")
    private WebElement templatePropertySaveButton;

    @SuppressWarnings(UNUSED)
    @FindBy(id = "yesButton")
    private WebElement confirmRemoveButton;

    public ProcessEditPage() {
        super("pages/processEdit");
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

    public ProcessesPage save() throws ReflectiveOperationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProcessesPage().getUrl());
        return Pages.getProcessesPage();
    }

    public ProcessesPage cancel() throws ReflectiveOperationException {
        clickButtonAndWaitForRedirect(cancelButton, Pages.getProcessesPage().getUrl());
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
