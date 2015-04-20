package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / ContentFileNotLinkedException.java
 * 
 * Copyright 2010 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

/*******************************************************************************
 * <p>
 * This exception is thrown, if a <code>ContentFile</code> is not linked to a
 * <code>DocStruct</code> object, but it is assumed, that it is.
 * </p>
 * <p>
 * Example: Exception is thrown when removing a <code>ContentFile</code> from
 * <code>DocStruct</code> instance.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-09-24
 ******************************************************************************/

public class ContentFileNotLinkedException extends UGHException {

	private static final long	serialVersionUID	= -3208509348842876202L;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public ContentFileNotLinkedException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public ContentFileNotLinkedException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public ContentFileNotLinkedException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public ContentFileNotLinkedException(String inReason, Exception e) {
		super(inReason, e);
	}

}
