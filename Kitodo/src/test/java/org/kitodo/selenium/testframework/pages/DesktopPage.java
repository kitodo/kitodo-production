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

public class DesktopPage extends Page<DesktopPage>{

    public DesktopPage() {
        super("pages/desktop.jsf");
    }

    @Override
    public DesktopPage goTo() {
        return null;
    }
}
