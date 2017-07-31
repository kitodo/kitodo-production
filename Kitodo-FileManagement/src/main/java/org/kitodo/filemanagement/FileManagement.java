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

package org.kitodo.filemanagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.config.Config;

public class FileManagement implements FileManagementInterface {

    private static final String METADATA_DIRECTORY = "MetadatenVerzeichnis";

    @Override
    public OutputStream write(URI uri) throws IOException {
        return new FileOutputStream(new File(uri));
    }

    @Override
    public InputStream read(URI uri) throws IOException {
        URL url = uri.toURL();
        return url.openStream();
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        File file = new File(uri);
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
            return true;
        }
        return false;
    }

    @Override
    public void copy(URI sourceResource, URI targetResource) throws IOException {

    }

    @Override
    public void move(URI sourceUri, URI targetUri) throws IOException {

    }

    @Override
    public URI rename(URI uri, String newName) {
        return URI.create("");
    }

    @Override
    public boolean fileExist(URI uri) {
        return false;
    }

    @Override
    public boolean isFile(URI uri) {
        return false;
    }

    @Override
    public boolean isDirectory(URI directory) {
        return false;
    }

    @Override
    public boolean canRead(URI uri) {
        return false;
    }

    @Override
    public Integer getNumberOfFiles(FilenameFilter filter, URI directory) {
        return 0;
    }

    @Override
    public String getFileNameWithExtension(URI uri) {
        return "";
    }

    public ArrayList<URI> getSubUris(FilenameFilter filter, URI uri) {
        return new ArrayList<>();
    }

    @Override
    public URI createProcessLocation(String processId) throws IOException {
        File processRootDirectory = new File(
                (Config.getConfig().getString(METADATA_DIRECTORY) + File.separator + processId));
        if (!processRootDirectory.mkdir()) {
            throw new IOException("Could not create processRoot directory.");
        }
        return processRootDirectory.toURI();

    }

    @Override
    public URI getProcessSubTypeUri(URI processBaseUri, ProcessSubType subType, int id) {
        return null;
    }

    @Override
    public URI create(URI parentFolderUri, String directoryName, boolean file) throws IOException {
        File directory = new File(parentFolderUri.getPath() + File.separator + directoryName);
        if (!directory.mkdir()) {
            throw new IOException("Could not create directory.");
        }
        return directory.toURI();
    }

    @Override
    public boolean createSymLink(URI targetUri, URI homeUri, boolean onlyRead, String userLogin) {
        return false;
    }

    @Override
    public boolean deleteSymLink(URI homeUri) {
        return false;
    }

    @Override
    public URI createUriForExistingProcess(String processId) {
        return null;
    }
}
