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

public class HelpPage extends Page<HelpPage> {

    public HelpPage() {
        super("help");
    }

    /**
     * Goes to help page.
     *
     * @return The help page.
     */
    @Override
    public HelpPage goTo() throws Exception {
        Pages.getTopNavigation().gotoHelp();
        return this;
    }
}
