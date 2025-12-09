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
    private static final String SELECT_FILE_BUTTON_BAR = ".ui-fileupload-buttonbar .ui-button";
    private static final String UPLOAD_FILE_INPUT = "editForm:csvFileUpload_input";
    private static final String GBV = "GBV";
    private static final String OK_BUTTON_ID = "buttonForm:okButton";

    public MassImportPage() {
        super("pages/massImport");
    }

    @Override
    public MassImportPage goTo() throws Exception {
        return null;
    }

    /**
     * Click 'OK' on popup dialog to acknowledge given explanation about mass import.
     */
    public void acknowledgeExplanationDialog() {
        clickElement(Browser.getDriver().findElement(By.id(OK_BUTTON_ID)));
    }

    /**
     * Upload CSV test file.
     *
     * @param filepath file path of CSV test file
     */
    public void uploadTestCsvFile(String filepath) {
        Browser.getDriver().findElement(By.id(UPLOAD_FILE_INPUT)).sendKeys(filepath);
        await("Wait for 'Upload' button to become displayed").pollDelay(300, TimeUnit.MILLISECONDS)
                        .atMost(3, TimeUnit.SECONDS).ignoreExceptions().until(() -> Browser.getDriver()
                        .findElements(By.cssSelector(SELECT_FILE_BUTTON_BAR)).get(1).isDisplayed());
        Browser.getDriver().findElements(By.cssSelector(SELECT_FILE_BUTTON_BAR)).get(1).click();
    }

    /**
     * Select GBV option from catalogue select menu.
     */
    public void selectCatalogueGbv() {
        clickElement(Browser.getDriver().findElement(By.id(CATALOG_SELECTION)));
        clickElement(Browser.getDriver().findElement(By.cssSelector("li[data-label='" + GBV + "']")));
    }
}
