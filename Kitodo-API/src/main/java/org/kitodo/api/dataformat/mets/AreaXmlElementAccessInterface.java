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

package org.kitodo.api.dataformat.mets;

/**
 * Interface for a service that handles access to the {@code <mets:area>}
 * element.
 *
 * <p>
 * An area is a view on a media unit (METS idiom: file). For a still image, this
 * is often a rectangle which is left over when cutting off the top, bottom,
 * right, and left sides of the image. For other media formats, however, this
 * can also be other properties, such as the time for the start and end of the
 * playback for a sound or video sequence.
 *
 * <p>
 * This part is provided here only insofar as there is one area per page that
 * currently has no attributes apart from a reference to the file. This is
 * intended to be extended here later for the support of Area. In the future,
 * there may be several areas per page, and the areas will have other attributes
 * that describe their positioning.
 */
public interface AreaXmlElementAccessInterface {
    /**
     * Returns a service to access the media unit ({@code <mets:file>} element)
     * on which the view is based.
     *
     * @return a service to access the file
     */
    FileXmlElementAccessInterface getFile();

    /**
     * Sets the service to access the media unit ({@code <mets:file>} element)
     * on which the view is based.
     *
     * @param file
     *            service to access the file to set
     */
    void setFile(FileXmlElementAccessInterface file);
}
