/*
 * This file is a contribution to the the ContentServer project, mainly for research purposes.
 * 
 * Copyright 2009, Christian Mahnke<cmahnke@gmail.com>.
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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

//Avoid logger as external dependency here, try to wrap System.out.* streams
//import org.apache.log4j.Logger;

public class ImageSourceIterator implements Iterator<Image>, Iterable<Image> {
	//protected static Logger logger = Logger.getLogger(ImageSourceIterator.class);

	ImageSource is = null;
	Integer pageNr = -1;

	/**
	 * Instantiates a new image source iterator.
	 * 
	 * @param is the ImageSource
	 */
	public ImageSourceIterator(ImageSource is) {
		this.is = is;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (pageNr < is.getNumberOfPages()) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Image next() {
		pageNr++;
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		try {
			return is.getImage(pageNr);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Image> iterator() {
		return this;
	}
}
