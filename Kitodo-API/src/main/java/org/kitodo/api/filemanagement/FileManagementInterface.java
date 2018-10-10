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
import java.net.ProtocolException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

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
     * Attempts to get locks on one or more files. There are only two results:
     * either, all locks can be granted, or none of the requested locks are
     * granted. In the former case, the conflicts map in the locking result is
     * empty, in the latter case it contains for each conflicting file the users
     * who hold a conflicting lock on the file. If no locks have been granted,
     * the call to {@code close()} on the locking result is meaningless, meaning
     * that leaving the try-with-resources statement will not throw an
     * exception. Just to mention that.
     * 
     * @param user
     *            A human-readable string that identifies the user or process
     *            requesting the locks. This string will later be returned to
     *            other users if they try to request a conflicting lock.
     * @param requests
     *            the locks to request
     * @return An object that manages allocated locks or provides information
     *         about conflict originators in case of error.
     * @throws IOException
     *             if the file does not exist or if an error occurs in disk
     *             access, e.g. because the write permission for the directory
     *             is missing
     */
    LockingResult tryLock(String user, Map<URI, LockingMode> requests) throws IOException;

    /**
     * Opens an OutputStream to a given uri.
     *
     * @param uri
     *            the uri to write to
     * @return an writable OutputStream
     * @deprecated This write function creates an exclusive lock with the
     *             meaningless user name “System”. Therefore, this writing
     *             function should not be used anymore. Use
     *             {@link #write(URI, LockingResult)} instead.
     */
    @Deprecated
    OutputStream write(URI uri) throws IOException;

    /**
     * Opens an OutputStream to a given uri.
     *
     * @param uri
     *            the uri to write to
     * @param permission
     *            the result of a successful lock operation that authorizes the
     *            opening of the stream
     * @return an writable OutputStream
     * @throws AccessDeniedException
     *             if the user does not have sufficient authorization
     * @throws ProtocolException
     *             if the file had to be first read in again, but this step was
     *             skipped on the protocol. This error can occur with the
     *             UPGRADE_WRITE_ONCE lock because its protocol form requires
     *             that the file must first be read in again and the input
     *             stream must be closed after the lock has been upgraded.
     */
    OutputStream write(URI uri, LockingResult permission) throws IOException;

    /**
     * Opens an InputStream to a given uri.
     *
     * @param uri
     *            the uri to write from
     * @return a readable InputStream
     * @deprecated This read function creates an exclusive lock with the
     *             meaningless user name “System”. Therefore, this reading
     *             function should not be used anymore. Use
     *             {@link #read(URI, LockingResult)} instead.
     */
    @Deprecated
    InputStream read(URI uri) throws IOException;

    /**
     * Opens an InputStream to a given URI.
     *
     * @param uri
     *            the URI to read from
     * @param permission
     *            the result of a successful lock operation that authorizes the
     *            opening of the stream
     * @return a readable InputStream
     * @throws AccessDeniedException
     *             if the user does not have sufficient authorization
     */
    InputStream read(URI uri, LockingResult permission) throws IOException;

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
     * @return true, if successful, false otherwise
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
