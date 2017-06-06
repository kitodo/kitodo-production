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

import java.awt.*;
import java.net.URI;

public interface ImageManagementInterface {

    /**
     * Scales an image at a given path and returns it.
     *
     * @param imageFileUri
     *            The uri to the image which should be scaled.
     * @param percent
     *            The percentage for scaling.
     * @return The scaled image.
     */
    Image getScaledWebImage(URI imageFileUri, double percent);

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
     */
    boolean createDerivative(URI imageFileUri, double percent, URI resultFileUri, ImageFileFormat resultFileFormat);

    /**
     * Changes the dpi of an image at a given uri.
     * 
     * @param imagefileUri
     *            The uri of the image.
     * @param dpi
     *            the new dpi.
     * @return the image with the new dpi.
     */
    Image changeDpi(URI imagefileUri, int dpi);

    /**
     * changes the size (in pixel) of the image.
     * 
     * @param imageFileUri
     *            The uri of the image to size.
     * @param pixelWidth
     *            The new pixelWidth.
     * @return The new sized image
     */
    Image getSizedWebImage(URI imageFileUri, int pixelWidth);

}
