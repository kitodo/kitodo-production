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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public interface FileManagementInterface {

    /**
     * Opens an OutputStream to a given uri.
     * 
     * @param uri
     *            the uri to write to
     * @return an writable OutputStream
     */
    OutputStream write(URI uri) throws IOException;

    /**
     * Opens an InputStream to a given uri.
     * 
     * @param uri
     *            the uri to write from
     * @return a readable InputStream
     */
    InputStream read(URI uri) throws IOException;

    /**
     * Delets content at a given uri.
     * 
     * @param uri
     *            the uri to delete
     * @return true if successfull, false otherwise
     */
    boolean delete(URI uri) throws IOException;

    /**
     * Creates the FolderStructure needed for a process in kitodo.
     * 
     * @param processId
     *            the id of the process
     * @return a ProcessLocation
     */
    ProcessLocation createProcessLocation(String processId) throws IOException;

    /**
     * Creates a directory with a given name at a given uri.
     *
     * @param parentFolderUri
     *            the location to create the folder
     * @param directoryName
     *            the name of the directory
     * @return the URI to the userHomeLocation
     */
    URI createDirectory(URI parentFolderUri, String directoryName) throws IOException;

    /**
     * Creates a resource with a given fileName.
     * 
     * @param parentFolderUri
     *            the Location to create the resource
     * @param fileName
     *            the fileName of the new resource
     * @return the URI of the new resource
     */
    URI createResource(URI parentFolderUri, String fileName);

}
