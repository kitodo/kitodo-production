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
import org.kitodo.selenium.testframework.Pages;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;

public class ProcessesPage {

    @SuppressWarnings("unused")
    @FindBy(id = "processesTabView:processesForm:processesTable_data")
    private WebElement processesTable;

    /**
     * Goes to processes page.
     *
     * @return The processes page.
     */
    public ProcessesPage goTo() throws Exception {
        Pages.getTopNavigation().gotoProcesses();
        return this;
    }

    /**
     * Checks if the browser is currently at processes page.
     *
     * @return True if browser is at processes page.
     */
    public boolean isAt() {
        return Browser.getCurrentUrl().contains("processes");
    }

    public int countListedProcesses() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(processesTable).size();
    }

}
