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

package org.kitodo.dataeditor;

import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class FileService {

    /**
     * Get all sub URIs of an URI.
     *
     * @param uri
     *            the URI, to get the sub URIs from
     * @return a List of sub URIs
     */
    public List<URI> getSubUris(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getSubUris(null, uri);
    }

    /**
     * Get all sub URIs of an URI with a given filter.
     *
     * @param filter
     *            the filter to filter the sub URIs
     * @param uri
     *            the URI, to get the sub URIs from
     * @return a List of sub URIs
     */
    public List<URI> getSubUris(FilenameFilter filter, URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.getSubUris(filter, uri);
    }

    private FileManagementInterface getFileManagementModule() {
        KitodoServiceLoader<FileManagementInterface> loader = new KitodoServiceLoader<>(FileManagementInterface.class);
        return loader.loadModule();
    }

    /**
     * Creates a directory at a given URI with a given name.
     *
     * @param parentFolderUri
     *            the uri, where the directory should be created
     * @param directoryName
     *            the name of the directory.
     * @return the URI of the new directory or URI of parent directory if
     *         directoryName is null or empty
     */
    public URI createDirectory(URI parentFolderUri, String directoryName) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        if (directoryName != null) {
            return fileManagementModule.create(parentFolderUri, directoryName, false);
        }
        return URI.create("");
    }

    /**
     * Creates a resource at a given URI with a given name.
     *
     * @param targetFolder
     *            the URI of the target folder
     * @param name
     *            the name of the new resource
     * @return the URI of the created resource
     */
    public URI createResource(URI targetFolder, String name) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.create(targetFolder, name, true);
    }

    /**
     * Checks if a resource at a given URI is a file.
     *
     * @param uri
     *            the URI to check, if there is a file
     * @return true, if it is a file, false otherwise
     */
    public boolean isFile(URI uri) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.isFile(uri);
    }

    /**
     * checks, if a URI leads to a directory.
     *
     * @param dir
     *            the uri to check.
     * @return true, if it is a directory.
     */
    public boolean isDirectory(URI dir) {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.isDirectory(dir);
    }

    /**
     * Deletes a resource at a given URI.
     *
     * @param uri
     *            the uri to delete
     * @return true, if successful, false otherwise
     * @throws IOException
     *             if get of module fails
     */
    public boolean delete(URI uri) throws IOException {
        FileManagementInterface fileManagementModule = getFileManagementModule();
        return fileManagementModule.delete(uri);
    }
}
