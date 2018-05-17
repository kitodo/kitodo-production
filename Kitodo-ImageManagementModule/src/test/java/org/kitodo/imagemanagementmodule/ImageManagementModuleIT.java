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

package org.kitodo.imagemanagementmodule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.SystemUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.core.InfoException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.imagemanagement.ImageManagementInterface;

/**
 * Tests the Almighty Content Module.
 */
public class ImageManagementModuleIT {
    /**
     * Folder for test resources (images).
     */
    private static final String RESOURCES = "src/test/resources";

    /**
     * Input tiff file.
     */
    private static final String _00000001_TIF = RESOURCES + "/00000001.tif";

    /**
     * Derivative to generate.
     */
    private static final String _00000001_MAX_JPG = RESOURCES + "/00000001_max.jpg";

    /**
     * Creates the input tiff file. The tiff file will contain the “wizard”
     * image that is compiled into ImageMagick. The image has a size of 480 ×
     * 640 pixels and a density of 72 DPI.
     */
    @BeforeClass
    public static void createTestTiff() throws IOException, InterruptedException, IM4JavaException {

        // create the resources directory if it is not there
        File resourcesDirectory = new File(RESOURCES);
        if (!resourcesDirectory.exists()) {
            resourcesDirectory.mkdirs();
        }

        // create the test tiff
        IMOperation operation = new IMOperation();
        operation.addRawArgs(Arrays.asList("wizard:", "-compress", "None", _00000001_TIF));
        ConvertCmd convertCmd = new ConvertCmd();
        if (SystemUtils.IS_OS_WINDOWS) {
            convertCmd.setSearchPath(ImageConverter.pathToTheWindowsInstallation());
        }
        convertCmd.run(operation);
    }

    @Test
    public void testGetScaledWebImage() throws IOException, InfoException {
        assert new File(_00000001_TIF).exists();
        ImageManagementInterface module = new ImageManagementModule();
        Image scaledWebImage = module.getScaledWebImage(new File(_00000001_TIF).toURI(), 0.3);
        assertThat(scaledWebImage.getWidth(null),
            is((int) Math.round(0.3 * new Info(_00000001_TIF, true).getImageWidth())));
    }

    @Test
    public void testCreateDerivative() throws IOException, InfoException {
        assert new File(_00000001_TIF).exists();
        File maxDerivative = new File(_00000001_MAX_JPG);
        ImageManagementInterface module = new ImageManagementModule();
        assertThat(
            module.createDerivative(new File(_00000001_TIF).toURI(), 1.0, maxDerivative.toURI(), ImageFileFormat.JPEG),
            is(true));
        assertThat(maxDerivative.exists(), is(true));
        assertThat(new Info(_00000001_MAX_JPG, true).getImageWidth(),
            is(equalTo(new Info(_00000001_TIF, true).getImageWidth())));
    }

    @Test
    public void testChangeDpi() throws IOException, InfoException {
        assert new File(_00000001_TIF).exists();
        ImageManagementInterface module = new ImageManagementModule();
        Image image = module.changeDpi(new File(_00000001_TIF).toURI(), 300);
        assertThat(72 * image.getWidth(null) / new Info(_00000001_TIF, true).getImageWidth(), is(equalTo(300)));

    }

    @Test
    public void testGetSizedWebImage() throws IOException {
        assert new File(_00000001_TIF).exists();
        ImageManagementInterface module = new ImageManagementModule();
        Image scaledWebImage = module.getSizedWebImage(new File(_00000001_TIF).toURI(), 150);
        assertThat(scaledWebImage.getWidth(null), is(equalTo(150)));
    }

    /**
     * Clean up after tests.
     */
    @AfterClass
    public static void cleanUp() {
        File tiffFile = new File(_00000001_TIF);
        if (tiffFile.exists()) {
            tiffFile.delete();
        }
        File jpgMaxFile = new File(_00000001_MAX_JPG);
        if (jpgMaxFile.exists()) {
            jpgMaxFile.delete();
        }
    }
}
