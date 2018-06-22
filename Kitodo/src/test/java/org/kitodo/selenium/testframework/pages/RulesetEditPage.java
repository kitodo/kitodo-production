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

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RulesetEditPage extends Page {

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:save")
    private WebElement saveRulesetButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:rulesetTabView:title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:rulesetTabView:file")
    private WebElement fileInput;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-messages-error-summary")
    private WebElement errorMessage;

    public RulesetEditPage() {
        super("pages/rulesetEdit.jsf");
    }

    public RulesetEditPage insertRulesetData(Ruleset ruleset) {
        titleInput.sendKeys(ruleset.getTitle());
        fileInput.sendKeys(ruleset.getFile());
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        Browser.clickAjaxSaveButton(saveRulesetButton);
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 30); //seconds
        wait.until(ExpectedConditions.urlContains(Pages.getProjectsPage().getUrl()));
        return Pages.getProjectsPage();
    }

    public String saveWithError() {
        return saveWithError(saveRulesetButton);
    }
}
