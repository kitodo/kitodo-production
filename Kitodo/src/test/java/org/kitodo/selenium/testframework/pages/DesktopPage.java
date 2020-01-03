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

import java.util.List;

import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

public class DesktopPage extends Page<DesktopPage> {

    private static final String PROCESS_TABLE = "processTable";
    private static final String PROJECTS_TABLE = "projectTable";
    private static final String TASKS_TABLE = "taskTable";
    private static final String STATISTICS_TABLE = "statisticsTable";

    @SuppressWarnings("unused")
    @FindBy(id = PROCESS_TABLE + DATA)
    private WebElement processTable;

    @SuppressWarnings("unused")
    @FindBy(id = PROJECTS_TABLE + DATA)
    private WebElement projectsTable;

    @SuppressWarnings("unused")
    @FindBy(id = TASKS_TABLE + DATA)
    private WebElement tasksTable;

    @SuppressWarnings("unused")
    @FindBy(id = STATISTICS_TABLE + DATA)
    private WebElement statisticsTable;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='desktop.jsf']")
    private WebElement desktopLink;

    public DesktopPage() {
        super("desktop.jsf");
    }

    @Override
    public DesktopPage goTo() throws Exception {
        clickButtonAndWaitForRedirect(desktopLink, Pages.getDesktopPage().getUrl());
        return this;
    }

    public int countListedProcesses()  throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(processTable).size();
    }

    public int countListedProjects()  throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(projectsTable).size();
    }

    public int countListedTasks()  throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(tasksTable).size();
    }

    public int countListedStatistics()  throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(statisticsTable).size();
    }

    public List<String> getStatistics()  throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(statisticsTable, 1);
    }
}
