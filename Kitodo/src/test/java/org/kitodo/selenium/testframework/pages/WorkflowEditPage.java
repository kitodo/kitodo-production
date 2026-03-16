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

import java.time.Duration;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkflowEditPage extends EditPage<WorkflowEditPage> {

    private static final String WORKFLOW_TAB_VIEW = EDIT_FORM + ":workflowTabView";

    @SuppressWarnings("unused")
    @FindBy(id = WORKFLOW_TAB_VIEW + ":xmlDiagramName")
    private WebElement fileInput;

    @SuppressWarnings("unused")
    @FindBy(id = WORKFLOW_TAB_VIEW + ":js-create-diagram")
    private WebElement createDiagram;

    @SuppressWarnings("unused")
    @FindBy(id = WORKFLOW_TAB_VIEW + ":status")
    private WebElement workflowStatusInput;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//div[@id='"+WORKFLOW_TAB_VIEW +":status_panel']/div/ul/li[text()='Aktiv']" )
    private WebElement activeOption;

    @Override
    public WorkflowEditPage goTo() {
        return null;
    }

    public WorkflowEditPage() {
        super("pages/workflowEdit.jsf");
    }

    public WorkflowEditPage insertWorkflowData(Workflow workflow) {
        fileInput.sendKeys(workflow.getTitle());
        WebElement taskBox = Browser.getDriver()
                .findElement(By.cssSelector("#js-canvas [data-element-id='Task_0i1d0ke']"));
        Actions builder = new Actions(Browser.getDriver());
        builder.click(taskBox).build().perform();

        WebElement permissionsHeader = Browser.getDriver().findElement(By.cssSelector(
            "[data-group-id='group-kitodo-permissions'] .bio-properties-panel-group-header"));
        if (!permissionsHeader.getAttribute("class").contains("open")) {
            builder.click(permissionsHeader).build().perform();
        }

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), Duration.ofSeconds(10));
        WebElement firstRole = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("[data-group-id='group-kitodo-permissions'] .bio-properties-panel-group-entries input[type='checkbox']")));
        builder.click(firstRole).build().perform();

        return this;
    }

    public void changeWorkflowStatusToActive(){
        workflowStatusInput.click();
        activeOption.click();
    }

    public ProjectsPage save() throws ReflectiveOperationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }

    public SystemPage saveForMigration() throws ReflectiveOperationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getSystemPage().getUrl());
        return Pages.getSystemPage();
    }

    public String getWorkflowTitle() {
        return fileInput.getAttribute("value");
    }

    public void changeWorkflowTitle(String workflowTitle) {
        fileInput.clear();
        fileInput.sendKeys(workflowTitle);
    }
}
