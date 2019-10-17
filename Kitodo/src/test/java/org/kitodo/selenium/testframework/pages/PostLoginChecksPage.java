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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PostLoginChecksPage extends Page<PostLoginChecksPage> {

    @FindBy(id = "retryForm:logoutButton")
    private WebElement logoutButton;

    public PostLoginChecksPage() {
        super("pages/checks.jsf");
    }

    @Override
    public PostLoginChecksPage goTo() {
        // this page is never navigated to directly by the user
        return null;
    }

    public void logout() {
        clickElement(logoutButton);
    }

}
