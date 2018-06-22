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

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;

public class ProjectsPage extends Page {

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView")
    private WebElement projectsTabView;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:projectsTable_data")
    private WebElement projectsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:templateTable_data")
    private WebElement templatesTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:docketTable_data")
    private WebElement docketsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectsTabView:rulesetTable_data")
    private WebElement rulesetsTable;

    public ProjectsPage() {
       super("pages/projects.jsf");
    }

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
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @return The users page.
     */
    public ProjectsPage switchToTabByIndex(int index) throws Exception {
        if (isNotAt()) {
            goTo();
        }
        clickTab(index, projectsTabView);
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

    public int countListedDockets() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(docketsTable).size();
    }

    public int countListedRulesets() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(rulesetsTable).size();
    }

}
