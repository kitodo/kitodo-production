package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / UGHException.java
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
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/*******************************************************************************
 * <p>
 * Base class for all UGH Exceptions This should make it easier to capture all
 * EExceptions thrown by the UGH library
 * </p>
 * 
 * @author cmahnke
 * @version 2009-09-24
 ******************************************************************************/

public class UGHException extends Exception {

	private static final long	serialVersionUID	= -2321019939574764131L;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public UGHException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public UGHException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public UGHException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public UGHException(String inReason, Exception e) {
		super(inReason, e);
	}

}
