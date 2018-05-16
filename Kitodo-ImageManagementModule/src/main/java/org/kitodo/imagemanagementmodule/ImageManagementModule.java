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
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.imagemanagement.ImageManagementInterface;

/**
 * An ImageManagementInterface implementation using ImageMagick.
 */
public class ImageManagementModule implements ImageManagementInterface {

    /**
     * Image format used internally to create image derivatives, optimized for
     * loss-free image quality, and speed. The format must be supported by both
     * ImageMagick and {@link javax.imageio.ImageIO}.
     */
    private static final String RAW_IMAGE_FORMAT = ".bmp";

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
    public Image getScaledWebImage(URI imageFileUri, double percent) {
        assert new File(imageFileUri).exists();
        assert !Double.isNaN(percent);
        assert percent > 0.0;

        try {
            File temporaryWebImage = File.createTempFile("scaledWebImage-", WEB_IMAGE_FORMAT);
            temporaryWebImage.deleteOnExit();
            URI webImageUri = temporaryWebImage.toURI();
            ImageConverter imageConverter = new ImageConverter(imageFileUri);
            imageConverter.addResult(webImageUri).resize(percent);

            imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
            imageConverter.run();

            Image result = ImageIO.read(temporaryWebImage);
            temporaryWebImage.delete();
            return result;
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e);
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
    public boolean createDerivative(URI imageFileUri, double percent, URI resultFileUri,
            ImageFileFormat resultFileFormat) {

        ImageConverter imageConverter = new ImageConverter(imageFileUri);
        imageConverter.addResult(resultFileUri, resultFileFormat).resize(percent);
        imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
        imageConverter.run();
        return new File(resultFileUri).exists();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#changeDpi(java.net.URI,
     *      int)
     */
    @Override
    public Image changeDpi(URI imagefileUri, int dpi) {
        assert new File(imagefileUri).exists();

        try {
            File temporaryImage = File.createTempFile("changedDpiImage-", RAW_IMAGE_FORMAT);
            temporaryImage.deleteOnExit();
            URI imageUri = temporaryImage.toURI();
            ImageConverter imageConverter = new ImageConverter(imagefileUri);
            imageConverter.addResult(imageUri).setDpi(dpi);

            imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
            imageConverter.run();

            Image result = ImageIO.read(temporaryImage);
            temporaryImage.delete();
            return result;
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.kitodo.api.imagemanagement.ImageManagementInterface#getSizedWebImage(java.net.URI,
     *      int)
     */
    @Override
    public Image getSizedWebImage(URI imageFileUri, int pixelWidth) {
        assert new File(imageFileUri).exists();
        assert pixelWidth > 0;

        try {
            File temporaryWebImage = File.createTempFile("sizedWebImage", WEB_IMAGE_FORMAT);
            temporaryWebImage.deleteOnExit();
            URI webImageUri = temporaryWebImage.toURI();
            ImageConverter imageConverter = new ImageConverter(imageFileUri);
            imageConverter.addResult(webImageUri).resize(pixelWidth);

            imageConverter.useAMaximumOfRAM(memorySizeLimitMB);
            imageConverter.run();

            Image result = ImageIO.read(temporaryWebImage);
            temporaryWebImage.delete();
            return result;
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
