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
package de.unigoettingen.sub.commons.util.datasource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The Interface ImageSource.
 */
public interface ImageSource extends DataSource {
	
	/**
	 * Gets the image.
	 * 
	 * @param pageNr the page nr
	 * 
	 * @return the image
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	abstract Image getImage (Integer pageNr) throws IOException;
	
	/**
	 * Gets the images.
	 * 
	 * @return the images
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	abstract List<? extends Image> getImageList () throws IOException;
	
	/**
	 * Gets the images.
	 * 
	 * @return the images
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	abstract Map<Integer, ? extends Image> getImageMap () throws IOException;
	
	/**
	 * Gets the number of pages.
	 * 
	 * @return the number of pages
	 */
	abstract Integer getNumberOfPages ();
		
}
