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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.im4java.core.IMOperation;
import org.kitodo.api.imagemanagement.ImageFileFormat;

/**
 * One image derivative to be created. Multiple result images with different
 * properties can be created from one source image. This is done in one single
 * ImageMagick call, reading and decoding the source image only once.
 */
class FutureDerivative {

    /**
     * ImageMagick file type prefix to request the bitmap file format.
     */
    private static final String FORMAT_BITMAP_PREFIX = "bmp:";

    /**
     * ImageMagick file type prefix to request the gif file format.
     */
    private static final String FORMAT_GIF_PREFIX = "gif:";

    /**
     * ImageMagick file type prefix to request the jpeg file format.
     */
    private static final String FORMAT_JPEG_PREFIX = "jpeg:";

    /**
     * ImageMagick file type prefix to request the jpeg-2000 file format.
     */
    private static final String FORMAT_JPEG2000_PREFIX = "jp2:";

    /**
     * ImageMagick file type prefix to request the PNG file format.
     */
    private static final String FORMAT_PNG_PREFIX = "png:";

    /**
     * ImageMagick file type prefix to request the tiff file format.
     */
    private static final String FORMAT_TIFF_PREFIX = "tiff:";

    /**
     * ImageMagick file type prefix to request the pdf file format.
     */
    private static final String FORMAT_PDF_PREFIX = "pdf:";

    /**
     * ImageMagick operation {@code -resample}. Resizes an image to a given
     * resolution.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#resample"
     */
    private static final String OPTION_RESAMPLE = "-resample";

    /**
     * ImageMagick option {@code -resize}. Resizes an image in various ways.
     *
     * @see "https://www.imagemagick.org/script/command-line-options.php?#resize"
     */
    private static final String OPTION_RESIZE = "-resize";

    /**
     * List of image operations to apply when creating the derivative.
     */
    private final List<Pair<String, String>> operations = new LinkedList<>();

    /**
     * Path to write the image file to.
     */
    private String outputFile;

    /**
     * Creates a new conversion result.
     *
     * @param outputFile
     *            path to write the result to
     */
    FutureDerivative(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Creates a new conversion result.
     *
     * @param outputFile
     *            path to write the result to
     * @param outputFormat
     *            image file format to create
     */
    FutureDerivative(String outputFile, ImageFileFormat outputFormat) {
        switch (outputFormat) {
            case BMP:
                this.outputFile = FORMAT_BITMAP_PREFIX.concat(outputFile);
                break;
            case GIF:
                this.outputFile = FORMAT_GIF_PREFIX.concat(outputFile);
                break;
            case JPEG:
                this.outputFile = FORMAT_JPEG_PREFIX.concat(outputFile);
                break;
            case JPEG2000:
                this.outputFile = FORMAT_JPEG2000_PREFIX.concat(outputFile);
                break;
            case PNG:
                this.outputFile = FORMAT_PNG_PREFIX.concat(outputFile);
                break;
            case TIFF:
                this.outputFile = FORMAT_TIFF_PREFIX.concat(outputFile);
                break;
            case PDF:
                this.outputFile = FORMAT_PDF_PREFIX.concat(outputFile);
                break;
            default:
                this.outputFile = outputFile;
                break;
        }
    }

    /**
     * Add the parameters to create this result to the ImageMagick command line.
     *
     * @param commandLine
     *            ImageMagick command line object to add the parameters to
     */
    void addToCommandLine(IMOperation commandLine) {
        commandLine.openOperation();
        commandLine.p_clone();
        operations.forEach(operation -> commandLine.addRawArgs(operation.getKey(), operation.getValue()));
        commandLine.write();
        commandLine.addImage(URLDecoder.decode("'" + outputFile + "'", StandardCharsets.UTF_8));
        commandLine.p_delete();
        commandLine.closeOperation();
    }

    /**
     * Defines a resize operation to create this result.
     *
     * @param percent
     *            percentage to scale the image
     * @return this, for method chaining
     */
    FutureDerivative resize(double percent) {
        if (Double.isNaN(percent)) {
            throw new IllegalArgumentException("percent must be a number, but was " + percent);
        }
        if (percent <= 0.0) {
            throw new IllegalArgumentException("percent must be > 0.0, but was " + percent);
        }
        String percentValue = Double.toString(100 * percent).concat("%");
        operations.add(Pair.of(OPTION_RESIZE, percentValue));
        return this;
    }

    /**
     * Defines an output DPI value to create this result.
     *
     * @param dpi
     *            new image resolution in DPI
     * @return this, for method chaining
     */
    FutureDerivative resizeToDpi(int dpi) {
        if (dpi <= 0) {
            throw new IllegalArgumentException("dpi must be > 0, but was " + dpi);
        }
        operations.add(Pair.of(OPTION_RESAMPLE, Integer.toString(dpi)));
        return this;
    }

    /**
     * Defines a resize operation to create this result.
     *
     * @param pixelWidth
     *            with of created image in pixels
     * @return this, for method chaining
     */
    FutureDerivative resizeToWidth(int pixelWidth) {
        if (pixelWidth <= 0) {
            throw new IllegalArgumentException("pixelWidth must be > 0, but was " + pixelWidth);
        }
        operations.add(Pair.of(OPTION_RESIZE, Integer.toString(pixelWidth)));
        return this;
    }

}
