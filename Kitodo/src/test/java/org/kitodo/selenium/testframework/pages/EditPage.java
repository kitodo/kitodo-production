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

abstract class EditPage<T> extends Page<T> {

    static final String EDIT_FORM = "editForm";

    @SuppressWarnings("unused")
    @FindBy(id = EDIT_FORM + ":save")
    WebElement saveButton;

    EditPage(String URL) {
        super(URL);
    }
}
