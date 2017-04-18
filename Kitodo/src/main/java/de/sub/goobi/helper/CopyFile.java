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

package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import org.goobi.io.SafeFile;

// Only usage: in de.sub.goobi.helper.tasks.ProcessSwapOutTask
public class CopyFile {

    // program options initialized to default values
    private static final int BUFFER_SIZE = 4 * 1024;

    private static Long copyFile(SafeFile srcFile, SafeFile destFile) throws IOException {
        // TODO use a better checksumming algorithm like SHA-1
        CRC32 checksum = new CRC32();
        checksum.reset();

        try (InputStream in = srcFile.createFileInputStream(); OutputStream out = destFile.createFileOutputStream();) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                checksum.update(buffer, 0, bytesRead);
                out.write(buffer, 0, bytesRead);
            }
        }
        return Long.valueOf(checksum.getValue());
    }

    private static Long createChecksum(SafeFile file) throws IOException {
        CRC32 checksum = new CRC32();
        checksum.reset();
        try (InputStream in = file.createFileInputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                checksum.update(buffer, 0, bytesRead);
            }
        }
        return Long.valueOf(checksum.getValue());
    }

    /**
     * start copying of file.
     *
     * @param srcFile
     *            source file
     * @param destFile
     *            destination file
     * @return Long
     */
    public static Long start(SafeFile srcFile, SafeFile destFile) throws IOException {
        // make sure the source file is indeed a readable file
        if (!srcFile.isFile() || !srcFile.canRead()) {
            System.err.println("Not a readable file: " + srcFile.getName());
        }

        // copy file, optionally creating a checksum
        Long checksumSrc = copyFile(srcFile, destFile);

        // copy timestamp of last modification
        if (!destFile.setLastModified(srcFile.lastModified())) {
            System.err.println("Error: Could not set " + "timestamp of copied file.");
        }

        // verify file
        Long checksumDest = createChecksum(destFile);
        if (checksumSrc.equals(checksumDest)) {
            return checksumDest;
        } else {
            return Long.valueOf(0);
        }
    }
}
