/*
 * This file is part of the SUB Commons  project.
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

public class SimpleStructure extends AbstractStructure<SimpleStructure> {
	
	/**
	 * Instantiates a new simple structure.
	 */
	public SimpleStructure() {

	}
		
	/**************************************************************************************
	 * Constructor which create a new bookmark with pagename and content
	 * 
	 * @param pagename as Integer
	 * @param content as String
	 **************************************************************************************/
	public SimpleStructure(Integer pagename, String content) {
		super(pagename, content);
	}

}
