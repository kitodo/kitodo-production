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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class IndexingPage {

    private static final Logger logger = LogManager.getLogger(IndexingPage.class);

    @SuppressWarnings("unused")
    @FindBy(id = "indexingTable")
    private WebElement indexingTable;

    @SuppressWarnings("unused")
    @FindBy(id = "indexing_form:createMappingButton")
    private WebElement createMappingButton;

    @SuppressWarnings("unused")
    @FindBy(id = "indexing_form:deleteIndexButton")
    private WebElement deleteIndexButton;

    @SuppressWarnings("unused")
    @FindBy(id = "indexing_form:startIndexingAllButton")
    private WebElement startIndexingAllButton;

    /**
     * Goes to indexing page.
     *
     * @return The indexing page.
     */
    public IndexingPage goTo() throws Exception {
        Pages.getTopNavigation().gotoIndexing();
        return this;
    }

    /**
     * Checks if the browser is currently at indexing page.
     *
     * @return True if browser is at indexing page.
     */
    public boolean isAt() throws InterruptedException {
        return Browser.getCurrentUrl().contains("indexing");
    }

    public void deleteIndex() throws InterruptedException {
        deleteIndexButton.click();
        Thread.sleep(Browser.getDelayIndexing());
        Alert javascriptconfirm = Browser.getDriver().switchTo().alert();
        javascriptconfirm.accept();
        Thread.sleep(Browser.getDelayIndexing());
    }

    public void createMapping() throws InterruptedException {
        createMappingButton.click();
        Thread.sleep(Browser.getDelayIndexing());
    }

    public void startIndexingAll() throws InterruptedException {
        startIndexingAllButton.click();
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
        int attempt = 0;
        while (attempt < 3) {
            try {
                List<WebElement> listOfRows = Browser.getRowsOfTable(indexingTable);
                WebElement lastRow = listOfRows.get(listOfRows.size() - 1);
                String value = lastRow.findElement(By.className("ui-progressbar-label")).getText();
                return value;
            } catch (StaleElementReferenceException e) {
                attempt++;
                logger.debug("Indexing progress is not readable, retrying now, " + attempt);
            }
        }
        logger.error("could not read indexing progress");
        return "";
    }

    /**
     * Checks if the indexing progress is at 100%.
     * 
     * @return True if indexing is at 100%.
     */
    public boolean isIndexingComplete() {
        return getIndexingProgress().equals("100%");
    }
}
