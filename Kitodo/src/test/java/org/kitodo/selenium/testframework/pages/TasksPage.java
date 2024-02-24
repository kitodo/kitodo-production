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
import static org.junit.Assert.assertTrue;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;
import static org.kitodo.selenium.testframework.Browser.scrollWebElementIntoView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TasksPage extends Page<TasksPage> {

    private static final String TASKS_TAB_VIEW = "tasksTabView";
    private static final String TASK_TABLE = TASKS_TAB_VIEW + ":tasksForm:taskTable";
    private static final String TASK_TABLE_DATA = TASK_TABLE + "_data";
    private static final String TASK_TABLE_HEADER = TASK_TABLE + "_head";
    private static final String SUGGESTION_ITEMS = "#filterOptionsForm\\:suggestions .suggestion";
    private static final String WAIT_FOR_FILTER_FORM_MENU = "Wait for filter form menu to open";
    private static final String WAIT_FOR_TASK_TABLE_COLUMN_SORT = "Wait for task table column sort";

    @SuppressWarnings("unused")
    @FindBy(id = TASK_TABLE_DATA)
    private WebElement taskTable;

    @SuppressWarnings("unused")
    @FindBy(id = TASK_TABLE_HEADER)
    private WebElement taskTableHeader;

    private WebElement editTaskLink;

    private WebElement takeTaskLink;

    @FindBy(id = "filterInputForm:filterfield")
    private WebElement filterField;

    @SuppressWarnings("unused")
    @FindBy(id = "filterOptionsFormWrapper")
    private WebElement filterOptionsMenu;

    @FindBy(css = "#filterOptionsForm\\:taskStatus tr:last-child .ui-chkbox-box")
    private WebElement inWorkStatusCheckbox;

    public TasksPage() {
        super("pages/tasks.jsf");
    }

    /**
     * Goes to tasks page.
     *
     * @return The tasks page.
     */
    @Override
    public TasksPage goTo() throws Exception {
        Pages.getTopNavigation().gotoTasks();
        await("Wait for execution of link click").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
        return this;
    }

    public List<String> getTaskDetails() {
        int index = triggerRowToggle(taskTable, "Progress");
        String firstElementId = TASK_TABLE + ":" + index + ":taskDetailTableFirst";
        String secondElementId = TASK_TABLE + ":" + index + ":taskDetailTableSecond";
        await("Wait for first task details to become visible").atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(Browser.getDriver().findElement(By.id(firstElementId)).isDisplayed()));
                WebElement firstDetails = Browser.getDriver().findElementById(firstElementId);
        List<String> taskDetails = getTableDataByColumn(firstDetails, 1);
        await("Wait for second task details to become visible").atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(Browser.getDriver().findElement(By.id(secondElementId)).isDisplayed()));
                WebElement secondDetails = Browser.getDriver().findElementById(secondElementId);
        taskDetails.addAll(getTableDataByColumn(secondDetails, 1));
        return taskDetails;
    }

    public void applyFilterShowOnlyOpenTasks() {
        filterField.click();
        await(WAIT_FOR_FILTER_FORM_MENU).pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> filterOptionsMenu.isDisplayed());
        scrollWebElementIntoView(inWorkStatusCheckbox);
        inWorkStatusCheckbox.click();

        await("Wait for task list to be restricted to open tasks").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> taskTable.isDisplayed());
    }

    /**
     * Input given String 'character' into filter field.
     * @param character as String
     */
    public void typeCharactersIntoFilter(String character) {
        filterField.click();
        filterField.sendKeys(character);
        await(WAIT_FOR_FILTER_FORM_MENU).pollDelay(4, TimeUnit.SECONDS)
                .atMost(8, TimeUnit.SECONDS)
                .until(() -> filterOptionsMenu.isDisplayed());
    }

    /**
     * Submit filter.
     */
    public void submitFilter() {
        filterField.sendKeys(Keys.RETURN);
        await("Wait for task list to be filtered").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> taskTable.isDisplayed());
    }

    /**
     * Get suggestions.
     * @return suggestions as list of WebElement
     */
    public List<WebElement> getSuggestions() {
        return Browser.getDriver().findElements(By.cssSelector(SUGGESTION_ITEMS));
    }

    /**
     * Select suggestion with given index 'suggestionIndex'.
     * @param suggestionIndex index of suggestion to select
     */
    public void selectSuggestion(int suggestionIndex) {
        for (int i = 0; i <= suggestionIndex; i++) {
            filterField.sendKeys(Keys.ARROW_DOWN);
        }
        filterField.sendKeys(Keys.RETURN);
    }

    public String getFilterInputValue() {
        return filterField.getAttribute("value");
    }

    public int countListedTasks() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(taskTable).size();
    }

    public void takeOpenTask(String taskTitle, String processTitle) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        setTakeTaskLink(taskTitle, processTitle);
        takeTaskLink.click();
    }

    public void editOwnedTask(String taskTitle, String processTitle) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        setEditTaskLink(taskTitle, processTitle);
        editTaskLink.click();
    }

    /**
     * Clicks the header of the the n-th column of the task table in order to
     * trigger sorting tasks by that column.
     */
    public void clickTaskTableColumnHeaderForSorting(int column) {
        WebElement columnHeader = taskTableHeader.findElement(By.cssSelector("tr th:nth-child(" + column + ")"));
        // remember aria-sort attribute of th-tag of title column
        String previousAriaSort = columnHeader.getAttribute("aria-sort");

        // click title th-tag to trigger sorting
        columnHeader.click();

        // wait for the sorting to be applied (which requires ajax request to backend)
        await(WAIT_FOR_TASK_TABLE_COLUMN_SORT)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .ignoreExceptions()
            .until(() -> !columnHeader.getAttribute("aria-sort").equals(previousAriaSort));
    }

    /**
     * Returns the task title of the first row in the task table.
     *
     * @return the task title
     */
    public String getFirstRowTaskTitle() {
        List<String> taskTitles = getTableDataByColumn(taskTable, 1);
        if (!taskTitles.isEmpty()) {
            return taskTitles.get(0);
        }
        return "";
    }

    private void setEditTaskLink(String taskTitle, String processTitle) {
        int index = getRowIndexForTask(taskTable, taskTitle, processTitle);
        editTaskLink = Browser.getDriver().findElementById(TASK_TABLE + ":" + index + ":editOwnTask");
    }

    private void setTakeTaskLink(String taskTitle, String processTitle) {
        int index = getRowIndexForTask(taskTable, taskTitle, processTitle);
        takeTaskLink = Browser.getDriver().findElementById(TASK_TABLE + ":" + index + ":take");
    }

    private int getRowIndexForTask(WebElement dataTable, String searchedTaskTitle, String searchedProcessTitle) {
        List<WebElement> tableRows = getRowsOfTable(dataTable);

        for (int i = 0; i < tableRows.size(); i++) {
            WebElement tableRow = tableRows.get(i);
            String taskTitle = Browser.getCellDataByRow(tableRow, 1);
            String processTitle = Browser.getCellDataByRow(tableRow, 2);

            if (taskTitle.equals(searchedTaskTitle) && processTitle.equals(searchedProcessTitle)) {
                return i;
            }
        }

        throw new NotFoundException("Row for task title " + searchedTaskTitle + " and process title "
                + searchedProcessTitle + "was not found!");
    }
}
