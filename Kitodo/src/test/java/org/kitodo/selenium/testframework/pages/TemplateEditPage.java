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

import org.kitodo.data.database.beans.Template;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TemplateEditPage extends EditPage<TemplateEditPage> {

    private static final String TEMPLATE_TAB_VIEW = EDIT_FORM + ":templateTabView";
    private static final String CSS_SELECTOR_DROPDOWN_TRIGGER =  ".ui-selectonemenu-trigger";

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TAB_VIEW)
    private WebElement templateTabView;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TAB_VIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TAB_VIEW + ":workflow")
    private WebElement workflowSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TAB_VIEW + ":ruleset")
    private WebElement rulesetSelect;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TAB_VIEW + ":docket")
    private WebElement docketSelect;

    @FindBy(id = "editForm:templateTabView:active")
    private WebElement activeSwitch;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TAB_VIEW + ":taskTable:0:editTask")
    private WebElement editTaskLink;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":tabs:j_id_44:0:validator")
    private WebElement firstValidatorCheckbox;

    public TemplateEditPage() {
        super("pages/templateEdit.jsf");
    }

    @Override
    public TemplateEditPage goTo() {
        return null;
    }

    public TemplateEditPage insertTemplateData(Template template) {
        titleInput.sendKeys(template.getTitle());
        Browser.getDriver().findElements(By.className("ui-chkbox")).get(0).click();
        clickElement(workflowSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(workflowSelect.getAttribute("id") + "_0")));

        clickElement(rulesetSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(rulesetSelect.getAttribute("id") + "_1")));

        clickElement(docketSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(docketSelect.getAttribute("id") + "_1")));
        return this;
    }

    public TemplateEditPage addSecondProject() {
        Browser.getDriver().findElements(By.className("ui-chkbox")).get(1).click();
        return this;
    }

    public TemplateEditPage editTemplateTask() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATE_TASKS.getIndex(), templateTabView);
        editTaskLink.click();
        // TODO: I'm unable to reproduce conditions to display this checkbox
        // TODO: find out what more is needed
        //firstValidatorCheckbox.click();
        return this;
    }

    public void saveTask()throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getTemplateEditPage().getUrl());
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }

    /**
     * Toggle switch to activate/deactivate template.
     */
    public void hideTemplate() {
        activeSwitch.findElement(By.className("ui-chkbox-box")).click();
    }
}
