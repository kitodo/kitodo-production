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

package org.kitodo.api.imagemanagement;

import java.awt.Image;
import java.io.IOException;
import java.net.URI;

public interface ImageManagementInterface {

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
    Image changeDpi(URI imageFileUri, int dpi) throws IOException;

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
            throws IOException;

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
    Image getSizedWebImage(URI imageFileUri, int pixelWidth) throws IOException;
}
