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

package org.kitodo.services.image;

import java.awt.Image;
import java.io.IOException;
import java.net.URI;

import org.kitodo.api.imagemanagement.ImageFileFormat;
import org.kitodo.api.imagemanagement.ImageManagementInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

/**
 * This class does nothing more than call the methods on the image management
 * interface.
 */
public class ImageService {

    private ImageManagementInterface imageManagement;

    public ImageService() {
        imageManagement = new KitodoServiceLoader<ImageManagementInterface>(ImageManagementInterface.class)
                .loadModule();
    }

    /**
     * Changes the DPI of an image at a given URI.
     *
     * @param imageFileUri
     *            the URI of the image
     * @param dpi
     *            the new DPI
     * @return the image with the new DPI
     * @throws IOException
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    Image changeDpi(URI imageFileUri, int dpi) throws IOException {
        return imageManagement.changeDpi(imageFileUri, dpi);
    }

    /**
     * Creates a derivative for an image at a given path.
     *
     * @param imageFileUri
     *            the URI to the image
     * @param percent
     *            the percentage of scaling for the derivative
     * @param resultFileUri
     *            the URI to save the derivative to
     * @param resultFileFormat
     *            the format for the derivative
     * @return true, if creation was successful, false otherwise
     * @throws IOException
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    boolean createDerivative(URI imageFileUri, double percent, URI resultFileUri, ImageFileFormat resultFileFormat)
            throws IOException {
        return imageManagement.createDerivative(imageFileUri, percent, resultFileUri, resultFileFormat);
    }

    /**
     * Scales an image at a given path and returns it.
     *
     * @param imageFileUri
     *            the URI to the image which should be scaled
     * @param percent
     *            the percentage for scaling
     * @return the scaled image
     * @throws IOException
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    Image getScaledWebImage(URI imageFileUri, double percent) throws IOException {
        return imageManagement.getScaledWebImage(imageFileUri, percent);
    }

    /**
     * Changes the size (in pixel) of the image.
     *
     * @param imageFileUri
     *            the URI of the image to size
     * @param pixelWidth
     *            the new width in pixels
     * @return the new sized image
     * @throws IOException
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    Image getSizedWebImage(URI imageFileUri, int pixelWidth) throws IOException {
        return imageManagement.getSizedWebImage(imageFileUri, pixelWidth);
    }
}
