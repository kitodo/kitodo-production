/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
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
 * file.xml	// would be the original
 * file.xml.1	// the latest backup
 * file.xml.2	// an older backup
 * ...
 * file.xml.6	// the oldest backup, if maximum number was 6
 * </pre>
 */
public class BackupFileRotation {

	private static final Logger myLogger = Logger.getLogger(BackupFileRotation.class);

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
			myLogger.info("No files matching format '" + format + "' in directory " + processDataDirectory + " found.");
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
			myLogger.error(message);
			throw new IOException(message);
		}

		for (int count = numberOfBackups; count > 1; count--) {
			String oldName = fileName + "." + (count - 1);
			String newName = fileName + "." + count;
			try {
				FilesystemHelper.renameFile(oldName, newName);
			} catch (FileNotFoundException oldNameNotYetPresent) {
				myLogger.debug(oldName + " does not yet exist >>> nothing to do");
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
