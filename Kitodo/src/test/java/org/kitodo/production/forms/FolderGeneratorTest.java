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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.production.forms.dto.FolderDTO;
import org.kitodo.production.forms.helper.FolderGenerator;

public class FolderGeneratorTest {

    private static final String DERIVATIVE = "createDerivative";
    private static final String DPI = "changeDpi";
    private static final String IMAGE_SIZE = "getSizedWebImage";

    @Test
    public void shouldGetMethod() {
        FolderDTO derivativeFolder = new FolderDTO();
        derivativeFolder.setDerivative(0.75d);

        FolderDTO dpiFolder = new FolderDTO();
        dpiFolder.setDpi(300);

        FolderDTO imageSizeFolder = new FolderDTO();
        imageSizeFolder.setImageSize(150);

        FolderGenerator derivativeFolderGenerator = new FolderGenerator(derivativeFolder);
        Assertions.assertEquals(DERIVATIVE, derivativeFolderGenerator.getMethod());

        FolderGenerator dpiFolderGenerator = new FolderGenerator(dpiFolder);
        Assertions.assertEquals(DPI, dpiFolderGenerator.getMethod());

        FolderGenerator imageSizeFolderGenerator = new FolderGenerator(imageSizeFolder);
        Assertions.assertEquals(IMAGE_SIZE, imageSizeFolderGenerator.getMethod());
    }

    @Test
    public void shouldSetMethod() {
        FolderDTO folder = new FolderDTO();
        FolderGenerator folderGenerator = new FolderGenerator(folder);
        folderGenerator.setMethod(DERIVATIVE);
        Assertions.assertNotNull(folder.getDerivative(), "Derivative should be set");
        Assertions.assertEquals(1.00d, folder.getDerivative());
        Assertions.assertNull(folder.getDpi(), "DPI should be null");
        Assertions.assertNull(folder.getImageSize(), "Image size should be null");
        folderGenerator.setMethod(DPI);
        Assertions.assertNull(folder.getDerivative(), "Derivative should be null");
        Assertions.assertNotNull(folder.getDpi(), "DPI should be set");
        Assertions.assertNull(folder.getImageSize(), "Image size should be null");
        folderGenerator.setMethod(IMAGE_SIZE);
        Assertions.assertNull(folder.getDerivative(), "Derivative should be null");
        Assertions.assertNull(folder.getDpi(), "DPI should be null");
        Assertions.assertNotNull(folder.getImageSize(), "Image size should be set");
    }

}
