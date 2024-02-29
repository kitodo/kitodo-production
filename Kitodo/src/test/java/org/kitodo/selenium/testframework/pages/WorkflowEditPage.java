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
import org.openqa.selenium.interactions.Actions;
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
                .findElementByCssSelector("#js-canvas > div > div > svg > g > g.layer-base > g:nth-child(3) > g > g");
        Actions builder = new Actions(Browser.getDriver());
        builder.click(taskBox).build().perform();

        WebElement roleTab = Browser.getDriver().findElementByCssSelector(
            "#js-properties-panel > div > div > div.bpp-properties-tab-bar > ul > li:nth-child(2)");
        builder.click(roleTab).build().perform();

        WebElement firstRole = Browser.getDriver().findElementByCssSelector("#camunda-permittedUserRole_1");
        builder.click(firstRole).build().perform();

        return this;
    }

    public void changeWorkflowStatusToActive(){
        workflowStatusInput.click();
        activeOption.click();
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }

    public SystemPage saveForMigration() throws IllegalAccessException, InstantiationException {
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
