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

package de.unigoettingen.sub.commons.util.stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * The Class SimpleInputStreamSaver provides a simple way to save the contents of an InputSream in a File.
 */
public class SimpleInputStreamSaver {
	
	/** The file. */
	File file;
	
	/** The is. */
	InputStream is;
	
	/**
	 * Instantiates a new simple input stream saver.
	 */
	public SimpleInputStreamSaver () {
		
	}
	
	/**
	 * Instantiates a new simple input stream saver.
	 * 
	 * @param file the file to the the contents to.
	 * @param is the InputStream to save
	 */
	public SimpleInputStreamSaver (File file, InputStream is) {
		this.file = file;
		this.is = is;
	}
	
	/**
	 * Safe the contents of the stream
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void safe () throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		FileOutputStream fos = new FileOutputStream(file);
		try {
			int bufSize = 1024 * 8;
			byte[] bytes = new byte[bufSize];
			int count = bis.read(bytes);
			while (count != -1 && count <= bufSize) {
				fos.write(bytes, 0, count);
				count = bis.read(bytes);
			}
			if (count != -1) {
				fos.write(bytes, 0, count);
			}
			fos.close();
		} finally {
			bis.close();
			fos.close();
		}
	}

	/**
	 * Gets the file.
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Sets the file.
	 * 
	 * @param file the new file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Gets the InputStream.
	 * 
	 * @return the InputStream.
	 */
	public InputStream getIs() {
		return is;
	}

	/**
	 * Sets the InputStream.
	 * 
	 * @param is the InputStream.
	 */
	public void setIs(InputStream is) {
		this.is = is;
	}
	
	

}
