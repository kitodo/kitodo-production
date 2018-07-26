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

public class StartPage extends Page<StartPage> {

    public StartPage() {
        super("pages/desktop.jsf");
    }

    @Override
    public StartPage goTo() {
        Browser.goTo(this.getUrl());
        return this;
    }
}
