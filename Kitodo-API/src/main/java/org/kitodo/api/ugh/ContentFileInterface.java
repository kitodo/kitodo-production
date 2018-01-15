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

package org.kitodo.api.ugh;

/**
 * A ContentFile represents a file which must be accessible via the file system
 * and contains the content of the {@code DigitalDocument}. A ContentFile
 * belongs always to a {@code FileSet}, which provides methods to add and remove
 * content files ({@code addFile} and {@code removeFile}. ContentFile objects
 * are not only be part of a FileSet but must also be linked to structure
 * elements. Therefore references to {@code DocStruct} objects exists.
 */
public interface ContentFileInterface {
    /**
     * Returns the location of the content file. The filename is always an
     * absolute file.
     *
     * @return the location of the content file
     */
    String getLocation();

    /**
     * Sets the location of the content file. The file must at least be readable
     * from this position.
     *
     * @param fileName
     *            the location of the content file
     * @return always {@code true}. Return value is never used.
     */
    boolean setLocation(String fileName);
}
