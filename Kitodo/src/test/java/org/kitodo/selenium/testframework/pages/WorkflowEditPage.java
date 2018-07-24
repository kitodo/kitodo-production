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

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkflowEditPage extends Page<WorkflowEditPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:save")
    private WebElement saveWorkflowButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:workflowTabView:xmlDiagramName")
    private WebElement fileInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:workflowTabView:js-create-diagram")
    private WebElement createDiagram;

    @Override
    public WorkflowEditPage goTo() {
        return null;
    }

    public WorkflowEditPage() {
        super("pages/workflowEdit.jsf");
    }

    public WorkflowEditPage insertWorkflowData(Workflow workflow) {
        fileInput.sendKeys(workflow.getFileName());
        createDiagram.click();
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        Browser.clickAjaxSaveButtonAndRedirect(saveWorkflowButton, Pages.getProjectsPage().getUrl());
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 30); //seconds
        wait.until(ExpectedConditions.urlContains(Pages.getProjectsPage().getUrl()));
        return Pages.getProjectsPage();
    }
}
