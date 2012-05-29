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

import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * The Class WrappedImage is a simple wrapper for java.awt.image.RenderedImage instances.
 */
public class WrappedImage implements Image {
	
	/** The page nr. */
	protected Integer pageNr = -1;
	
	/** The image. */
	protected RenderedImage image = null;
	
	/**
	 * Instantiates a new wrapped image.
	 * 
	 * @param pagenr the page number
	 * @param image the RenderedImage
	 */
	public WrappedImage (Integer pagenr, RenderedImage image) {
		this.pageNr = pagenr;
		this.image = image;
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.Image#getPageNumber()
	 */
	public Integer getPageNumber() {
		return pageNr;
	}

	/* (non-Javadoc)
	 * @see de.unigoettingen.sub.commons.util.datasource.Image#getRenderedImage()
	 */
	public RenderedImage getRenderedImage() throws IOException {
		return image;
	}

}
