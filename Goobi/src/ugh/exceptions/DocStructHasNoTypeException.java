package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / DocStructHasNoTypeException.java
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
 * Exception is thrown, if metadata should be added, but the DocStruct has no
 * DocStructType.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-09-24
 * @since 2005-03-09
 ******************************************************************************/

public class DocStructHasNoTypeException extends RuntimeException {

	private static final long	serialVersionUID	= -1950082217522786783L;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public DocStructHasNoTypeException() {
		super();
	}

	/***************************************************************************
	 * @param inMessage
	 **************************************************************************/
	public DocStructHasNoTypeException(String inMessage) {
		super(inMessage);
	}

	/***************************************************************************
	 * @param exp
	 **************************************************************************/
	public DocStructHasNoTypeException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inMessage
	 * @param exp
	 **************************************************************************/
	public DocStructHasNoTypeException(String inMessage, Exception e) {
		super(inMessage, e);
	}

}
