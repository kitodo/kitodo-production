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

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.List;

import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProjectsPage extends Page<ProjectsPage> {

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
    @FindBy(id = "projectsTabView:workflowTable_data")
    private WebElement workflowsTable;

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
    @FindBy(id = "projectForm:newTemplateButton")
    private WebElement newTemplateButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newWorkflowButton")
    private WebElement newWorkflowButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newDocketButton")
    private WebElement newDocketButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newRulesetButton")
    private WebElement newRulesetButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:templateTable:0:templateActionForm:action22")
    private WebElement createProcess;

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
        return switchToTabByIndex(index, projectsTabView);
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

    public int countListedWorkflows() throws Exception {
        switchToTabByIndex(TabIndex.WORKFLOWS.getIndex());
        return getRowsOfTable(workflowsTable).size();
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
     * Returns a list of all template titles which were displayed on workflows page.
     *
     * @return list of template titles
     */
    public List<String> getTemplateTitles() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());
        return getTableDataByColumn(templatesTable, 1);
    }

    /**
     * Returns a list of all workflow titles which were displayed on workflows page.
     *
     * @return list of workflow titles
     */
    public List<String> getWorkflowTitles() throws Exception {
        switchToTabByIndex(TabIndex.WORKFLOWS.getIndex());
        return getTableDataByColumn(workflowsTable, 0);
    }

    /**
     * Returns a list of all docket titles which were displayed on dockets page.
     *
     * @return list of docket titles
     */
    public List<String> getDocketTitles() throws Exception {
        switchToTabByIndex(TabIndex.DOCKETS.getIndex());
        return getTableDataByColumn(docketsTable, 0);
    }

    /**
     * Returns a list of all ruleset titles which were displayed on rulesets page.
     *
     * @return list of ruleset titles
     */
    public List<String> getRulesetTitles() throws Exception {
        switchToTabByIndex(TabIndex.RULESETS.getIndex());
        return getTableDataByColumn(rulesetsTable, 0);
    }

    public void createNewProcess() {
        createProcess.click();
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
        clickButtonAndWaitForRedirect(newProjectButton, Pages.getProjectEditPage().getUrl());
        return Pages.getProjectEditPage();
    }

    /**
     * Go to edit page for creating a new template.
     *
     * @return template edit page
     */
    public TemplateEditPage createNewTemplate() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();

        clickButtonAndWaitForRedirect(newTemplateButton, Pages.getTemplateEditPage().getUrl());
        return Pages.getTemplateEditPage();
    }

    /**
     * Go to edit page for creating a new workflow.
     *
     * @return workflow edit page
     */
    public WorkflowEditPage createNewWorkflow() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newWorkflowButton, Pages.getWorkflowEditPage().getUrl());
        return Pages.getWorkflowEditPage();
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
        clickButtonAndWaitForRedirect(newDocketButton, Pages.getDocketEditPage().getUrl());
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
        clickButtonAndWaitForRedirect(newRulesetButton, Pages.getRulesetEditPage().getUrl());
        return Pages.getRulesetEditPage();
    }
}
