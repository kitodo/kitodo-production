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

import org.kitodo.data.database.beans.Docket;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DocketEditPage extends EditPage<DocketEditPage> {

    private static final String DOCKET_TAB_VIEW = EDIT_FORM + ":docketTabView";

    @SuppressWarnings("unused")
    @FindBy(id = DOCKET_TAB_VIEW + ":title")
    private WebElement titleInput;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-selectonemenu-trigger")
    private WebElement selectTrigger;

    @SuppressWarnings("unused")
    @FindBy(className = "ui-messages-error-summary")
    private WebElement errorMessage;

    public DocketEditPage() {
        super("pages/docketEdit.jsf");
    }

    @Override
    public DocketEditPage goTo() {
        return null;
    }

    public DocketEditPage insertDocketData(Docket docket) {
        titleInput.sendKeys(docket.getTitle());
        selectTrigger.click();
        Browser.getDriver().findElement(By.id("editForm:docketTabView:file_0")).click();
        return this;
    }

    public ProjectsPage save() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(saveButton, Pages.getProjectsPage().getUrl());
        return Pages.getProjectsPage();
    }
}
