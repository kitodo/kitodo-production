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

import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * The Interface Image.
 */
public interface Image {

	/**
	 * Gets the rendered image.
	 * 
	 * @return the rendered image
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	abstract RenderedImage getRenderedImage () throws IOException;
	
	/**
	 * Gets the page number.
	 * 
	 * @return the page number
	 */
	abstract Integer getPageNumber ();
	
}
