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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * Manages the handling of files.
 */
public interface FileManagementInterface {

    /**
     * Creates a resource (file/folder) with a given name. Important is that
     * resource will be created only in case when parent folder exists.
     *
     * @param parentFolderUri
     *            the location to create the resource
     * @param name
     *            the name of the new resource
     * @param file
     *            if true it creates the file, if false it created directory
     * @return the URI of the new resource
     */
    URI create(URI parentFolderUri, String name, boolean file) throws IOException;

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
     * Copy resource.
     *
     * @param sourceResource
     *            source file as uri
     * @param targetResource
     *            destination file as uri
     */
    void copy(URI sourceResource, URI targetResource) throws IOException;

    /**
     * Delete content at a given URI.
     *
     * @param uri
     *            the URI to delete
     * @return true if successful, false otherwise
     */
    boolean delete(URI uri) throws IOException;

    /**
     * Moves a resource from a given URI to a given URI.
     *
     * @param sourceUri
     *            the source URI
     * @param targetUri
     *            the target URI
     * @throws IOException
     *             if directory cannot be accessed
     */
    void move(URI sourceUri, URI targetUri) throws IOException;

    /**
     * Renames a resource at a given URI.
     *
     * @param uri
     *            the URI to the resource to rename
     * @param newName
     *            the new name of the resource
     * @return URI of renamed resource
     */
    URI rename(URI uri, String newName) throws IOException;

    /**
     * Calculate all files with given file extension at specified directory
     * recursively.
     *
     * @param directory
     *            the directory to run through
     * @return number of files as Integer
     */
    Integer getNumberOfFiles(FilenameFilter filter, URI directory);

    /**
     * Get size of directory.
     *
     * @param directory
     *            URI to get size
     * @return size of directory as Long
     */
    Long getSizeOfDirectory(URI directory) throws IOException;

    /**
     * Returns the name of a file with extension at a given URI.
     *
     * @param uri
     *            the URI, to get the filename from
     * @return the name of the file with extension
     */
    String getFileNameWithExtension(URI uri);

    /**
     * Checks, if a file exists.
     *
     * @param uri
     *            the URI, to check, if there is a file.
     * @return true, if the file exists, false otherwise
     */
    boolean fileExist(URI uri);

    /**
     * Checks if a resource at a given uri is a file.
     *
     * @param uri
     *            the uri to check, if there is a file.
     * @return true, if it is a file, false otherwise
     */
    boolean isFile(URI uri);

    /**
     * Checks, if a URI leads to a directory.
     *
     * @param directory
     *            the URI to check
     * @return true, if it is a directory
     */
    boolean isDirectory(URI directory);

    /**
     * Checks if an URI is readable.
     *
     * @param uri
     *            the URI to check
     * @return true, if it's readable, false otherwise
     */
    boolean canRead(URI uri);

    /**
     * Get all sub URIs of an given URI.
     *
     * @param filter
     *            the filter to filter the sub URIs
     * @param uri
     *            the URI, to get the sub URIs from
     * @return a List of sub URIs
     */
    List<URI> getSubUris(FilenameFilter filter, URI uri);

    /**
     * Creates the FolderStructure needed for a process in kitodo.
     *
     * @param processId
     *            the id of the process
     * @return the URI to the process Location
     */
    URI createProcessLocation(String processId) throws IOException;

    /**
     * Creates the URI, if a process and it's location already exists, so no process
     * location needs to be created. This method is for migration purposes, because
     * when this interface is used, there will be already processes in the
     * filesystem.
     *
     * @param processId
     *            the id of the process.
     * @return The uri of the process location.
     */
    URI createUriForExistingProcess(String processId);

    /**
     * Returns the uri to a given subtype of a process.
     *
     * @param processUri
     *            the base uri of the process.
     * @param subType
     *            the subtype.
     * @param resourceName
     *            the id of a specific element at the subtype
     * @return the URI to the requested resource.
     */
    URI getProcessSubTypeUri(URI processUri, String processTitle, ProcessSubType subType, String resourceName);

    /**
     * Creates a symbolic link.
     *
     * @param homeUri
     *            the home URI
     * @param targetUri
     *            the target URI for the link
     * @param onlyRead
     *            boolean, true if user has only read rights, false otherwise
     * @param userLogin
     *            login of the user
     * @return true, if link creation was successful.
     */
    boolean createSymLink(URI homeUri, URI targetUri, boolean onlyRead, String userLogin);

    /**
     * Delete a symbolic link.
     *
     * @param homeUri
     *            the uri of the home folder, where the link should be deleted.
     * @return true, if deletion was successful.
     */
    boolean deleteSymLink(URI homeUri);

    /**
     * Temporal method until UGH is used.
     *
     * @param uri
     *            URI
     * @return File
     */
    File getFile(URI uri);

}
