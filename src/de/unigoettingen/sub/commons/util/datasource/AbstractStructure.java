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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractStructure <S extends AbstractStructure<S>> implements Structure {
	protected List<S> children;
	protected Integer imagenumber; // imagenumber, integer from imageURLs HashMap
	protected String content; // content of bookmark
	
	/**
	 * Instantiates a new abstract structure.
	 */
	public AbstractStructure() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Instantiates a new abstract structure.
	 * 
	 * @param struct the struct
	 */
	public AbstractStructure(Structure struct) {
		this(struct.getImageNumber(), struct.getContent());
	}
	
		
	/**************************************************************************************
	 * Constructor which create a new bookmark with pagename and content
	 * 
	 * @param pagename as Integer
	 * @param content as String
	 **************************************************************************************/
	public AbstractStructure(Integer pagename, String content) {
		this.imagenumber = pagename;
		this.content = content;
	}
	
	/*************************************************************************************
	 * Getter for children
	 *
	 * @return the children
	 *************************************************************************************/
	public List<S> getChildren() {
		if (children != null) {
			return children;
		} else {
			return new LinkedList<S>();
		}
	}

	/**************************************************************************************
	 * Add bookmark to list of children
	 * 
	 * @param child the Bookmark to add as child
	 **************************************************************************************/
	public void addChildBookmark(S child) {
		if (children == null) { // no list available, create one for all child bookmarks
			children = new ArrayList<S>();
		}
		children.add(child);
	}

	/**************************************************************************************
	 * Setter for children
	 *
	 * @param children the children to set
	 **************************************************************************************/
	public void setChildren(List<S> children) {
		this.children = children;
	}

	
	/*************************************************************************************
	 * Getter for content
	 *
	 * @return the content
	 *************************************************************************************/
	public String getContent() {
		return content;
	}

	/*************************************************************************************
	 * Getter for imagenumber
	 *
	 * @return the imagenumber
	 *************************************************************************************/
	public Integer getImageNumber() {
		return imagenumber;
	}
	
	/**************************************************************************************
	 * Setter for content
	 *
	 * @param content the content to set
	 **************************************************************************************/
	public void setContent(String content) {
		this.content = content;
	}

	/**************************************************************************************
	 * Setter for imagenumber
	 *
	 * @param imagenumber the imagenumber to set
	 **************************************************************************************/
	public void setImageNumber(Integer imagenumber) {
		this.imagenumber = imagenumber;
	}

}
