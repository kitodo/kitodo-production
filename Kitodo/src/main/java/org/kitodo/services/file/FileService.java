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

package org.kitodo.services.file;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ShellScript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.goobi.io.SafeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    // program options initialized to default values
    private static final int BUFFER_SIZE = 4 * 1024;

    public void createDirectory(URI parentFolderUri, String directoryName) throws IOException {
        if (!new File(parentFolderUri + directoryName).exists()) {
            ShellScript createDirScript = new ShellScript(new File(ConfigCore.getParameter("script_createDirMeta")));
            createDirScript.run(Arrays.asList(new String[] {parentFolderUri + directoryName }));
        }

    }

    /**
     * Creates a directory with a name given and assigns permissions to the
     * given user. Under Linux a script is used to set the file system
     * permissions accordingly. This cannot be done from within java code before
     * version 1.7.
     *
     * @param dirName
     *            Name of directory to create
     * @throws InterruptedException
     *             If the thread running the script is interrupted by another
     *             thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     * @throws IOException
     *             If an I/O error occurs.
     */

    public void createDirectoryForUser(String dirName, String userName) throws IOException, InterruptedException {
        if (!new File(dirName).exists()) {
            ShellScript createDirScript = new ShellScript(
                    new File(ConfigCore.getParameter("script_createDirUserHome")));
            createDirScript.run(Arrays.asList(userName, dirName));
        }
    }

    /**
     * This function implements file renaming. Renaming of files is full of
     * mischief under Windows which unaccountably holds locks on files.
     * Sometimes running the JVM’s garbage collector puts things right.
     *
     * @param oldFileName
     *            File to move or rename
     * @param newFileName
     *            New file name / destination
     * @throws IOException
     *             is thrown if the rename fails permanently
     * @throws FileNotFoundException
     *             is thrown if old file (source file of renaming) does not
     *             exists
     */
    public void renameFile(String oldFileName, String newFileName) throws IOException {
        final int SLEEP_INTERVAL_MILLIS = 20;
        final int MAX_WAIT_MILLIS = 150000; // 2½ minutes
        File oldFile;
        File newFile;
        boolean success;
        int millisWaited = 0;

        if ((oldFileName == null) || (newFileName == null)) {
            return;
        }

        oldFile = new File(oldFileName);
        newFile = new File(newFileName);

        if (!oldFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("File " + oldFileName + " does not exist for renaming.");
            }
            throw new FileNotFoundException(oldFileName + " does not exist for renaming.");
        }

        if (newFile.exists()) {
            String message = "Renaming of " + oldFileName + " into " + newFileName + " failed: Destination exists.";
            logger.error(message);
            throw new IOException(message);
        }

        do {
            if (SystemUtils.IS_OS_WINDOWS && millisWaited == SLEEP_INTERVAL_MILLIS) {
                logger.warn("Renaming " + oldFileName
                        + " failed. This is Windows. Running the garbage collector may yield good results. "
                        + "Forcing immediate garbage collection now!");
                System.gc();
            }
            success = oldFile.renameTo(newFile);
            if (!success) {
                if (millisWaited == 0 && logger.isInfoEnabled()) {
                    logger.info("Renaming " + oldFileName + " failed. File may be locked. Retrying...");
                }
                try {
                    Thread.sleep(SLEEP_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                }
                millisWaited += SLEEP_INTERVAL_MILLIS;
            }
        } while (!success && millisWaited < MAX_WAIT_MILLIS);

        if (!success) {
            logger.error("Rename " + oldFileName + " failed. This is a permanent error. Giving up.");
            throw new IOException("Renaming of " + oldFileName + " into " + newFileName + " failed.");
        }

        if (millisWaited > 0 && logger.isInfoEnabled()) {
            logger.info("Rename finally succeeded after" + Integer.toString(millisWaited) + " milliseconds.");
        }
    }

    /**
     * calculate all files with given file extension at specified directory
     * recursively.
     *
     * @param inDir
     *            the directory to run through
     * @return number of files as Integer
     */
    public Integer getNumberOfFiles(SafeFile inDir) {
        int anzahl = 0;
        if (inDir.isDirectory()) {
            /*
             * die Images zählen
             */
            anzahl = inDir.list(Helper.imageNameFilter).length;

            /*
             * die Unterverzeichnisse durchlaufen
             */
            String[] children = inDir.list();
            for (int i = 0; i < children.length; i++) {
                anzahl += getNumberOfFiles(new SafeFile(inDir, children[i]));
            }
        }
        return anzahl;
    }

    public Integer getNumberOfFiles(String inDir) {
        return getNumberOfFiles(new SafeFile(inDir));
    }

    private Long copyFile(File srcFile, File destFile) throws IOException {
        // TODO use a better checksumming algorithm like SHA-1
        CRC32 checksum = new CRC32();
        checksum.reset();

        try (InputStream in = read(srcFile.toURI()); OutputStream out = write(destFile.toURI())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                checksum.update(buffer, 0, bytesRead);
                out.write(buffer, 0, bytesRead);
            }
        }
        return Long.valueOf(checksum.getValue());
    }

    /**
     * Start copying of file.
     *
     * @param srcFile
     *            source file
     * @param destFile
     *            destination file
     * @return Long
     */
    public Long start(File srcFile, File destFile) throws IOException {
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

    private Long createChecksum(File file) throws IOException {
        CRC32 checksum = new CRC32();
        checksum.reset();
        try (InputStream in = read(file.toURI())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                checksum.update(buffer, 0, bytesRead);
            }
        }
        return Long.valueOf(checksum.getValue());
    }

    public OutputStream write(URI uri) throws IOException {
        return new FileOutputStream(new File(uri));
    }

    public InputStream read(URI uri) throws IOException {
        URL url = uri.toURL();
        return url.openStream();
    }

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
}
