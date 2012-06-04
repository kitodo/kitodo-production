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

import java.net.URL;

public class SimpleUrlImage extends AbstractUrlImage implements UrlImage {
	
	/**
	 * Instantiates a new simple url image.
	 * 
	 * @param pageNumber the page number
	 * @param url the url
	 */
	public SimpleUrlImage (Integer pageNumber, URL url) {
		this.pagenumber = pageNumber;
		this.url = url;
	}
	
	/**
	 * Instantiates a new simple url image.
	 */
	public SimpleUrlImage () {
		
	}
	
	/**************************************************************************************
	 * Setter for url
	 *
	 * @param url the imageurl to set
	 **************************************************************************************/
	public void setURL(URL imageurl) {
		this.url = imageurl;
	}
	
	/**************************************************************************************
	 * Setter for pdfpagenumber
	 *
	 * @param pagenumber the pdfpagenumber to set
	 **************************************************************************************/
	public void setPageNumber(Integer pagenumber) {
		this.pagenumber = pagenumber;
	}
	

}
