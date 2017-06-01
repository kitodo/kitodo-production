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

/**
 * Manages the handling of files.
 */
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
     * @return true if successful, false otherwise
     */
    boolean delete(URI uri) throws IOException;

    /**
     * Renames a resource at a given URI.
     * 
     * @param uri
     *            The uri to the ressource to rename.
     * @param newName
     *            The new name of the resource.
     * @return true, if successful, false otherwise.
     */
    boolean rename(URI uri, String newName);

    /**
     * Returns the uri to a given subtype of a process.
     * 
     * @param processUri
     *            The base uri of the process.
     * @param subType
     *            the subtype.
     * @param id
     *            the id of a specific element at the subtype
     * @return the URI to the requested resource.
     */
    URI getProcessSubTypeUri(URI processUri, ProcessSubType subType, int id);

    /**
     * Creates the FolderStructure needed for a process in kitodo.
     * 
     * @param processId
     *            the id of the process
     * @return the URI to the process Location
     */
    URI createProcessLocation(String processId) throws IOException;

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

    /**
     * Creates a symbolic link.
     *
     * @param targetUri
     *            The target URI for the link.
     * @param homeUri
     *            The home URI.
     * @return true, if link creation was successfull.
     */
    boolean createSymLink(URI targetUri, URI homeUri);

    /**
     * Delets a symbolik link.
     *
     * @param homeUri
     *            The uri of the home folder, where the link should be deleted.
     * @return true, if deletion was successull.
     */
    boolean deleteSymLink(URI homeUri);

    /**
     * Creates the URI, if a process and it's location already exists, so no
     * processlocation needs to be created. This method is for migration
     * purposes, because when this interface is used, there will be already
     * processes in the filesystem.
     * 
     * @param processId
     *            the id of the process.
     * @return The uri of the process location.
     */
    URI createUriForExistingProcess(String processId);

}
