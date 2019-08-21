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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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

    @SuppressWarnings("unused")
    @FindBy(xpath = "//html/body/div[3]/main/div/div/form/div[3]/div/div/div[2]/div/div/div[3]/div/div/svg/g/g[1]/g[3]/g/g" )
    private WebElement taskBox;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//div[@id='js-properties-panel']/div/div/div[2]/ul/li[2]" )
    private WebElement roleTab;

    @SuppressWarnings("unused")
    @FindBy(id = "camunda-permittedUserRole_3")
    private WebElement firstRole;

    @Override
    public WorkflowEditPage goTo() {
        return null;
    }

    public WorkflowEditPage() {
        super("pages/workflowEdit.jsf");
    }

    public WorkflowEditPage insertWorkflowData(Workflow workflow) throws InterruptedException {
        fileInput.sendKeys(workflow.getTitle());
        taskBox = Browser.getDriver().findElementByXPath("/html/body/div[3]/main/div/div/form/div[3]/div/div/div[2]/div/div/div[3]/div/div/svg/g/g[1]/g[3]/g/g");
        Thread.sleep(2000);
        taskBox.click();
        roleTab.click();
        firstRole.click();

        return this;
    }

    public WorkflowEditPage changeWorkflowStatusToActive(){
        workflowStatusInput.click();
        activeOption.click();
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }
}
