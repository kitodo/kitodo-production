/*
 * This file is part of the SUB Commons project.
 * Visit the websites for more information. 
 * 		- http://gdz.sub.uni-goettingen.de 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * intranda software.
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
package de.unigoettingen.sub.commons.util.datasource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectoryListingUrlImageSource.
 */
public class DirectoryListingUrlImageSource implements ImageSource {
	
	/** The dir. */
	protected File dir = null;
	
	/** The files. */
	private List<File> files = null;
	
	/**
	 * Instantiates a new directory listing url image source.
	 * 
	 * @param dir the dir
	 */
	public DirectoryListingUrlImageSource (File dir) {
		this.dir = dir;
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.ImageSource#getImage(java.lang.Integer)
	 */
	public Image getImage(Integer pageNr) throws IOException {
		init();
		SimpleUrlImage img = new SimpleUrlImage();
		img.setPageNumber(pageNr);
		img.setURL(files.get(pageNr).toURI().toURL());
		return img;
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.ImageSource#getImageList()
	 */
	public List<? extends Image> getImageList() throws IOException {
		init();
		List<SimpleUrlImage> images = new ArrayList<SimpleUrlImage>();
		for (int i = 0; i < files.size(); i++) {
			SimpleUrlImage img = new SimpleUrlImage();
			img.setPageNumber(i + 1);
			img.setURL(files.get(i).toURI().toURL());
			images.add(img);
		}
		return images;
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.ImageSource#getImageMap()
	 */
	public Map<Integer, ? extends Image> getImageMap() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.ImageSource#getNumberOfPages()
	 */
	public Integer getNumberOfPages() {
		try {
			init();
		} catch (IOException e) {
			return 0;
		}
		return files.size();
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.DataSource#close()
	 */
	public void close() throws IOException {
		//Do nothing
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.DataSource#getUrl()
	 */
	public URL getUrl() {
		try {
			return dir.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Checks the given directory and add its contens as list
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void init () throws IOException {
		if (files == null) {
			if (!dir.isDirectory()) {
				throw new IOException("Given File is not a directory");
			}
			files = Arrays.asList(dir.listFiles());
			//TODO: Filter for supported Filetypes
		}
	}

}
