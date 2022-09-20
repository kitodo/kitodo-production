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
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.helper.Helper;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class ProcessesPage extends Page<ProcessesPage> {

    private static final String PROCESSES_TAB_VIEW = "processesTabView";
    private static final String PROCESSES_FORM = PROCESSES_TAB_VIEW + ":processesForm";
    private static final String BATCH_FORM = PROCESSES_TAB_VIEW + ":batchForm";
    private static final String PROCESSES_TABLE = PROCESSES_FORM + ":processesTable";
    private static final String PROCESSES_TABLE_TITLE_COLUMN = PROCESSES_TABLE + ":titleColumn";
    private static final String PROCESS_TITLE = "Second process";
    private static final String WAIT_FOR_ACTIONS_BUTTON = "Wait for actions menu button";
    private static final String WAIT_FOR_ACTIONS_MENU = "Wait for actions menu to open";

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TAB_VIEW)
    private WebElement processesTabView;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TABLE + DATA)
    private WebElement processesTable;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TABLE_TITLE_COLUMN)
    private WebElement processesTableTitleColumn;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":selectBatches")
    private WebElement batchesSelect;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":selectProcesses")
    private WebElement processesSelect;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/processEdit.jsf?referer=processes&id=1']")
    private WebElement editProcessLink;

    private WebElement downloadDocketLink;

    private WebElement downloadLogLink;

    private WebElement editMetadataLink;

    @SuppressWarnings("unused")
    @FindBy(id = "search")
    private WebElement searchForProcessesButton;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_FORM + ":createExcel")
    private WebElement downloadSearchResultAsExcel;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_FORM + ":createPdf")
    private WebElement downloadSearchResultAsPdf;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_FORM + ":actionsButton")
    private WebElement actionsButton;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":batchActionsButton")
    private WebElement possibleBatchActionsButton;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":processActionsButton")
    private WebElement possibleProcessActionsButton;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":createBatchSelection")
    private WebElement createBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":renameBatchSelection")
    private WebElement renameBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":deleteBatchSelection")
    private WebElement deleteBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":addProcessesToBatch")
    private WebElement addProcessesToBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":removeProcessesFromBatchSelection")
    private WebElement removeProcessesFromBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":downloadDocket")
    private WebElement downloadDocketForBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = "createBatchForm:batchTitle")
    private WebElement createBatchTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "createBatchForm:save")
    private WebElement createBatchSaveButton;

    @SuppressWarnings("unused")
    @FindBy(id = "renameBatchForm:batchTitle")
    private WebElement renameBatchTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "renameBatchForm:save")
    private WebElement renameBatchSaveButton;

    public ProcessesPage() {
        super("pages/processes.jsf");
    }

    /**
     * Goes to processes page.
     *
     * @return The processes page.
     */
    @Override
    public ProcessesPage goTo() throws Exception {
        Pages.getTopNavigation().gotoProcesses();
        await("Wait for execution of link click").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
        return this;
    }

    /**
     * Go to edit page for creating a new project.
     *
     * @return project edit page
     */
    public ProcessEditPage editProcess() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        clickButtonAndWaitForRedirect(editProcessLink, Pages.getProcessEditPage().getUrl());
        return Pages.getProcessEditPage();
    }

    public int countListedProcesses() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(processesTable).size();
    }

    public int countListedBatches() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());
        Select batchSelect = new Select(batchesSelect);
        return batchSelect.getOptions().size();
    }

    /**
     * Returns a list of all processes titles which were displayed on process page.
     *
     * @return list of processes titles
     */
    public List<String> getProcessTitles() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(processesTable, 3);
    }

    public void createNewBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select processSelect = new Select(processesSelect);
        processSelect.selectByIndex(0);
        processSelect.selectByIndex(1);

        possibleProcessActionsButton.click();
        await(WAIT_FOR_ACTIONS_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> createBatchLink.isDisplayed());
        createBatchLink.click();

        createBatchTitleInput.sendKeys("SeleniumBatch");
        createBatchSaveButton.click();
    }

    public void editBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("Third batch (2 Vorgänge)");

        await("Wait for select list to be visible").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> processesSelect.isDisplayed());
        Select processSelect = new Select(processesSelect);
        processSelect.deselectAll();
        processSelect.selectByVisibleText("First process [1]");

        await(WAIT_FOR_ACTIONS_BUTTON).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> possibleProcessActionsButton.isDisplayed());
        possibleProcessActionsButton.click();
        await(WAIT_FOR_ACTIONS_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> removeProcessesFromBatchLink.isDisplayed());
        removeProcessesFromBatchLink.click();

        await(WAIT_FOR_ACTIONS_BUTTON).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> possibleProcessActionsButton.isDisplayed());
        possibleProcessActionsButton.click();
        await(WAIT_FOR_ACTIONS_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> renameBatchLink.isDisplayed());
        renameBatchLink.click();
        renameBatchTitleInput.sendKeys("SeleniumBatch");
        renameBatchSaveButton.click();
    }

    public void deleteBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("Third batch (2 Vorgänge)");

        await(WAIT_FOR_ACTIONS_BUTTON).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> possibleBatchActionsButton.isDisplayed());
        possibleBatchActionsButton.click();
        await(WAIT_FOR_ACTIONS_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> deleteBatchLink.isDisplayed());
        deleteBatchLink.click();
    }

    public void downloadDocketForBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("Third batch (2 Vorgänge)");

        downloadDocketForBatchLink.click();

        await("Wait for docket file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> isFileDownloaded.test(new File(Browser.DOWNLOAD_DIR + PROCESS_TITLE + ".pdf")));
    }

    public void downloadDocket() {
        setDownloadDocketLink();
        downloadDocketLink.click();

        await("Wait for docket file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isFileDownloaded.test(
                    new File(Browser.DOWNLOAD_DIR + Helper.getNormalizedTitle(PROCESS_TITLE) + ".pdf")));
    }

    public void downloadLog() {
        setDownloadLogLink();
        downloadLogLink.click();

        await("Wait for log file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> isFileDownloaded.test(new File(KitodoConfig.getParameter(ParameterCore.DIR_USERS)
                        + "kowal/" + Helper.getNormalizedTitle(PROCESS_TITLE) + "_log.xml")));
    }

    public void editMetadata() throws IllegalAccessException, InstantiationException {
        setEditMetadataLink();
        clickButtonAndWaitForRedirect(editMetadataLink, Pages.getMetadataEditorPage().getUrl());
    }

    public void downloadSearchResultAsExcel() {
        actionsButton.click();
        await(WAIT_FOR_ACTIONS_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> downloadSearchResultAsExcel.isDisplayed());
        downloadSearchResultAsExcel.click();

        await("Wait for search result excel file download").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isFileDownloaded.test(new File(Browser.DOWNLOAD_DIR + "search.xls")));
    }

    public void downloadSearchResultAsPdf() {
        actionsButton.click();
        await(WAIT_FOR_ACTIONS_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> downloadSearchResultAsPdf.isDisplayed());
        downloadSearchResultAsPdf.click();

        await("Wait for search result pdf file download").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isFileDownloaded.test(new File(Browser.DOWNLOAD_DIR + "search.pdf")));
    }

    private void setDownloadDocketLink() {
        int index = getRowIndex(processesTable, PROCESS_TITLE, 3);
        downloadDocketLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":downloadDocket");
    }

    private void setEditMetadataLink() {
        int index = getRowIndex(processesTable, PROCESS_TITLE, 3);
        editMetadataLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":readXML");
    }

    private void setDownloadLogLink() {
        int index = getRowIndex(processesTable, PROCESS_TITLE, 3);
        downloadLogLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":exportLogXml");
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @param index
     *            of tab to be clicked
     */
    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, processesTabView);
    }

    public void navigateToExtendedSearch() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(searchForProcessesButton, Pages.getExtendedSearchPage().getUrl());
    }

    /**
     * Clicks the header of the title column of the processes table in order to 
     * trigger sorting the processes list by title.
     */
    public void clickProcessesTitleColumnForSorting() {
        // remember aria-sort attribute of th-tag of title column
        String previousAriaSort = processesTableTitleColumn.getAttribute("aria-sort");

        // click title th-tag to trigger sorting
        processesTableTitleColumn.click();

        // wait for the sorting to be applied (which requires ajax request to backend)
        await("title column sorting changed")
            .pollDelay(200, TimeUnit.MILLISECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .ignoreExceptions()
            .until(new Callable<Boolean>() {
                public Boolean call() {
                    // check aria-sort attribute has changed (either empty, "ascending" or "descending")
                    return !processesTableTitleColumn.getAttribute("aria-sort").equals(previousAriaSort);
                }
            });
    }
}
