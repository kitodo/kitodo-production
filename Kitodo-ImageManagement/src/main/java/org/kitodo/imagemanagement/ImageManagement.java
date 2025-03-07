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

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.imagemanagement.ImageManagementInterface;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterImageManagement;
import org.kitodo.utils.Guard;

/**
 * An ImageManagementInterface implementation using ImageMagick.
 */
public class ImageManagement implements ImageManagementInterface {
    private static final Logger logger = LogManager.getLogger(ImageManagement.class);

    /**
     * Image format used internally to create image derivatives, optimized for
     * loss-free image quality, and speed. The format must be supported by both
     * ImageMagick and {@link javax.imageio.ImageIO}.
     */
    private static final String RAW_IMAGE_FORMAT = ".bmp";

    /**
     * Temporary directory location.
     */
    private static final File TMPDIR = new File(
            KitodoConfig.getParameter(ParameterImageManagement.DIR_TMP, System.getProperty("java.io.tmpdir")));

    /**
     * Image format used internally to create web images, optimized for small
     * size. The format must be supported by both ImageMagick and
     * {@link javax.imageio.ImageIO}.
     */
    private static final String WEB_IMAGE_FORMAT = ".jpeg";

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#changeDpi(java.net.URI,
     *      int)
     */
    @Override
    public Image changeDpi(URI sourceUri, int dpi) throws IOException {
        fileExists(sourceUri);
        Guard.isPositive("dpi", dpi);
        return summarize("dpiChangedImage-", RAW_IMAGE_FORMAT, sourceUri, lambda -> lambda.resizeToDpi(dpi),
            "Resizing {} as {} to {} DPI", dpi);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#createDerivative(java.net.URI,
     *      double, java.net.URI,
     *      org.kitodo.api.imagemanagement.ImageFileFormat)
     */
    @Override
    public boolean createDerivative(URI sourceUri, double factor, URI resultUri, ImageFileFormat format)
            throws IOException {

        fileExists(sourceUri);
        Guard.isPositive("factor", factor);
        if (resultUri == null) {
            throw new NullPointerException("resultUri must not be null");
        }

        ImageConverter imageConverter = new ImageConverter(sourceUri);
        imageConverter.addResult(resultUri, format).resize(factor);
        logger.info("Creating derivative from {} as {}, format {}, factor {}%", sourceUri, resultUri, format,
            100 * factor);
        imageConverter.run();
        return new File(resultUri).exists();
    }

    /**
     * {@inheritDoc}
     *

     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#getSizedWebImage(java.net.URI,
     *      int)
     */
    @Override
    public Image getSizedWebImage(URI sourceUri, int width) throws IOException {

        fileExists(sourceUri);
        Guard.isPositive("width", width);

        return summarize("sizedWebImage-", WEB_IMAGE_FORMAT, sourceUri, lambda -> lambda.resizeToWidth(width),
            "Generating sized web image from {} as {}, width {} px", width);
    }

    /**
     * Summarizes three similar codes.
     *
     * @param prefix
     *            The prefix string to be used in generating the file's name;
     *            must be at least three characters long
     * @param suffix
     *            The suffix string to be used in generating the file's name;
     *            may be {@code null}, in which case the suffix {@code ".tmp"}
     *            will be used
     * @param sourceUri
     *            source image to convert
     * @param lambda
     *            lambda expression to apply to the result of the conversion
     *            process
     * @param message
     *            the message to log; the format depends on the message factory.
     * @param pTwo
     *            parameter to the message.
     * @return the generated image
     * @throws IOException
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    private static Image summarize(String prefix, String suffix, URI sourceUri, Function<FutureDerivative, ?> lambda,
            String message, Object pTwo) throws IOException {

        File tempFile = File.createTempFile(prefix, suffix, TMPDIR);
        try {
            tempFile.deleteOnExit();
            ImageConverter imageConverter = new ImageConverter(sourceUri);
            lambda.apply(imageConverter.addResult(tempFile.toURI()));

            logger.info(message, sourceUri, tempFile, pTwo);
            imageConverter.run();

            logger.trace("Loading {}", tempFile);
            Image buffer = ImageIO.read(tempFile);
            logger.trace("{} successfully loaded", tempFile);
            return buffer;
        } finally {
            logger.debug("Deleting {}", tempFile);
            try {
                Files.delete(tempFile.toPath());
                logger.trace("Successfully deleted {}", tempFile);
            } catch (IOException e) {
                logger.warn("Couldn’t delete {}", tempFile, e);
            }
        }
    }

    private void fileExists(URI sourceUri) throws FileNotFoundException {
        if (!new File(sourceUri).exists()) {
            throw new FileNotFoundException("sourceUri must exist: " + sourceUri.getRawPath());
        }
    }
}
