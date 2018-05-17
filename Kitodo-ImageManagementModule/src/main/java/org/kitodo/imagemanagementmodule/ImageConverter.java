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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.SystemUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.kitodo.api.imagemanagement.ImageFileFormat;

/**
 * An image conversion task. One conversion task can create multiple result
 * images with different properties. This is done in one single ImageMagick
 * call, reading and decoding the source image into memory only once.
 */
class ImageConverter {

    /**
     * ImageMagick file type prefix to request no file being written.
     */
    private static final String FORMAT_OFF = "NULL:";

    /**
     * ImageMagick option {@code -limit}.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#limit"
     */
    private static final String OPTION_LIMIT = "-limit";

    /**
     * ImageMagick {@code map} limit.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#limit"
     */
    private static final String OPTION_LIMIT_TYPE_MAP = "map";

    /**
     * ImageMagick {@code memory} limit.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#limit"
     */
    private static final String OPTION_LIMIT_TYPE_MEMORY = "memory";

    /**
     * ImageMagick option {@code -units}. Note that {@code -units} must be set
     * <i>before</i> the operation whose value shall be interpreted in this
     * unit.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#units"
     */
    private static final String OPTION_UNITS = "-units";

    /**
     * ImageMagick option {@code -depth}’s value {@code PixelsPerInch}.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#units"
     */
    private static final String OPTION_UNITS_TYPE_PIXELSPERINCH = "PixelsPerInch";

    /**
     * Path to read the source image from.
     */
    private final String source;

    /**
     * Conversion results to create.
     */
    private final Collection<FutureDerivative> results = new LinkedList<>();

    private String memoryLimit;

    /**
     * Creates a new image conversion task.
     *
     * @param imageFileUri
     *            source image to convert
     */
    ImageConverter(URI imageFileUri) {
        source = uriToPath(imageFileUri);
    }

    /**
     * Defines another result of the conversion process.
     *
     * @param path
     *            output file URI
     * @return the conversion result object to define conversion properties
     */
    FutureDerivative addResult(URI path) {
        FutureDerivative result = new FutureDerivative(uriToPath(path));
        results.add(result);
        return result;
    }

    /**
     * Defines another result of the conversion process.
     *
     * @param path
     *            output file URI
     * @param resultFileFormat
     *            image format to generate
     * @return the conversion result object to define conversion properties
     */
    FutureDerivative addResult(URI path, ImageFileFormat resultFileFormat) {
        FutureDerivative result = new FutureDerivative(uriToPath(path), resultFileFormat);
        results.add(result);
        return result;
    }

    static String pathToTheWindowsInstallation() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            throw new IllegalStateException(
                    "pathToTheWindowsInstallation() can only be called on Windows operating systems");
        }
        File programFiles = new File(System.getenv("ProgramFiles"));
        File[] candidates = programFiles
                .listFiles(file -> file.isDirectory() && file.getName().toUpperCase().startsWith("IMAGEMAGICK"));
        if (candidates == null || candidates.length == 0) {
            throw new NoSuchElementException("ImageMagick was not found in " + programFiles);
        }
        return candidates[candidates.length - 1].getAbsolutePath();
    }

    /**
     * Performs the conversion by calling ImageMagick.
     */
    void run() throws IOException {
        IMOperation commandLine = new IMOperation();
        commandLine.addRawArgs(Arrays.asList(OPTION_LIMIT, OPTION_LIMIT_TYPE_MEMORY, memoryLimit));
        commandLine.addRawArgs(Arrays.asList(OPTION_LIMIT, OPTION_LIMIT_TYPE_MAP, memoryLimit));
        commandLine.addRawArgs(Arrays.asList(OPTION_UNITS, OPTION_UNITS_TYPE_PIXELSPERINCH));
        commandLine.addImage(source);
        results.forEach(result -> result.addToCommandLine(commandLine));
        commandLine.addImage(FORMAT_OFF);
        ConvertCmd convertCmd = new ConvertCmd();
        if (SystemUtils.IS_OS_WINDOWS) {
            convertCmd.setSearchPath(ImageConverter.pathToTheWindowsInstallation());
        }
        try {
            convertCmd.run(commandLine);
        } catch (InterruptedException | IM4JavaException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Converts an URI to an absolute local path as String.
     *
     * @param uri
     *            input file URI
     * @return absolute local path
     */
    private static String uriToPath(URI uri) {
        return new File(uri).getAbsolutePath();
    }

    void useAMaximumOfRAM(int ofMB) {
        if (ofMB <= 0) {
            throw new IllegalArgumentException("ofMB must be > 0, but was " + Integer.toString(ofMB));
        }
        memoryLimit = Integer.toString(ofMB) + "MB";
    }
}
