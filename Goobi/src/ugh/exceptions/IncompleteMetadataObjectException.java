package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / IncompleteMetadataObjectException.java
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
 * The IncompleteMetadataObjectException may be thrown, when incomplete
 * <code>Metadata</code> objects are created and used. E.g. some methods are
 * using information about the object type. If this is not available, this
 * exception is thrown.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-09-24
 ******************************************************************************/

public class IncompleteMetadataObjectException extends RuntimeException {

	private static final long	serialVersionUID	= -3017121111006455108L;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public IncompleteMetadataObjectException() {
		super();
	}

	/***************************************************************************
	 * @param inMessage
	 **************************************************************************/
	public IncompleteMetadataObjectException(String inMessage) {
		super(inMessage);
	}

	/***************************************************************************
	 * @param exp
	 **************************************************************************/
	public IncompleteMetadataObjectException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inMessage
	 * @param exp
	 **************************************************************************/
	public IncompleteMetadataObjectException(String inMessage, Exception e) {
		super(inMessage, e);
	}

}
