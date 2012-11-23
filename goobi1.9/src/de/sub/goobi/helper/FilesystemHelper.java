/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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

package de.sub.goobi.helper;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Helper class for file system operations.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class FilesystemHelper {
	private static final Logger logger = Logger
			.getLogger(FilesystemHelper.class);

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
	 *             is thrown if old file (source file of renaming) does not exists
	 */
	public static void renameFile(String oldFileName, String newFileName)
			throws IOException {
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

		if (! oldFile.exists()) {
			logger.debug("File " + oldFileName  + " does not exist for renaming.");
			throw new FileNotFoundException(oldFileName + " does not exist for renaming.");
		}

		do {
			if (SystemUtils.IS_OS_WINDOWS
					&& millisWaited == SLEEP_INTERVAL_MILLIS) {
				logger.warn("Renaming " + oldFileName  + " failed. This is Windows. Running the garbage collector may yield good results. Forcing immediate garbage collection now!");
				System.gc();
			}
			success = oldFile.renameTo(newFile);
			if (!success) {
				if (millisWaited == 0)
					logger.info("Renaming " + oldFileName + " failed. File may be locked. Retrying...");
				try {
					Thread.sleep(SLEEP_INTERVAL_MILLIS);
				} catch (InterruptedException e) {
				}
				millisWaited = SLEEP_INTERVAL_MILLIS;
			}
		} while (!success && millisWaited < MAX_WAIT_MILLIS);

		if (!success) {
			logger.error("Rename " + oldFileName + " failed. This is a permanent error. Giving up.");
			throw new IOException("Renaming of " + oldFileName + " into " +
					 newFileName + " failed.");
		}
		
		if (millisWaited > 0) {
			logger.info("Rename finally succeeded after" + Integer.toString(millisWaited) + " milliseconds.");
		}
	}
}