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
import static org.kitodo.selenium.testframework.Browser.getSelectedRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.helper.Helper;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class ProcessesPage extends Page<ProcessesPage> {

    private static final String PROCESSES_TAB_VIEW = "processesTabView";
    private static final String PROCESSES_FORM = PROCESSES_TAB_VIEW + ":processesForm";
    private static final String BATCH_FORM = PROCESSES_TAB_VIEW + ":batchForm";
    private static final String PROCESSES_TABLE = PROCESSES_FORM + ":processesTable";
    private static final String PROCESSES_TABLE_HEADER = PROCESSES_TABLE + "_head";
    private static final String FILTER_INPUT = "filterInputForm:filterfield";
    private static final String PARSED_FILTERS = "#parsedFiltersForm\\:parsedFilters .ui-datalist-item";
    private static final String SECOND_PROCESS_TITLE = "Second process";
    private static final String PARENT_PROCESS_TITLE = "Parent process";
    private static final String WAIT_FOR_ACTIONS_BUTTON = "Wait for actions menu button";
    private static final String WAIT_FOR_ACTIONS_MENU = "Wait for actions menu to open";
    private static final String WAIT_FOR_COLUMN_SORT = "Wait for column sorting";
    private static final String MULTI_VOLUME_WORK_PROCESS_TITLE = "Multi volume work test process";
    private static final String WAIT_FOR_SELECTION_MENU = "Wait for process selection menu to open";
    private static final String CALENDER_ACTION_XPATH = "//a[@href='/kitodo/pages/calendarEdit.jsf?id=%s']";

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TAB_VIEW)
    private WebElement processesTabView;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TABLE + DATA)
    private WebElement processesTable;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TABLE_HEADER)
    private WebElement processesTableHeader;

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

    @SuppressWarnings("unused")
    @FindBy(id = FILTER_INPUT)
    private WebElement filterInput;

    @FindBy(css = ".ui-chkbox-all .ui-chkbox-box")
    private WebElement selectAllCheckBox;

    @FindBy(id = PROCESSES_FORM + ":selectAllRowsOnPage")
    private WebElement selectAllRowsOnPageLink;

    @FindBy(id = PROCESSES_FORM + ":selectAllRows")
    private WebElement selectAllRowsLink;

    @FindBy(className = "ui-paginator-next")
    private WebElement nextPage;

    @FindBy(id = "headerText")
    private WebElement headerText;

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
     * Gets number of selected rows in processesTable.
     * @return number of selected rows
     * @throws Exception exception
     */
    public long countListedSelectedProcesses() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getSelectedRowsOfTable(processesTable);
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

    /**
     * Returns a list of all process IDs which were displayed on process page.
     *
     * @return list of process IDs
     * @throws Exception when navigating to processes page fails
     */
    public List<String> getProcessIds() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(processesTable, 2);
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
                .until(() -> isFileDownloaded.test(new File(Browser.DOWNLOAD_DIR + SECOND_PROCESS_TITLE + ".pdf")));
    }

    public void downloadDocket() {
        setDownloadDocketLink();
        downloadDocketLink.click();

        await("Wait for docket file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isFileDownloaded.test(
                    new File(Browser.DOWNLOAD_DIR + Helper.getNormalizedTitle(SECOND_PROCESS_TITLE) + ".pdf")));
    }

    public void downloadLog() {
        setDownloadLogLink();
        downloadLogLink.click();

        await("Wait for log file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> isFileDownloaded.test(new File(KitodoConfig.getParameter(ParameterCore.DIR_USERS)
                        + "kowal/" + Helper.getNormalizedTitle(SECOND_PROCESS_TITLE) + "_log.xml")));
    }

    /**
     * Set 'edit metadata' link for default process with title saved in PROCESS_TITLE and click it.
     * @throws IllegalAccessException when retrieving metadata editor page fails
     * @throws InstantiationException when retrieving metadata editor page fails
     */
    public void editMetadata() throws IllegalAccessException, InstantiationException {
        setEditMetadataLink(SECOND_PROCESS_TITLE);
        clickButtonAndWaitForRedirect(editMetadataLink, Pages.getMetadataEditorPage().getUrl());
    }

    /**
     * Set 'edit metadata' link for default process with given title 'processTitle' and click it.
     * @param processTitle title of process whose 'edit metadata' link is clicked
     * @throws InstantiationException when retrieving metadata editor page fails
     * @throws IllegalAccessException when retrieving metadata editor page fails
     */
    public void editMetadata(String processTitle) throws InstantiationException, IllegalAccessException {
        setEditMetadataLink(processTitle);
        clickButtonAndWaitForRedirect(editMetadataLink, Pages.getMetadataEditorPage().getUrl());
    }

    /**
     * Open second process in metadata editor.
     * @throws IllegalAccessException when navigating to metadata editor page fails
     * @throws InstantiationException when navigating to metadata editor page fails
     */
    public void editSecondProcessMetadata() throws IllegalAccessException, InstantiationException {
        try {
            setEditMetadataLink(SECOND_PROCESS_TITLE);
            clickButtonAndWaitForRedirect(editMetadataLink, Pages.getMetadataEditorPage().getUrl());
        } catch (StaleElementReferenceException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open parent process in metadata editor.
     * @throws IllegalAccessException when navigating to metadata editor page fails
     * @throws InstantiationException when navigating to metadata editor page fails
     */
    public void editParentProcessMetadata() throws InstantiationException, IllegalAccessException {
        try {
            setEditMetadataLink(PARENT_PROCESS_TITLE);
            clickButtonAndWaitForRedirect(editMetadataLink, Pages.getMetadataEditorPage().getUrl());
        } catch (StaleElementReferenceException e) {
            e.printStackTrace();
        }
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
        int index = getRowIndex(processesTable, SECOND_PROCESS_TITLE, 3);
        downloadDocketLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":downloadDocket");
    }

    /**
     * Set metadata edit link.
     * @param processTitle title of process whose 'edit metadata' link is set
     */
    private void setEditMetadataLink(String processTitle) {
        int index = getRowIndex(processesTable, processTitle, 3);
        editMetadataLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":editMetadata");
    }

    private void setDownloadLogLink() {
        int index = getRowIndex(processesTable, SECOND_PROCESS_TITLE, 3);
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
     * Submits a filter query by typing some text into the input field and submitting the filter form.
     *
     * <p>This method doesn't block until the filter is sucessfully applied.</p>
     *
     * @param filterQuery the query
     */
    public void applyFilter(String filterQuery) {
        filterInput.clear();
        filterInput.sendKeys(filterQuery);
        filterInput.sendKeys(Keys.RETURN);
        await("Wait for loading screen to disappear").pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> filterInput.isDisplayed());
        // hide filter menu to enable action buttons positioned behind it
        headerText.click();
        await("Wait for filter menu to close").pollDelay(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> filterInput.isEnabled());
    }

    public List<WebElement> getParsedFilters() {
        return Browser.getDriver().findElements(By.cssSelector(PARSED_FILTERS));
    }

    /**
     * Remove filter with index 'index' from list of parsed filters.
     * @param index index of filter to remove
     */
    public void removeParsedFilter(int index) {
        getParsedFilters().get(index).findElement(By.tagName("button")).click();
        await("Wait for loading screen to disappear").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> filterInput.isDisplayed());
    }

    /**
     * Clicks a column in the header of the processes table in order to
     * trigger sorting the processes list by that column.
     *
     * @param column the column index
     */
    public void clickProcessesTableHeaderForSorting(int column) {
        WebElement columnHeader = processesTableHeader.findElement(By.cssSelector("tr th:nth-child(" + column + ")"));
        // remember aria-sort attribute of th-tag of title column
        String previousAriaSort = columnHeader.getAttribute("aria-sort");

        // click title th-tag to trigger sorting
        columnHeader.click();

        // wait for the sorting to be applied (which requires ajax request to backend)
        await(WAIT_FOR_COLUMN_SORT)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .ignoreExceptions()
            .until(() -> !columnHeader.getAttribute("aria-sort").equals(previousAriaSort));
    }

    /**
     * Navigate to calendar page to create child processes for process with provided ID 'processId'.
     * @param processId ID of process for which child processes are created using the calendar
     * @throws Exception when navigating to the calendar page fails
     */
    public void goToCalendar(int processId) throws Exception {
        String xpath = String.format(CALENDER_ACTION_XPATH, processId);
        WebElement openCalendarLink = Browser.getDriver().findElementByXPath(xpath);
        if (isNotAt()) {
            goTo();
        }
        openCalendarLink.click();
    }

    /**
     * Select all rows on a page in processesTable.
     */
    public void selectAllRowsOnPage() {
        selectAllCheckBox.click();
        await(WAIT_FOR_SELECTION_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> selectAllRowsOnPageLink.isDisplayed());
        selectAllRowsOnPageLink.click();
        await("Wait for visible processes table").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> processesTable.isDisplayed());
    }

    /**
     * Select all rows on all pages in processesTable.
     */
    public void selectAllRows() {
        selectAllCheckBox.click();
        await(WAIT_FOR_SELECTION_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> selectAllRowsLink.isDisplayed());
        selectAllRowsLink.click();
        await("Wait for visible processes table").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).until(() -> processesTable.isDisplayed());
    }

    /**
     * Go to next page in processesTable.
     */
    public void goToNextPage() {
        if (nextPage.isEnabled()) {
            nextPage.click();
            await("Wait for visible processes table").pollDelay(700, TimeUnit.MILLISECONDS)
                    .atMost(30, TimeUnit.SECONDS).until(() -> processesTable.isDisplayed());
        }
    }

    /**
     * Clicks 'create child process' link in actions column and waits for redirect to 'create new process' page.
     * @throws InstantiationException when retrieving process from template page fails
     * @throws IllegalAccessException when retrieving process from template page fails
     */
    public void createChildProcess() throws InstantiationException, IllegalAccessException {
        List<WebElement> processTitleCells = processesTable.findElements(By.cssSelector("tr td:nth-child(4)"));
        Optional<WebElement> mvwTitleCell = processTitleCells.stream().filter(row -> row.getText()
                .equals(MULTI_VOLUME_WORK_PROCESS_TITLE)).findFirst();
        if (mvwTitleCell.isPresent()) {
            int rowIndex = processTitleCells.indexOf(mvwTitleCell.get());
            WebElement createLink = processesTable.findElement(By.id(PROCESSES_TABLE + ":" + rowIndex
                    + ":createChildren"));
            clickButtonAndWaitForRedirect(createLink, Pages.getProcessFromTemplatePage().getUrl());
        } else {
            throw new NoSuchElementException("Unable to find table row for process with title '"
                    + MULTI_VOLUME_WORK_PROCESS_TITLE + "'");
        }
    }

    /**
     * Toggles first row expansion in process list.
     */
    public void filterByChildren() {
        WebElement rowToggler = processesTable.findElement(By.className("ui-row-toggler"));
        rowToggler.click();
        await("Wait for row expansion to become visible").pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS).atMost(3, TimeUnit.SECONDS)
                .until(() -> Browser.getDriver().findElement(By.className("row-expansion-wrapper")).isDisplayed());
        WebElement rowExpansion = Browser.getDriver().findElement(By.className("row-expansion-wrapper"));
        WebElement childFilterLink = rowExpansion.findElement(By.cssSelector(".value a"));
        childFilterLink.click();
        await("Wait for execution of link click").pollDelay(1, TimeUnit.SECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
    }
}
