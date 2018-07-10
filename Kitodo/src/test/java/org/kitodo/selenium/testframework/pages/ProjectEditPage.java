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

import org.apache.commons.lang.SystemUtils;
import org.kitodo.data.database.beans.Project;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProjectEditPage extends Page {

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:save")
    private WebElement saveProjectButton;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:projectTabView:title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:projectTabView:pages")
    private WebElement pagesAmountInput;

    @SuppressWarnings("unused")
    @FindBy(id = "editForm:projectTabView:band")
    private WebElement volumeAmountInput;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-selectonemenu-trigger")
    private WebElement selectTrigger;

    public ProjectEditPage() {
        super("pages/projectEdit.jsf");
    }

    public ProjectEditPage insertProjectData(Project project) {
        titleInput.sendKeys(project.getTitle());
        pagesAmountInput.clear();
        pagesAmountInput.sendKeys(project.getNumberOfPages().toString());
        volumeAmountInput.clear();
        volumeAmountInput.sendKeys(project.getNumberOfVolumes().toString());
        selectTrigger.click();
        WebElement option = Browser.getDriver().findElement(By.id("editForm:projectTabView:client_1"));
        option.click();
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        Browser.clickAjaxSaveButton(saveProjectButton);
        WebDriverWait wait = new WebDriverWait(Browser.getDriver(), 30); //seconds
        wait.until(ExpectedConditions.urlContains(Pages.getProjectsPage().getUrl()));
        return Pages.getProjectsPage();
    }
}
