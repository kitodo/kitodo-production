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
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

abstract class EditPage<T> extends Page<T> {

    static final String EDIT_FORM = "editForm";

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":save")
    WebElement saveButton;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":cancel")
    WebElement cancelButton;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":error-messages")
    WebElement errorMessages;

    EditPage(String URL) {
        super(URL);
    }

    void addRow(List<WebElement> tableRows, String title, WebElement dialog) {
        for (WebElement tableRow : tableRows) {
            if (Browser.getCellDataByRow(tableRow, 0).equals(title)) {
                clickLinkOfTableRow(tableRow);

                // Hold up for some seconds…
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    Logger logger = LogManager.getLogger(EditPage.class);
                    logger.error(e.getMessage(), e);
                }

                Browser.closeDialog(dialog);
                return;
            }
        }
        throw new NoSuchElementException("No row for given value found: " + title);
    }

    private void clickLinkOfTableRow(WebElement tableRow) {

        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 5);
        wait.until(ExpectedConditions.elementToBeClickable(tableRow.findElement(By.tagName("a"))));

        tableRow.findElement(By.tagName("a")).click();
    }
}
