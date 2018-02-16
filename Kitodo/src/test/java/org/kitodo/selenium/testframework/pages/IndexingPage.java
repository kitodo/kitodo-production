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

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class IndexingPage {

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

    public IndexingPage goTo() throws Exception {
        Pages.getTopNavigation().gotoIndexing();
        return this;
    }

    public boolean isAt() throws InterruptedException {
        return Browser.getCurrentUrl().contains("indexing");
    }

    public void deleteIndex() throws InterruptedException {
        deleteIndexButton.click();
        Thread.sleep(500);
        Alert javascriptconfirm = Browser.getDriver().switchTo().alert();
        javascriptconfirm.accept();
        Thread.sleep(500);
    }

    public void createMapping() throws InterruptedException {
        createMappingButton.click();
        Thread.sleep(2000);
    }

    public void startIndexingAll() throws InterruptedException {
        startIndexingAllButton.click();
        Thread.sleep(500);
    }

    public void startReindexingAll() throws InterruptedException {
        deleteIndex();
        createMapping();
        startIndexingAll();
    }

    public int getIndexingProgress() {
        List<WebElement> listOfRows = indexingTable.findElements(By.tagName("tr"));
        WebElement lastRow = listOfRows.get(listOfRows.size() - 1);
        String value = lastRow.findElement(By.className("ui-progressbar-label")).getText();
        value = value.replace("%", "");
        return Integer.parseInt(value);
    }

    public boolean isIndexingComplete() {
        return getIndexingProgress() == 100;
    }

}
