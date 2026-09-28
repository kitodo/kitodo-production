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

package org.kitodo.production.forms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.forms.FolderGenerator;

public class FolderGeneratorTest {

    private static final String DERIVATIVE = "createDerivative";
    private static final String DPI = "changeDpi";
    private static final String IMAGE_SIZE = "getSizedWebImage";

    @Test
    public void shouldGetMethod() {
        Folder derivativeFolder = new Folder();
        derivativeFolder.setDerivative(0.75d);
        Folder dpiFolder = new Folder();
        dpiFolder.setDpi(300);
        Folder imageSizeFolder = new Folder();
        imageSizeFolder.setImageSize(150);
        FolderGenerator derivativeFolderGenerator = new FolderGenerator(derivativeFolder);
        assertEquals(DERIVATIVE, derivativeFolderGenerator.getMethod());
        FolderGenerator dpiFolderGenerator = new FolderGenerator(dpiFolder);
        assertEquals(DPI, dpiFolderGenerator.getMethod());
        FolderGenerator imageSizeFolderGenerator = new FolderGenerator(imageSizeFolder);
        assertEquals(IMAGE_SIZE, imageSizeFolderGenerator.getMethod());
    }

    @Test
    public void shouldSetMethod() {
        Folder folder = new Folder();
        FolderGenerator folderGenerator = new FolderGenerator(folder);
        folderGenerator.setMethod(DERIVATIVE);
        assertTrue(folder.getDerivative().isPresent(), "Derivative should be set");
        assertEquals(1.00d, folder.getDerivative().get());
        assertFalse(folder.getDpi().isPresent(), "DPI should be null");
        assertFalse(folder.getImageSize().isPresent(), "Image size should be null");
        folderGenerator.setMethod(DPI);
        assertFalse(folder.getDerivative().isPresent(), "Derivative should be null");
        assertTrue(folder.getDpi().isPresent(), "DPI should be set");
        assertFalse(folder.getImageSize().isPresent(), "Image size should be null");
        folderGenerator.setMethod(IMAGE_SIZE);
        assertFalse(folder.getDerivative().isPresent(), "Derivative should be null");
        assertFalse(folder.getDpi().isPresent(), "DPI should be null");
        assertTrue(folder.getImageSize().isPresent(), "Image size should be set");
    }

}
