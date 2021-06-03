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

package org.kitodo.production.file;

import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.filters.FileNameMatchesFilter;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

/**
 * Creates backup for files in a given directory that match a regular
 * expression.
 *
 * <p>
 * All backup files are named by the original file with a number appended. The
 * bigger the number, the older the backup. A specified maximum number of backup
 * files are generated:
 *
 * <pre>
 * file.xml	// would be the original
 * file.xml.1	// the latest backup
 * file.xml.2	// an older backup
 * ...
 * file.xml.6	// the oldest backup, if maximum number was 6
 * </pre>
 */
public class BackupFileRotation {

    private static final Logger logger = LogManager.getLogger(BackupFileRotation.class);

    private int numberOfBackups;
    private String format;
    private Process process;

    public final FileService fileService = ServiceManager.getFileService();

    /**
     * Start the configured backup.
     *
     * <p>
     * If the maximum backup count is less then 1, nothing happens.
     *
     * @throws IOException
     *             if a file system operation fails
     */
    public void performBackup() throws IOException {
        List<URI> metaFiles;

        if (numberOfBackups < 1) {
            return;
        }

        metaFiles = generateBackupBaseNameFileList(format, process);

        if (metaFiles.isEmpty()) {
            logger.info("No files matching format '{}' in directory {} found.",
                    this.format, ServiceManager.getProcessService().getProcessDataDirectory(process));
            return;
        }

        for (URI metaFile : metaFiles) {
            createBackupForFile(metaFile);
        }
    }

    /**
     * Set the number of backup files to create for each individual original
     * file.
     *
     * @param numberOfBackups
     *            Maximum number of backup files
     */
    public void setNumberOfBackups(int numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    /**
     * Set file name matching pattern for original files to create backup files
     * for.
     *
     * @param format
     *            Java regular expression string.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Sets the process.
     *
     * @param process
     *            the process
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    private void createBackupForFile(URI fileName) throws IOException {
        rotateBackupFilesFor(fileName);

        String newName = fileService.getFileNameWithExtension(fileName) + ".1";
        fileService.renameFile(fileName, newName);
    }

    private void rotateBackupFilesFor(URI fileName) throws IOException {

        URI oldest = URI.create(fileName + "." + numberOfBackups);
        if (fileService.fileExist(oldest)) {
            boolean deleted = fileService.delete(oldest);
            if (!deleted) {
                String message = "Could not delete " + oldest;
                logger.error(message);
                throw new IOException(message);
            }
        }

        for (int count = numberOfBackups; count > 1; count--) {
            URI oldName = URI.create(fileName + "." + (count - 1));
            String newName = fileService.getFileNameWithExtension(fileName) + "." + count;
            try {
                fileService.renameFile(oldName, newName);
            } catch (FileNotFoundException oldNameNotYetPresent) {
                logger.debug("{} does not yet exist >>> nothing to do", oldName);
            }
        }
    }

    private List<URI> generateBackupBaseNameFileList(String filterFormat, Process process) {
        FilenameFilter filter = new FileNameMatchesFilter(filterFormat);

        URI processDataDirectory = ServiceManager.getProcessService().getProcessDataDirectory(process);
        return fileService.getSubUris(filter, processDataDirectory);
    }

}
