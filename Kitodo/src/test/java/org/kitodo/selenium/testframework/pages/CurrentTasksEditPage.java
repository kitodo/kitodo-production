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

import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.awaitility.Awaitility.await;

public class CurrentTasksEditPage extends Page<CurrentTasksEditPage> {

    private static final String ACTION_FORM = "tasksTabView:actionForm";

    @SuppressWarnings("unused")
    @FindBy(id = ACTION_FORM + ":close")
    private WebElement closeTaskLink;

    @SuppressWarnings("unused")
    @FindBy(id = ACTION_FORM + ":cancel")
    private WebElement releaseTaskLink;

    @SuppressWarnings("unused")
    @FindBy(id = "yesButton")
    private WebElement confirmButton;

    public CurrentTasksEditPage() {
        super("pages/currentTasksEdit.jsf");
    }

    @Override
    public CurrentTasksEditPage goTo() {
        return null;
    }

    public void closeTask() throws Exception {
        closeTaskLink.click();

        await("Wait for 'confirm close' dialog to be displayed")
                .atMost(Browser.getDelayAfterDelete(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(() -> confirmButton.isDisplayed());
        confirmButton.click();

        Thread.sleep(Browser.getDelayAfterDelete());
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60);
        wait.until(ExpectedConditions.urlContains(Pages.getTasksPage().getUrl()));
    }

    public void releaseTask() throws Exception {
        releaseTaskLink.click();

        await("Wait for 'confirm release' dialog to be displayed")
                .atMost(Browser.getDelayAfterDelete(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(() -> confirmButton.isDisplayed());
        confirmButton.click();

        Thread.sleep(Browser.getDelayAfterDelete());
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 60);
        wait.until(ExpectedConditions.urlContains(Pages.getTasksPage().getUrl()));
    }
}
