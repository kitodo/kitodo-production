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

package de.sub.goobi.helper;
//TODO Replace with SUB Commons
import java.io.File;
import java.io.FilenameFilter;

import de.sub.goobi.metadaten.MetadatenImagesHelper;

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
	 * @param ext
	 *            the file extension to use for counting, not case sensitive
	 * @return number of files as Integer
	 */
	public static Integer getNumberOfFiles(File inDir) {
		int anzahl = 0;
		if (inDir.isDirectory()) {
			/* --------------------------------
			 * die Images zählen
			 * --------------------------------*/
			anzahl = inDir.list(MetadatenImagesHelper.filter).length;

			/* --------------------------------
			 * die Unterverzeichnisse durchlaufen
			 * --------------------------------*/
			String[] children = inDir.list();
			for (int i = 0; i < children.length; i++) {
				anzahl += getNumberOfFiles(new File(inDir, children[i]));
				}
		}
		return anzahl;
	}
	
	public static Integer getNumberOfFiles(String inDir) {
		return getNumberOfFiles(new File(inDir));
	}
	

	
	
	
	
	public static class FileListFilter implements FilenameFilter {
		  private String name; 
		  public FileListFilter(String name) {
		    this.name = name;
		  }
		  public boolean accept(File directory, String filename) {
		    boolean fileOK = true;
		    if (name != null) {
		      fileOK &= filename.matches(name);
		    }
		    return fileOK;
		  }
		}

}
