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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;

public class ProjectsPage {

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView")
    private WebElement projectsTabView;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:projectsTable_data")
    private WebElement projectsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:templateTable_data")
    private WebElement templatesTable;

    /**
     * Goes to projects page.
     *
     * @return The projects page.
     */
    public ProjectsPage goTo() throws Exception {
        Pages.getTopNavigation().gotoProjects();
        return this;
    }

    /**
     * Checks if the browser is currently at projects page.
     *
     * @return True if browser is at projects page.
     */
    public boolean isAt() {
        return Browser.getCurrentUrl().contains("projects");
    }

    /**
     * Checks if the browser is currently not at users page.
     *
     * @return True if browser is not at users page.
     */
    public boolean isNotAt() {
        return !isAt();
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @return The users page.
     */
    public ProjectsPage switchToTabByIndex(int index) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        List<WebElement> listTabs = projectsTabView.findElements(By.tagName("li"));
        WebElement tab = listTabs.get(index);
        tab.click();
        return this;
    }

    public int countListedProjects() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(projectsTable).size();
    }

    public int countListedTemplates() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(templatesTable).size();
    }

}
