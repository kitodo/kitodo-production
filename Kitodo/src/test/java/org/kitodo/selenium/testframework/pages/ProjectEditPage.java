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

import org.kitodo.data.database.beans.Project;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProjectEditPage extends Page<ProjectEditPage> {

    private static final String EDIT_FORM = "editForm";
    private static final String PROJECT_TAB_VIEW = EDIT_FORM + ":projectTabView";

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":save")
    private WebElement saveProjectButton;

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
    @FindBy(className = "ui-selectonemenu-trigger")
    private WebElement selectTrigger;

    public ProjectEditPage() {
        super("pages/projectEdit.jsf");
    }

    @Override
    public ProjectEditPage goTo() {
        return null;
    }

    public ProjectEditPage insertProjectData(Project project) {
        titleInput.sendKeys(project.getTitle());
        pagesAmountInput.clear();
        pagesAmountInput.sendKeys(project.getNumberOfPages().toString());
        volumeAmountInput.clear();
        volumeAmountInput.sendKeys(project.getNumberOfVolumes().toString());
        selectTrigger.click();
        WebElement option = Browser.getDriver().findElement(By.id(PROJECT_TAB_VIEW + ":client_1"));
        option.click();
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveProjectButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }
}
