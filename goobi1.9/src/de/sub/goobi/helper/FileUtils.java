package de.sub.goobi.helper;

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
			anzahl = inDir.list(Helper.imageNameFilter).length;

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
