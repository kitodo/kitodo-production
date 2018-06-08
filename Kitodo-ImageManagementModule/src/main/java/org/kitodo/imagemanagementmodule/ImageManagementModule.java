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

import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.imagemanagement.ImageManagementInterface;

/**
 * An ImageManagementInterface implementation using ImageMagick.
 */
public class ImageManagementModule implements ImageManagementInterface {
    private static final Logger logger = LogManager.getLogger(ImageManagementModule.class);

    /**
     * Image format used internally to create image derivatives, optimized for
     * loss-free image quality, and speed. The format must be supported by both
     * ImageMagick and {@link javax.imageio.ImageIO}.
     */
    private static final String RAW_IMAGE_FORMAT = ".bmp";

    /**
     * Temporary directory location.
     */
    private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Image format used internally to create web images, optimized for small
     * size. The format must be supported by both ImageMagick and
     * {@link javax.imageio.ImageIO}.
     */
    private static final String WEB_IMAGE_FORMAT = ".jpeg";

    /**
     * Memory size limit for ImageMagick. Larger images will be processed from a
     * memory-mapped file, or directly from disk.
     */
    private int memorySizeLimitMB = 40;

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#getScaledWebImage(java.net.URI,
     *      double)
     */
    @Override
    public Image getScaledWebImage(URI sourceUri, double scale) throws IOException {

        if (!new File(sourceUri).exists()) {
            throw new FileNotFoundException("sourceUri must exist: " + sourceUri.getRawPath());
        }
        if (Double.isNaN(scale)) {
            throw new IllegalArgumentException("scale must be a number, but was " + Double.toString(scale));
        }
        if (scale <= 0.0) {
            throw new IllegalArgumentException("scale must be > 0.0, but was " + Double.toString(scale));
        }

        File tempFile = File.createTempFile("scaledWebImage-", WEB_IMAGE_FORMAT, TMPDIR);
        try {
            tempFile.deleteOnExit();
            ImageConverter imageConverter = new ImageConverter(sourceUri);
            imageConverter.addResult(tempFile.toURI()).resize(scale);

            imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
            logger.info("Generating scaled web image from {} as {}, scale {}%", sourceUri, tempFile, 100 * scale);
            imageConverter.run();

            logger.trace("Loading {}", tempFile);
            Image buffer = ImageIO.read(tempFile);
            logger.trace("{} successfully loaded", tempFile);
            return buffer;
        } finally {
            logger.debug("Deleting {}", tempFile);
            tempFile.delete();
            logger.trace("Successfully deleted {}", tempFile);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#createDerivative(java.net.URI,
     *      double, java.net.URI,
     *      org.kitodo.api.imagemanagement.ImageFileFormat)
     */
    @Override
    public boolean createDerivative(URI sourceUri, double scale, URI resultUri, ImageFileFormat format)
            throws IOException {

        if (!new File(sourceUri).exists()) {
            throw new FileNotFoundException("sourceUri must exist: " + sourceUri.getRawPath());
        }
        if (Double.isNaN(scale)) {
            throw new IllegalArgumentException("scale must be a number, but was " + Double.toString(scale));
        }
        if (scale <= 0.0) {
            throw new IllegalArgumentException("scale must be > 0.0, but was " + Double.toString(scale));
        }
        if (resultUri == null) {
            throw new NullPointerException("resultUri must not be null");
        }

        ImageConverter imageConverter = new ImageConverter(sourceUri);
        imageConverter.addResult(resultUri, format).resize(scale);
        imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
        logger.info("Creating derivative from {} as {}, type {}, scale {}%", sourceUri, resultUri, format, 100 * scale);
        imageConverter.run();
        return new File(resultUri).exists();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#changeDpi(java.net.URI,
     *      int)
     */
    @Override
    public Image changeDpi(URI sourceUri, int dpi) throws IOException {
        if (!new File(sourceUri).exists()) {
            throw new FileNotFoundException("sourceUri must exist: " + sourceUri.getRawPath());
        }
        if (dpi <= 0) {
            throw new IllegalArgumentException("dpi must be > 0, but was " + Integer.toString(dpi));
        }

        File tempFile = File.createTempFile("dpiChangedImage-", RAW_IMAGE_FORMAT, TMPDIR);
        try {
            tempFile.deleteOnExit();
            URI imageUri = tempFile.toURI();
            ImageConverter imageConverter = new ImageConverter(sourceUri);
            imageConverter.addResult(imageUri).resizeToDpi(dpi);

            imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
            logger.info("Resizing {} as {} to {} DPI", sourceUri, tempFile, dpi);
            imageConverter.run();

            logger.trace("Loading {}", tempFile);
            Image buffer = ImageIO.read(tempFile);
            logger.trace("{} successfully loaded", tempFile);
            return buffer;
        } finally {
            logger.debug("Deleting {}", tempFile);
            tempFile.delete();
            logger.trace("Successfully deleted {}", tempFile);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#getSizedWebImage(java.net.URI,
     *      int)
     */
    @Override
    public Image getSizedWebImage(URI sourceUri, int width) throws IOException {

        if (!new File(sourceUri).exists()) {
            throw new FileNotFoundException("sourceUri must exist: " + sourceUri.getRawPath());
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0, but was " + Integer.toString(width));
        }

        File tempFile = File.createTempFile("sizedWebImage-", WEB_IMAGE_FORMAT, TMPDIR);
        try {
            tempFile.deleteOnExit();
            URI webImageUri = tempFile.toURI();
            ImageConverter imageConverter = new ImageConverter(sourceUri);
            imageConverter.addResult(webImageUri).resizeToWidth(width);

            imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
            logger.info("Generating sized web image from {} as {}, width {}px", sourceUri, tempFile, width);
            imageConverter.run();

            logger.trace("Loading {}", tempFile);
            Image buffer = ImageIO.read(tempFile);
            logger.trace("{} successfully loaded", tempFile);
            return buffer;
        } finally {
            logger.debug("Deleting {}", tempFile);
            tempFile.delete();
            logger.trace("Successfully deleted {}", tempFile);
        }
    }
}
