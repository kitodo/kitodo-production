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

import java.io.File;
import java.io.FilenameFilter;

public class BackupFileRotation {

	private int numberOfBackups;
	private String format;
	private String processDataDirectory;

	public void performBackup() {
		FilenameFilter filter = new FileUtils.FileListFilter(format);
		File metaFilePath = new File(processDataDirectory);
		File[] meta = metaFilePath.listFiles(filter);
		int count;
		if (meta != null) {
			if (meta.length > numberOfBackups) {
				count = numberOfBackups;
			} else {
				count = meta.length;
			}
			while (count >= 0) {
				for (File data : meta) {
					int length = data.toString().length();
					if (data.toString().contains("xml." + (count - 1))) {
						File newFile = new File(data.toString().substring(0, length - 2) + "." + (count));
						data.renameTo(newFile);
					}
					if (data.toString().endsWith(".xml")) {
						File newFile = new File(data.toString() + ".1");
						data.renameTo(newFile);
					}
				}
				count--;
			}
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
}
