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
import java.net.URI;

public interface ImageManagementInterface {

    /**
     * Changes the dpi of an image at a given uri.
     *
     * @param imagefileUri
     *            The uri of the image.
     * @param dpi
     *            the new dpi.
     * @return the image with the new dpi.
     * @throws Exception
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    Image changeDpi(URI imagefileUri, int dpi) throws Exception;

    /**
     * Creates a derivative for an image at a given path.
     *
     * @param imageFileUri
     *            The uri to the image.
     * @param percent
     *            The percentage of scaling for the derivative.
     * @param resultFileUri
     *            The uri to save the derivative to.
     * @param resultFileFormat
     *            The formate for the derivative.
     * @return true, if creation was successful, false otherwise.
     * @throws Exception
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    boolean createDerivative(URI imageFileUri, double percent, URI resultFileUri, ImageFileFormat resultFileFormat)
            throws Exception;

    /**
     * Scales an image at a given path and returns it.
     *
     * @param imageFileUri
     *            The uri to the image which should be scaled.
     * @param percent
     *            The percentage for scaling.
     * @return The scaled image.
     * @throws Exception
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    Image getScaledWebImage(URI imageFileUri, double percent) throws Exception;

    /**
     * changes the size (in pixel) of the image.
     *
     * @param imageFileUri
     *            The uri of the image to size.
     * @param pixelWidth
     *            The new pixelWidth.
     * @return The new sized image
     * @throws Exception
     *             if the plug-in is configured incorrectly, the image is
     *             missing or corrupted, etc.
     */
    Image getSizedWebImage(URI imageFileUri, int pixelWidth) throws Exception;

}
