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
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class MassImportPage extends Page<MassImportPage> {
    private static final String CATALOG_SELECTION = "editForm:catalogueSelect";
    private static final String RECORDS_TABLE = "editForm:recordsTable";
    private static final String SELECT_FILE_BUTTON_BAR = ".ui-fileupload-buttonbar .ui-button";
    private static final String UPLOAD_FILE_INPUT = "editForm:csvFileUpload_input";
    private static final String GBV = "GBV";
    private static final String CSV_SEPARATOR = "editForm:csvSeparator";

    public MassImportPage() {
        super("pages/massImport.jsf");
    }

    @Override
    public MassImportPage goTo() throws Exception {
        return null;
    }

    public void uploadTestCsvFile(String filepath) {
        clickElement(Browser.getDriver().findElement(By.id(CATALOG_SELECTION)));
        clickElement(Browser.getDriver().findElement(By.cssSelector("li[data-label='" + GBV + "']")));
        Browser.getDriver().findElement(By.id(UPLOAD_FILE_INPUT)).sendKeys(filepath);
        await("Wait for 'Upload' button to become displayed").pollDelay(300, TimeUnit.MILLISECONDS)
                        .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> Browser.getDriver()
                        .findElements(By.cssSelector(SELECT_FILE_BUTTON_BAR)).get(1).isDisplayed());
        Browser.getDriver().findElements(By.cssSelector(SELECT_FILE_BUTTON_BAR)).get(1).click();
    }

    public void updateSeparator(String separator) {
        await("Wait for CSV separator menu to be displayed").pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> Browser.getDriver()
                        .findElement(By.id(CSV_SEPARATOR)).isDisplayed());
        Browser.getDriver().findElement(By.id(CSV_SEPARATOR)).click();
        await("Wait for CSV separator menu option to be displayed").pollDelay(300, TimeUnit.MILLISECONDS)
                        .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> Browser.getDriver()
                        .findElement(By.cssSelector("li[data-label='" + separator + "']")).isDisplayed());
        Browser.getDriver().findElement(By.cssSelector("li[data-label='" + separator + "']")).click();
        await("Wait for records table to update").pollDelay(300, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> Browser.getDriver()
                        .findElement(By.id(RECORDS_TABLE)).isDisplayed());
    }
}
