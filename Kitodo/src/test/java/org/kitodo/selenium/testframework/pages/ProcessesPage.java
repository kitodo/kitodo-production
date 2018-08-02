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

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

public class ProcessesPage extends Page<ProcessesPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "processesTabView:processesForm:processesTable_data")
    private WebElement processesTable;

    @SuppressWarnings("unused")
    @FindBy(id = "processesTabView:processesForm:processesTable:0:deleteProcess")
    private WebElement removeFirstProcessButton;

    @SuppressWarnings("unused")
    @FindBy(id = "yesButton")
    private WebElement confirmRemoveButton;

    @SuppressWarnings("unused")
    @FindBy(id = "noButton")
    private WebElement cancelRemoveButton;

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

    public int countListedProcesses() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(processesTable).size();
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
        return getTableDataByColumn(processesTable, 0);
    }

    public void deleteFirstProcess() throws Exception {
        if (!isAt()) {
            goTo();
        }
        removeFirstProcessButton.click();
        await("Wait for 'confirm delete' button to be displayed")
                .atMost(Browser.getDelayAfterNewItemClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(() -> confirmRemoveButton.isDisplayed());
        confirmRemoveButton.click();
        Thread.sleep(Browser.getDelayAfterDelete());
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60); // seconds
        wait.until(ExpectedConditions.urlContains(Pages.getProcessesPage().getUrl()));
    }

}
