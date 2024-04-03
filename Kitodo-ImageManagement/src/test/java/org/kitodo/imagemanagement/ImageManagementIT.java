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

package org.kitodo.imagemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
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
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterImageManagement;

/**
 * Tests the Image Management.
 *
 * <p>
 * To successfully run this test on Windows, create the file
 * <code>C:\Users\</code><i>your user
 * name</i><code>\kitodo_config.properties</code> with path to the your
 * ImageMagick program folder in it, like:
 *
 * <p>
 * {@code ImageManagement.searchPath=C:\\Program Files\\ImageMagick-7.0.7-Q16}
 */
public class ImageManagementIT {
    /**
     * Folder for test resources (images).
     */
    private static final String RESOURCES = "src/test/resources";

    /**
     * Input tiff file.
     */
    private static final String _00000001_TIF = RESOURCES + "/00000001.tif";
    private static final String _00000001_TIF_WITH_WHITESPACE = RESOURCES + "/00000001 whiteSpace.tif";
    private static final String _00000001_TIF_WITH_SPECIAL_CHARACTER = RESOURCES + "/00000001_ÄÜÖ#.tif";

    /**
     * Derivative to generate.
     */
    private static final String _00000001_MAX_JPG = RESOURCES + "/00000001_max.jpg";
    private static final String _00000001_MAX_JPG_WITH_WHITESPACE = RESOURCES + "/00000001 whiteSpace_max.jpg";
    private static final String _00000001_MAX_JPG_WITH_SPECIAL_CHARACTER = RESOURCES + "/00000001_ÄÜÖ#_max.jpg";

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
            String parameter = KitodoConfig.getParameter(ParameterImageManagement.SEARCH_PATH);
            convertCmd.setSearchPath(parameter);
        }
        convertCmd.run(operation);
    }

    @Test
    public void testCreateDerivative() throws IOException, InfoException {
        assert new File(_00000001_TIF).exists();
        File maxDerivative = new File(_00000001_MAX_JPG);
        ImageManagementInterface module = new ImageManagement();
        assertTrue(module.createDerivative(new File(_00000001_TIF).toURI(), 1.0, maxDerivative.toURI(), ImageFileFormat.JPEG));
        assertTrue(maxDerivative.exists());
        assertEquals(new Info(_00000001_TIF, true).getImageWidth(), new Info(_00000001_MAX_JPG, true).getImageWidth());

        Path tifPath = Paths.get(_00000001_TIF);
        Files.copy(tifPath, Paths.get(_00000001_TIF_WITH_WHITESPACE), StandardCopyOption.REPLACE_EXISTING);

        assert new File(_00000001_TIF_WITH_WHITESPACE).exists();
        maxDerivative = new File(_00000001_MAX_JPG_WITH_WHITESPACE);
        assertTrue(module.createDerivative(new File(_00000001_TIF_WITH_WHITESPACE).toURI(), 1.0, maxDerivative.toURI(),
                ImageFileFormat.JPEG));
        assertTrue(maxDerivative.exists());

        Files.copy(tifPath, Paths.get(_00000001_TIF_WITH_SPECIAL_CHARACTER), StandardCopyOption.REPLACE_EXISTING);

        assert new File(_00000001_TIF_WITH_SPECIAL_CHARACTER).exists();
        maxDerivative = new File(_00000001_MAX_JPG_WITH_SPECIAL_CHARACTER);
        assertTrue(module.createDerivative(new File(_00000001_TIF_WITH_SPECIAL_CHARACTER).toURI(), 1.0, maxDerivative.toURI(),
                ImageFileFormat.JPEG));
        assertTrue(maxDerivative.exists());
    }

    @Test
    public void testChangeDpi() throws IOException, InfoException {
        assert new File(_00000001_TIF).exists();
        ImageManagementInterface module = new ImageManagement();
        Image image = module.changeDpi(new File(_00000001_TIF).toURI(), 300);
        assertEquals(300, 72 * image.getWidth(null) / new Info(_00000001_TIF, true).getImageWidth());

    }

    @Test
    public void testGetSizedWebImage() throws IOException {
        assert new File(_00000001_TIF).exists();
        ImageManagementInterface module = new ImageManagement();
        Image scaledWebImage = module.getSizedWebImage(new File(_00000001_TIF).toURI(), 150);
        assertEquals(150, scaledWebImage.getWidth(null));
    }

    /**
     * Clean up after tests.
     */
    @AfterClass
    public static void cleanUp() {
        File resources = new File(RESOURCES);
        for (File file : Objects.requireNonNull(resources.listFiles())) {
            if (FilenameUtils.isExtension(file.getName(), "tif", "jpg")) {
                file.delete();
            }
        }
    }
}
