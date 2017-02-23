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

package org.goobi.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.log4j.Logger;

import de.sub.goobi.helper.FilesystemHelper;

/**
 * Creates backup for files in a given directory that match a regular expression.
 *
 * All backup files are named by the original file with a number appended. The bigger the number, the older the backup. A specified maximum number of
 * backup files are generated:
 *
 * <pre>
 * file.xml // would be the original
 * file.xml.1   // the latest backup
 * file.xml.2   // an older backup
 * ...
 * file.xml.6   // the oldest backup, if maximum number was 6
 * </pre>
 */
public class BackupFileRotation {

    private static final Logger logger = Logger.getLogger(BackupFileRotation.class);

    private int numberOfBackups;
    private String format;
    private String processDataDirectory;

    /**
     * Start the configured backup.
     *
     * If the maximum backup count is less then 1, nothing happens.
     *
     * @throws IOException
     *             if a file system operation fails
     */
    public void performBackup() throws IOException {
        File[] metaFiles;

        if (numberOfBackups < 1) {
            return;
        }

        metaFiles = generateBackupBaseNameFileList(format, processDataDirectory);

        if (metaFiles.length < 1) {
            if(logger.isInfoEnabled()){
                logger.info("No files matching format '" + format + "' in directory " + processDataDirectory + " found.");
            }
            return;
        }

        for (File metaFile : metaFiles) {
            createBackupForFile(metaFile.getPath());
        }
    }

    /**
     * Set the number of backup files to create for each individual original file.
     *
     * @param numberOfBackups
     *            Maximum number of backup files
     */
    public void setNumberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    /**
     * Set file name matching pattern for original files to create backup files for.
     *
     * @param format
     *            Java regular expression string.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Set the directory to find the original files and to place the backup files.
     *
     * @param processDataDirectory
     *            A platform specific filesystem path
     */
    public void setProcessDataDirectory(String processDataDirectory) {
        this.processDataDirectory = processDataDirectory;
    }

    private void createBackupForFile(String fileName) throws IOException {
        rotateBackupFilesFor(fileName);

        String newName = fileName + ".1";
        FilesystemHelper.renameFile(fileName, newName);
    }

    private void rotateBackupFilesFor(String fileName) throws IOException {
        File oldest = new File(fileName + "." + numberOfBackups);
        if (oldest.exists() && !oldest.delete()) {
            String message = "Could not delete " + oldest.getAbsolutePath();
            logger.error(message);
            throw new IOException(message);
        }

        for (int count = numberOfBackups; count > 1; count--) {
            String oldName = fileName + "." + (count - 1);
            String newName = fileName + "." + count;
            try {
                FilesystemHelper.renameFile(oldName, newName);
            } catch (FileNotFoundException oldNameNotYetPresent) {
                if(logger.isDebugEnabled()){
                    logger.debug(oldName + " does not yet exist >>> nothing to do");
                }
                continue;
            }
        }
    }

    private File[] generateBackupBaseNameFileList(String filterFormat, String directoryOfBackupFiles) {
        FilenameFilter filter = new FileListFilter(filterFormat);
        File metaFilePath = new File(directoryOfBackupFiles);

        return metaFilePath.listFiles(filter);
    }

}
