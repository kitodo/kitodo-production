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

import org.goobi.io.SafeFile;

import java.io.File;
import java.io.FilenameFilter;

/**
 * File Utils collection
 *
 * @author Steffen Hankiewicz
 */
public class FileUtils {

	/**
	 * calculate all files with given file extension at specified directory
	 * recursivly
	 *
	 * @param inDir
	 *            the directory to run through
	 * @return number of files as Integer
	 */
	public static Integer getNumberOfFiles(SafeFile inDir) {
		int anzahl = 0;
		if (inDir.isDirectory()) {
			/* --------------------------------
			 * die Images zählen
			 * --------------------------------*/
			anzahl = inDir.list(Helper.imageNameFilter).length;

			/* --------------------------------
			 * die Unterverzeichnisse durchlaufen
			 * --------------------------------*/
			String[] children = inDir.list();
			for (int i = 0; i < children.length; i++) {
				anzahl += getNumberOfFiles(new SafeFile(inDir, children[i]));
				}
		}
		return anzahl;
	}

	public static Integer getNumberOfFiles(String inDir) {
		return getNumberOfFiles(new SafeFile(inDir));
	}






	public static class FileListFilter implements FilenameFilter {
		  private String name;
		  public FileListFilter(String name) {
		    this.name = name;
		  }
		  @Override
		public boolean accept(File directory, String filename) {
		    boolean fileOK = true;
		    if (this.name != null) {
		      fileOK &= filename.matches(this.name);
		    }
		    return fileOK;
		  }
		}

}
