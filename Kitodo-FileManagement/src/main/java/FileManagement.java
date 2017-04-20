
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessLocation;

public class FileManagement implements FileManagementInterface {
    @Override
    public OutputStream write(URI uri) throws IOException {
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        return urlConnection.getOutputStream();
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
    public ProcessLocation createProcessLocation(String processId) {
        return null;
    }

    @Override
    public URI createUserHomeLocation(String userId) {
        return null;
    }

    @Override
    public URI createResource(URI parentFolderUri, String fileEnding) {
        return null;
    }
}
