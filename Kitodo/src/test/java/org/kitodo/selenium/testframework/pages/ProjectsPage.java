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
import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

public class ProjectsPage extends Page {

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView")
    private WebElement projectsTabView;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:projectsTable_data")
    private WebElement projectsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:templateTable_data")
    private WebElement templatesTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:docketTable_data")
    private WebElement docketsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:rulesetTable_data")
    private WebElement rulesetsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newProjectButton")
    private WebElement newProjectButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newDocketButton")
    private WebElement newDocketButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newRulesetButton")
    private WebElement newRulesetButton;

    public ProjectsPage() {
        super("pages/projects.jsf");
    }

    /**
     * Goes to projects page.
     *
     * @return The projects page.
     */
    public ProjectsPage goTo() throws Exception {
        Pages.getTopNavigation().gotoProjects();
        return this;
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @return The users page.
     */
    public ProjectsPage switchToTabByIndex(int index) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        clickTab(index, projectsTabView);
        return this;
    }

    public int countListedProjects() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(projectsTable).size();
    }

    public int countListedTemplates() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());
        return getRowsOfTable(templatesTable).size();
    }

    public int countListedDockets() throws Exception {
        switchToTabByIndex(TabIndex.DOCKETS.getIndex());
        return getRowsOfTable(docketsTable).size();
    }

    public int countListedRulesets() throws Exception {
        switchToTabByIndex(TabIndex.RULESETS.getIndex());
        return getRowsOfTable(rulesetsTable).size();
    }

    /**
     * Return a list of all project titles which were displayed on clients page.
     *
     * @return list of project titles
     */
    public List<String> getProjectsTitles() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(projectsTable, 1);
    }

    /**
     * Returns a list of all docket titles which were displayed on dockets page.
     *
     * @return list of docket titles
     */
    public List<String> getDocketTitles() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(docketsTable, 0);
    }

    /**
     * Returns a list of all ruleset titles which were displayed on rulesets page.
     *
     * @return list of ruleset titles
     */
    public List<String> getRulesetTitles() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(rulesetsTable, 0);
    }

    /**
     * Go to edit page for creating a new project.
     *
     * @return project edit page
     */
    public ProjectEditPage createNewProject() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        await("Wait for create new project button").atMost(Browser.getDelayAfterNewItemClick(), TimeUnit.MILLISECONDS)
                .ignoreExceptions().until(() -> isButtonClicked.matches(newProjectButton));

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60); // seconds
        wait.until(ExpectedConditions.urlContains(Pages.getProjectEditPage().getUrl()));
        return Pages.getProjectEditPage();
    }

    /**
     * Go to edit page for creating a new docket.
     *
     * @return docket edit page
     */
    public DocketEditPage createNewDocket() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        await("Wait for create new docket button").atMost(Browser.getDelayAfterNewItemClick(), TimeUnit.MILLISECONDS)
                .ignoreExceptions().until(() -> isButtonClicked.matches(newDocketButton));

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60); // seconds
        wait.until(ExpectedConditions.urlContains(Pages.getDocketEditPage().getUrl()));
        return Pages.getDocketEditPage();
    }

    /**
     * Go to edit page for creating a new ruleset.
     *
     * @return ruleset edit page
     */
    public RulesetEditPage createNewRuleset() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        await("Wait for create new ruleset button").atMost(Browser.getDelayAfterNewItemClick(), TimeUnit.MILLISECONDS)
                .ignoreExceptions().until(() -> isButtonClicked.matches(newRulesetButton));

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60); // seconds
        wait.until(ExpectedConditions.urlContains(Pages.getRulesetEditPage().getUrl()));
        return Pages.getRulesetEditPage();
    }
}
