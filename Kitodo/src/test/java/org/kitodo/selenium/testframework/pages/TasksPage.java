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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TasksPage extends Page<TasksPage> {

    private static final String TASK_TABLE = "tasksTabView:taskTable";

    @SuppressWarnings("unused")
    @FindBy(id = TASK_TABLE + "_data")
    private WebElement taskTable;

    @SuppressWarnings("unused")
    @FindBy(id = "tasksTabView:filterForm:onlyOpenTasks")
    private WebElement showOnlyOpenTasksCheckbox;

    @SuppressWarnings("unused")
    @FindBy(id = "tasksTabView:filterForm:applyFilter")
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
                .findElement(By.id(TASK_TABLE + ":" + index + ":currentTaskDetailTable"));
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
}
