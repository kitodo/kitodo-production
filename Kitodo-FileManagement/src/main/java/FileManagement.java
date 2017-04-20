
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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.kitodo.api.filemanagement.FileManagementInterface;
import org.kitodo.api.filemanagement.ProcessLocation;

public class FileManagement implements FileManagementInterface {
    @Override
    public OutputStream write(URI uri) {
        return null;
    }

    @Override
    public InputStream read(URI uri) {
        return null;
    }

    @Override
    public boolean delete(URI uri) {
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
