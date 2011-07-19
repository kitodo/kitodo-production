package de.sub.goobi.helper;
//TODO Replace with SUB Commons
import java.io.File;
import java.io.FilenameFilter;

import de.sub.goobi.Metadaten.MetadatenImagesHelper;

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
//			FilenameFilter filter = new FilenameFilter() {
//				public boolean accept(File dir, String name) {
//					return name.toLowerCase().endsWith(ext.toLowerCase());
//				}
//			};
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
