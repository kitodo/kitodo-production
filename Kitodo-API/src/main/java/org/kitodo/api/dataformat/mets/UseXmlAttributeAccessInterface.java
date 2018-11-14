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
 * Interface for a service that handles access to the
 * {@code <mets:fileGrp USE="...">} attribute. A use is a variant of a media
 * unit in a particular file format held in a dedicated binary file that can be
 * referenced by its URI. In Production, each use is stored in its own use
 * folder below the process folder. The use has a specific MIME type resulting
 * from use. For example, this is an image format for image display, but it can
 * be XML for an OCR result or PDF for printing.
 */
public interface UseXmlAttributeAccessInterface {
    /**
     * Returns the Internet MIME type of use.
     *
     * @return the MIME type
     */
    String getMimeType();

    /**
     * Returns the identifier of the use. According to METS, this is a simple
     * text, but the ZVDD DFG-Viewer METS profile makes specific specifications
     * as to what this identifier should look like (see page 12 of the reference
     * manual).
     *
     * @return the use
     * @see "https://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf#page=12"
     */
    String getUse();

    /**
     * Sets the Internet MIME type of use.
     *
     * @param mimeType
     *            MIME type to be set
     */
    void setMimeType(String mimeType);

    /**
     * Sets the identifier of the use. According to METS, this is a simple text,
     * but the ZVDD DFG Viewer METS profile makes specific specifications as to
     * what this identifier should look like (see page 12 of the reference
     * manual). The identifier of the use can only be set as long as the use
     * service is not yet used as a key for referencing a F locat in a file.
     * Attempting to change the identifier of the use if the use service is
     * already used as the key to referencing a F locat in a file throws an
     * unsupported operation exception.
     *
     * @param use
     *            use to be set
     * @throws UnsupportedOperationException
     *             if the use service is already used as a key to referencing a
     *             F locat in a file
     * @see "https://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf#page=12"
     */
    void setUse(String use);
}
