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

package org.kitodo.tasks;

/**
 * Enumerates the steps to go to generate images.
 */
public enum ImageGeneratorStep {
    /**
     * Second step, (if demanded) check if the image exists in the destination
     * folder, and optionally validate the image file content for not being
     * corrupted.
     */
    DETERMINE_WHICH_IMAGES_NEED_TO_BE_GENERATED,

    /**
     * Third step, generate whatever needs to be generated.
     */
    GENERATE_IMAGES,

    /**
     * First step, get the list of images in the folder of source images.
     */
    LIST_SOURCE_FOLDER;
}
