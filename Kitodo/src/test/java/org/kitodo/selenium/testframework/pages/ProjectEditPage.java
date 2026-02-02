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

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.kitodo.data.database.beans.Project;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProjectEditPage extends EditPage<ProjectEditPage> {

    private static final String PROJECT_TAB_VIEW = EDIT_FORM + ":projectTabView";
    private static final String SAVE_BUTTON_ID = EDIT_FORM + ":save";

    @SuppressWarnings("unused")
    @FindBy(id = PROJECT_TAB_VIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = PROJECT_TAB_VIEW + ":pages")
    private WebElement pagesAmountInput;

    @SuppressWarnings("unused")
    @FindBy(id = PROJECT_TAB_VIEW + ":band")
    private WebElement volumeAmountInput;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-chkbox-box")
    private WebElement projectActiveCheckbox;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:detailLockedButton")
    private WebElement detailLockedButton;

    public ProjectEditPage() {
        super("pages/projectEdit.jsf");
    }

    @Override
    public ProjectEditPage goTo() {
        return null;
    }

    public ProjectEditPage insertProjectData(Project project) {
        titleInput.clear();
        titleInput.sendKeys(project.getTitle());
        pagesAmountInput.clear();
        pagesAmountInput.sendKeys(project.getNumberOfPages().toString());
        volumeAmountInput.clear();
        volumeAmountInput.sendKeys(project.getNumberOfVolumes().toString());
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }

    public ProjectsPage saveOnce() throws IllegalAccessException, InstantiationException {
        WebDriverWait webDriverWait = new WebDriverWait(Browser.getDriver(), Duration.ofSeconds(60));
        Browser.getDriver().findElement(By.id(SAVE_BUTTON_ID)).click();
        if (Browser.isAlertPresent() && Pages.getProjectsPage().getUrl().contains("login")) {
            Browser.getDriver().switchTo().alert().accept();
        }
        webDriverWait.until(ExpectedConditions.urlContains(Pages.getProjectsPage().getUrl()));
        return Pages.getProjectsPage();
    }

    public void changeTitle(String newTitle) throws InterruptedException {
        if (!areElementsEnabled()) {
            detailLockedButton.click();
            await("Wait for button clicked").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                    .ignoreExceptions().until(() -> titleInput.isEnabled());
        }
        titleInput.clear();
        titleInput.sendKeys(newTitle);
        pagesAmountInput.click();
    }

    public void changeTitleKeepFocus(String newTitle) throws InterruptedException {
        if (!areElementsEnabled()) {
            detailLockedButton.click();
            await("Wait for button clicked").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                    .ignoreExceptions().until(() -> titleInput.isEnabled());
        }
        titleInput.sendKeys(newTitle);
    }

    public boolean areElementsEnabled() {
        return titleInput.isEnabled() && pagesAmountInput.isEnabled() && volumeAmountInput.isEnabled();
    }

    public boolean isSaveButtonEnabled() {
        return Browser.getDriver().findElement(By.id(SAVE_BUTTON_ID)).isEnabled();
    }

    public ProjectEditPage toggleProjectActiveCheckbox() throws InterruptedException {
        if (!areElementsEnabled()) {
            detailLockedButton.click();
            await("Wait for button clicked").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                    .ignoreExceptions().until(() -> titleInput.isEnabled());
        }
        projectActiveCheckbox.click();
        Thread.sleep(2000);
        detailLockedButton.click();
        await("Wait for button clicked").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> !titleInput.isEnabled());
        return this;
    }
}
