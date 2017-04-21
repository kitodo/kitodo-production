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

package org.kitodo.api.filemanagement;

import java.net.URI;

public class ProcessLocation {

    /**
     * The main URI of the process.
     */
    private URI processFolder;
    /**
     * The image folder of the process.
     */
    private URI imageFolder;
    /**
     * The URI to the metaXml.
     */
    private URI metaXmlUri;

    /**
     * The Constructor.
     * 
     * @param processFolder
     *            the given processFolder.
     * @param imageFolder
     *            the given imageFolder.
     * @param metaXmlUri
     *            the girven metaXmlUri.
     */
    public ProcessLocation(URI processFolder, URI imageFolder, URI metaXmlUri) {
        this.processFolder = processFolder;
        this.imageFolder = imageFolder;
        this.metaXmlUri = metaXmlUri;
    }

    /**
     * Gets the process folder.
     * 
     * @return the URI to the process folder.
     */
    public URI getProcessFolder() {
        return processFolder;
    }

    /**
     * Gets the image folder.
     * 
     * @return the URI to the image folder.
     */
    public URI getImageFolder() {
        return imageFolder;
    }

    /**
     * Gets the metaXmlUri
     * 
     * @return the URI to the metaXml.
     */
    public URI getMetaXmlUri() {
        return metaXmlUri;
    }
}
