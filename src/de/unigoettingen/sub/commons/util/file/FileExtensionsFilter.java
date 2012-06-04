/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://gdz.sub.uni-goettingen.de 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.unigoettingen.sub.commons.util.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;


//TODO: This is guaranted to work case insensive only if you add extensions as String
/**
 * The Class FileExtensionsFilter implements a FileFilter. It can be used to filter files based on their extensions.
 */
public class FileExtensionsFilter implements FileFilter {
	
	/** The extension. */
	protected List<String> extensions = new ArrayList<String>();
	
	/**
	 * Instantiates a new file extensions filter.
	 * 
	 * @param extension the extension
	 */
	public FileExtensionsFilter (String extension) {
		this.extensions.add(extension.toLowerCase());
	}
	
	/**
	 * Instantiates a new file extensions filter.
	 * 
	 * @param extensions the extensions
	 */
	public FileExtensionsFilter (List<String> extensions) {
		this.extensions = extensions;
	};
	
	/**
	 * Adds the extension.
	 * 
	 * @param extension the extension
	 */
	public void addExtension (String extension) {
		this.extensions.add(extension.toLowerCase());
	}
	
	/**
	 * Sets the extension.
	 * 
	 * @param extensions the new extension
	 */
	public void setExtension (List<String> extensions) {
		this.extensions = extensions;
	}
	
	/**
	 * Gets the extensions.
	 * 
	 * @return the extensions
	 */
	public List<String> getExtensions () {
		return extensions;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept (File pathname) {
		if (extensions.contains(FileUtils.getFileExtensionFromFile(pathname).toLowerCase())) {
			return true;
		}
		return false;	
	}
	
	/**
	 * Utility Method to get the extension of a file
	 * 
	 * @param file the file
	 * 
	 * @return the extension
	 */
	public static String getExtension(String file) {
		return file.substring(file.lastIndexOf(".") + 1).toLowerCase();
	}

	/**
	 * Utility Method to get the extension of a file
	 * 
	 * @param file the file
	 * 
	 * @return the extension
	 */
	public static String getExtension(File file) {
		return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1).toLowerCase();
	}

}
