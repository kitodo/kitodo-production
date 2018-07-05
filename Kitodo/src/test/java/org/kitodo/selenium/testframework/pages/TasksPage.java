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

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;

import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TasksPage extends Page<TasksPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "tasksTabView:taskTable_data")
    private WebElement tasksTable;

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
        return this;
    }

    public int countListedTasks() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(tasksTable).size();
    }
}
