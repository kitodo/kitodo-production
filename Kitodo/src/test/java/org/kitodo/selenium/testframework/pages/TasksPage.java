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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TasksPage extends Page<TasksPage> {

    private static final String TASKS_TAB_VIEW = "tasksTabView";
    private static final String TASK_TABLE = TASKS_TAB_VIEW + ":taskTable";
    private static final String FILTER_FORM = TASKS_TAB_VIEW + ":filterForm";

    @SuppressWarnings("unused")
    @FindBy(id = TASK_TABLE + "_data")
    private WebElement taskTable;

    private WebElement editTaskLink;

    private WebElement takeTaskLink;

    @SuppressWarnings("unused")
    @FindBy(id = FILTER_FORM + ":onlyOpenTasks")
    private WebElement showOnlyOpenTasksCheckbox;

    @SuppressWarnings("unused")
    @FindBy(id = FILTER_FORM + ":applyFilter")
    private WebElement applyFilterLink;

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
        WebElement detailsTable = Browser.getDriver()
                .findElementById(TASK_TABLE + ":" + index + ":currentTaskDetailTable");
        return getTableDataByColumn(detailsTable, 1);
    }

    public void applyFilterShowOnlyOpenTasks() {
        showOnlyOpenTasksCheckbox.click();
        applyFilterLink.click();
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

    public void editOwnedTask(int id) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        setEditTaskLink(id);
        editTaskLink.click();
    }

    private void setEditTaskLink(int id) {
        editTaskLink = Browser.getDriver()
                .findElementByXPath("//a[@href='/kitodo/pages/currentTasksEdit.jsf?id=" + id + "']");
    }

    private void setTakeTaskLink(String taskTitle, String processTitle) {
        int index = getRowIndexForTask(taskTable, taskTitle, processTitle);
        takeTaskLink = Browser.getDriver().findElementById(TASK_TABLE + ":" + index + ":actions:take");
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
