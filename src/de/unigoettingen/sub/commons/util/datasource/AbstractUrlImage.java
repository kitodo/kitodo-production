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
import java.io.InputStream;
import java.net.URL;

public abstract class AbstractUrlImage implements UrlImage {

	protected URL url;
	protected Integer pagenumber;

	/*************************************************************************************
	 * Getter for pagenumber
	 *
	 * @return the pagenumber
	 *************************************************************************************/
	public Integer getPageNumber() {
		return pagenumber;
	}

	/*************************************************************************************
	 * Getter for url
	 *
	 * @return the url
	 *************************************************************************************/
	public URL getURL() {
		return url;
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
	 * Setter for pagenumber
	 *
	 * @param pagenumber the pagenumber to set
	 **************************************************************************************/
	public void setPageNumber(Integer pagenumber) {
		this.pagenumber = pagenumber;
	}
	
	/* (non-Javadoc)
	 * @see de.unigoettingen.commons.util.datasource.Image#openStream()
	 */
	public InputStream openStream() throws IOException {
		if (url == null) {
			throw new IllegalStateException("URL is null");
		}
		return url.openStream();
	}
	
	/**
	 * Get rendered image. (Throws a UnsupportedOperationException)
	 * 
	 * @see de.unigoettingen.sub.commons.util.datasource.Image#getRenderedImage()
	 * 
	 * @return the rendered image
	 */
	public RenderedImage getRenderedImage () {
		throw new UnsupportedOperationException("Method getRenderedImage() not implemented in AbstractUrlImage!");
	}

}
