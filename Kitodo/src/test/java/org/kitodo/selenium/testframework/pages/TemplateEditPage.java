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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TemplateEditPage extends Page<TemplateEditPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:save")
    private WebElement saveTemplateButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:templateTabView:title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:templateTabView:workflow")
    private WebElement workflowSelect;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:templateTabView:ruleset")
    private WebElement rulesetSelect;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:templateTabView:docket")
    private WebElement docketSelect;

    private static final String CSS_SELECTOR_DROPDOWN_TRIGGER =  ".ui-selectonemenu-trigger";

    public TemplateEditPage() {
        super("pages/templateEdit.jsf");
    }

    @Override
    public TemplateEditPage goTo() {
        return null;
    }

    public TemplateEditPage insertTemplateData(Template template) {
        titleInput.sendKeys(template.getTitle());
        Browser.getDriver().findElements(By.className("ui-selectlistbox-item")).get(0).click();
        clickElement(workflowSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(workflowSelect.getAttribute("id") + "_0")));

        clickElement(rulesetSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(rulesetSelect.getAttribute("id") + "_1")));

        clickElement(docketSelect.findElement(By.cssSelector(CSS_SELECTOR_DROPDOWN_TRIGGER)));
        clickElement(Browser.getDriver().findElement(By.id(docketSelect.getAttribute("id") + "_1")));
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveTemplateButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }
}
