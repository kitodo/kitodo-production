/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.selenium.testframework.pages;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RulesetEditPage extends EditPage<RulesetEditPage> {

    private static final String RULESET_TAB_VIEW = EDIT_FORM + ":rulesetTabView";

    @SuppressWarnings("unused")
    @FindBy(id = RULESET_TAB_VIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-selectonemenu-trigger")
    private WebElement selectTrigger;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-messages-error-summary")
    private WebElement errorMessage;

    public RulesetEditPage() {
        super("pages/rulesetEdit.jsf");
    }

    @Override
    public RulesetEditPage goTo() {
        return null;
    }

    public RulesetEditPage insertRulesetData(Ruleset ruleset) {
        titleInput.sendKeys(ruleset.getTitle());
        selectTrigger.click();
        Browser.getDriver().findElement(By.id(RULESET_TAB_VIEW + ":file_0")).click();
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }
}
