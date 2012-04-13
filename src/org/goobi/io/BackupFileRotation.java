/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.io;

import de.sub.goobi.helper.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;

public class BackupFileRotation {

	private static final Logger myLogger = Logger.getLogger(BackupFileRotation.class);

	private int numberOfBackups;
	private String format;
	private String processDataDirectory;

	public void performBackup() {
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

	public void setNumberOfBackups(int numberOfBackups) {
		this.numberOfBackups = numberOfBackups;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setProcessDataDirectory(String processDataDirectory) {
		this.processDataDirectory = processDataDirectory;
	}

	private void rename(String oldFileName, String newFileName) {
		File oldFile = new File(oldFileName);
		File newFile = new File(newFileName);

		if (oldFile.exists()) {
			boolean renameSuccessful = oldFile.renameTo(newFile);

			if (!renameSuccessful) {
				myLogger.warn("Renaming file from " + oldFileName + " to " +  newFileName + " failed.");
			}
		}
	}

	private void createBackupForFile(String fileName) {
		rotateBackupFilesFor(fileName);

		String newName = fileName + ".1";
		rename(fileName, newName);
	}

	private void rotateBackupFilesFor(String fileName) {
		for (int count = numberOfBackups; count > 1; count--) {
			String oldName = fileName + "." + (count - 1);
			String newName = fileName + "." + count;
			rename(oldName, newName);
		}
	}

	private File[] generateBackupBaseNameFileList(String filterFormat, String directoryOfBackupFiles) {
		FilenameFilter filter = new FileUtils.FileListFilter(filterFormat);
		File metaFilePath = new File(directoryOfBackupFiles);

		return metaFilePath.listFiles(filter);
	}

}
