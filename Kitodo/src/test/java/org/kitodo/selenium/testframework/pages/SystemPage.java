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

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SystemPage extends Page<SystemPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView")
    private WebElement systemTabView;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:indexingTable")
    private WebElement indexingTable;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:migrationForm:aggregatedTasksTable")
    private WebElement aggregatedTasksTable;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:createMappingButton")
    private WebElement createMappingButton;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:deleteIndexButton")
    private WebElement deleteIndexButton;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:startIndexingAllButton")
    private WebElement startIndexingAllButton;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:migrationForm:migrateWorkflows")
    private WebElement startWorkflowMigrationButton;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:migrationForm:migrateProject")
    private WebElement migrateSelectedProjectsButton;

    @SuppressWarnings("unused")
    @FindBy(id = "confirmWorkflowForm:createNewWorkflow")
    private WebElement createNewWorkflowButton;

    @SuppressWarnings("unused")
    @FindBy(id = "createTemplatesTable:0:createNewTemplateForm:createNewTemplate")
    private WebElement createNewTemplateButton;

    @SuppressWarnings("unused")
    @FindBy(id = "createTemplatesTable:0:createNewTemplateForm:templateTitle")
    private WebElement templateTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "closeForm:close")
    private WebElement closePopupButton;


    public SystemPage() {
        super("pages/system.jsf");
    }

    /**
     * Goes to system page.
     *
     * @return The system page.
     */
    @Override
    public SystemPage goTo() throws Exception {
        Pages.getTopNavigation().gotoSystem();
        await("Wait for execution of link click").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
        return this;
    }

    /**
     * Clicks on "delete index" button and accept dialog.
     */
    private void deleteIndex() throws Exception {
        switchToTabByIndex(TabIndex.INDEXING.getIndex(), systemTabView);
        await("Wait for delete index button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isButtonClicked.test(deleteIndexButton));

        Predicate<WebDriver> isAlertPresent = (d) -> {
            d.switchTo().alert();
            return true;
        };

        await("Wait for alert").atMost(5, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isAlertPresent.test(Browser.getDriver()));

        Browser.getDriver().switchTo().alert().accept();
        Thread.sleep(Browser.getDelayIndexing());
    }

    /**
     * Clicks on "create mapping" button.
     */
    private void createMapping() throws Exception {
        switchToTabByIndex(TabIndex.INDEXING.getIndex(), systemTabView);
        await("Wait for create mapping button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isButtonClicked.test(createMappingButton));
        Thread.sleep(Browser.getDelayIndexing());
    }

    /**
     * Clicks on "start indexing all" button.
     */
    private void startIndexingAll() throws Exception {
        switchToTabByIndex(TabIndex.INDEXING.getIndex(), systemTabView);
        await("Wait for start indexing button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isButtonClicked.test(startIndexingAllButton));
        Thread.sleep(Browser.getDelayIndexing());
    }

    /**
     * Deletes the old index, creates the mapping and starts new indexing.
     */
    public void startReindexingAll() throws Exception {
        switchToTabByIndex(TabIndex.INDEXING.getIndex(), systemTabView);
        deleteIndex();
        createMapping();
        startIndexingAll();
    }

    /**
     * Attempts to read indexing progress
     *
     * @return The indexing progress value as String. Empty String in case element
     *         is not readable.
     */
    public String getIndexingProgress() throws Exception {
        switchToTabByIndex(TabIndex.INDEXING.getIndex(), systemTabView);
        List<WebElement> listOfRows = Browser.getRowsOfTable(indexingTable);
        WebElement lastRow = listOfRows.get(listOfRows.size() - 1);
        return lastRow.findElement(By.className("ui-progressbar-label")).getAttribute("innerHTML");
    }

    public void startWorkflowMigration() throws Exception {
        switchToTabByIndex(TabIndex.MIGRATION.getIndex(), systemTabView);
        startWorkflowMigrationButton.click();
    }

    public WorkflowEditPage createNewWorkflow()
            throws IllegalAccessException, InstantiationException {
        WebElement element = Browser.getDriver()
                .findElement(By.xpath(
                    "//*[@id=\"systemTabView:migrationForm:aggregatedTasksTable:1:createWorkflowActionButton\"]"));
        element.click();
        clickButtonAndWaitForRedirect(createNewWorkflowButton, Pages.getWorkflowEditPage().getUrl());
        return Pages.getWorkflowEditPage();
    }

    public void selectProjects() {
        Browser.getDriver().findElements(By.className("ui-chkbox")).get(0).click();
        Browser.getDriver().findElements(By.className("ui-chkbox")).get(2).click();

        migrateSelectedProjectsButton.click();
    }

    public String getAggregatedTasks(int rowIndex) {
        List<String> tableDataByColumn = getTableDataByColumn(aggregatedTasksTable, 0);
        return tableDataByColumn.get(rowIndex);
    }

    public void createNewTemplateFromPopup(String title) {
        templateTitleInput.sendKeys(title);
        createNewTemplateButton.click();
        closePopupButton.click();
    }
}
