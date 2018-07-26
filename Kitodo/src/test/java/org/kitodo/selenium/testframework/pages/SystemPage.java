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
import java.util.concurrent.TimeUnit;

import org.awaitility.core.Predicate;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.awaitility.Awaitility.await;

public class SystemPage extends Page<SystemPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:indexingTable")
    private WebElement indexingTable;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:createMappingButton")
    private WebElement createMappingButton;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:deleteIndexButton")
    private WebElement deleteIndexButton;

    @SuppressWarnings("unused")
    @FindBy(id = "systemTabView:indexing_form:startIndexingAllButton")
    private WebElement startIndexingAllButton;

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
        return this;
    }

    /**
     * Clicks on "delete index" button and accept dialog.
     */
    private void deleteIndex() throws InterruptedException {
        await("Wait for delete index button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isButtonClicked.matches(deleteIndexButton));

        Predicate<WebDriver> isAlertPresent = (d) -> {
            d.switchTo().alert();
            return true;
        };

        await("Wait for alert").atMost(5, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isAlertPresent.matches(Browser.getDriver()));

        Browser.getDriver().switchTo().alert().accept();
        Thread.sleep(Browser.getDelayIndexing());
    }

    /**
     * Clicks on "create mapping" button.
     */
    private void createMapping() throws InterruptedException {
        await("Wait for create mapping button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isButtonClicked.matches(createMappingButton));
        Thread.sleep(Browser.getDelayIndexing());
    }

    /**
     * Clicks on "start indexing all" button.
     */
    private void startIndexingAll() throws InterruptedException {
        await("Wait for start indexing button").atMost(20, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isButtonClicked.matches(startIndexingAllButton));
        Thread.sleep(Browser.getDelayIndexing());
    }

    /**
     * Deletes the old index, creates the mapping and starts new indexing.
     */
    public void startReindexingAll() throws InterruptedException {
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
    public String getIndexingProgress() {
        List<WebElement> listOfRows = Browser.getRowsOfTable(indexingTable);
        WebElement lastRow = listOfRows.get(listOfRows.size() - 1);
        return lastRow.findElement(By.className("ui-progressbar-label")).getText();
    }
}
