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

public class MetadataEditorPage extends Page<MetadataEditorPage> {

    @SuppressWarnings("unused")
    @FindBy(id = "structureTreeForm")
    private WebElement structureTreeForm;

    public MetadataEditorPage() {
        super("metadataEditor.jsf");
    }

    @Override
    public MetadataEditorPage goTo() {
        return null;
    }

    public boolean isStructureTreeFormVisible() {
        return structureTreeForm.isDisplayed();
    }
}
