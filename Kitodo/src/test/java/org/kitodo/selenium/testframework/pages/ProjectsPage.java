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

public class ProjectsPage {

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

}
