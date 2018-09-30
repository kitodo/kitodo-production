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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TasksPage extends Page<TasksPage> {

    private static final String TASKS_TAB_VIEW = "tasksTabView";
    private static final String TASK_TABLE = TASKS_TAB_VIEW + ":taskTable";
    private static final String FILTER_FORM = TASKS_TAB_VIEW + ":filterForm";

    @SuppressWarnings("unused")
    @FindBy(id = TASK_TABLE + "_data")
    private WebElement taskTable;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/currentTasksEdit.jsf?id=8']")
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
        int index = triggerRowToggle(taskTable, "Processed");
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

    public void takeOpenTask() throws Exception {
        setTakeTaskLink();
        takeTaskLink.click();
    }

    public void editOwnedTask() {
        editTaskLink.click();
    }

    private void setTakeTaskLink() {
        int index = getRowIndex(taskTable, "Blocking");
        takeTaskLink = Browser.getDriver().findElementById(TASK_TABLE + ":" + index + ":actions:take");
    }
}
