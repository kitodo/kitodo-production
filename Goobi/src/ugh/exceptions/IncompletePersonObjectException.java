package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / IncompletePersonObjectException.java
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
 * This exception is thrown, when you are dealing with an incomplete
 * <code>Person</code> object. E.g. each object of this kind must have a
 * MetadataType. If this is NOT available, this exception may be thrown in some
 * methods, which need the type information.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-09-24
 ******************************************************************************/

public class IncompletePersonObjectException extends RuntimeException {

	private static final long	serialVersionUID	= 3080339507475098186L;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public IncompletePersonObjectException() {
		super();
	}

	/***************************************************************************
	 * @param inMessage
	 **************************************************************************/
	public IncompletePersonObjectException(String inMessage) {
		super(inMessage);
	}

	/***************************************************************************
	 * @param exp
	 **************************************************************************/
	public IncompletePersonObjectException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inMessage
	 * @param exp
	 **************************************************************************/
	public IncompletePersonObjectException(String inMessage, Exception e) {
		super(inMessage, e);
	}

}
