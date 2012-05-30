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

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface Structure.
 */
public interface Structure {

	/**
	 * Gets the image number.
	 * 
	 * @return the image number
	 */
	abstract Integer getImageNumber ();
	
	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	abstract String getContent ();
	
	/**
	 * Gets the children.
	 * 
	 * @return the children
	 */
	abstract List<? extends Structure> getChildren ();
}
