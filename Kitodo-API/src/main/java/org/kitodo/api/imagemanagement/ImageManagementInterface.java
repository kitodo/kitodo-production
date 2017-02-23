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
import java.nio.file.Path;

public interface ImageManagementInterface {

    /**
     * Scales an image at a given path and returns it.
     *
     * @param imageFilePath The path to the image which should be scaled.
     * @param percent The percentage for scaling.
     * @return The scaled image.
     */
    public Image getScaledWebImage(Path imageFilePath, double percent);

    /**
     * Creates a derivative for an image at a given path.
     *
     * @param imagefilePath The path to the image.
     * @param percent The percentage of scaling for the derivative.
     * @param resultFilePath The path to save the derivative to.
     * @param resultFileFormat The formate for the derivative.
     * @return true, if creation was successfull, false otherwise.
     */
    public boolean createDerivative(Path imagefilePath, double percent, Path resultFilePath, ImageFileFormat resultFileFormat);

}
