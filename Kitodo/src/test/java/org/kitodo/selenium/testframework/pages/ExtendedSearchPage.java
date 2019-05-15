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

import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ExtendedSearchPage extends Page<ExtendedSearchPage> {

    private static final String EDIT_FORM = "editForm";

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":submitSearch")
    private WebElement submitButton;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":searchTabView:processID")
    private WebElement processIdInput;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":searchTabView:processProject")
    private WebElement projectDrowdown;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":searchTabView:taskStatus")
    private WebElement taskStatusDropDown;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":searchTabView:taskName")
    private WebElement taskNameDropDown;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":searchTabView:taskName_1")
    private WebElement taskAdditional;

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":searchTabView:taskStatus_3")
    private WebElement taskDone;

    public ExtendedSearchPage() {
        super("searchProcess.jsf");
    }

    @Override
    public ExtendedSearchPage goTo() throws IllegalAccessException, InstantiationException {
        Pages.getTopNavigation().gotoProcesses();
        Pages.getProcessesPage().navigateToExtendedSearch();
        return Pages.getExtendedSearchPage();
    }

    public ProcessesPage searchById(String processId) throws IllegalAccessException, InstantiationException {
        processIdInput.clear();
        processIdInput.sendKeys(processId);
        return triggerSearch();

    }

    public ProcessesPage seachByTaskStatus() throws InstantiationException, IllegalAccessException {
        taskNameDropDown.click();
        taskAdditional.click();

        taskStatusDropDown.click();
        taskDone.click();
        return triggerSearch();
    }

    private ProcessesPage triggerSearch() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(submitButton, Pages.getProcessesPage().getUrl());
        return Pages.getProcessesPage();
    }
}
