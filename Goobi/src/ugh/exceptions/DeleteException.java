package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / DeleteException.java
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
 * @author Markus Enders
 * @version 2009-09-24
 ******************************************************************************/

public class DeleteException extends UGHException {

	private static final long	serialVersionUID	= 5095477783159963783L;

	private int					errorcode;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public DeleteException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public DeleteException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public DeleteException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public DeleteException(String inReason, Exception e) {
		super(inReason, e);
	}

	/***************************************************************************
	 * @param inError
	 * @param inReason
	 **************************************************************************/
	public DeleteException(int inError, String inReason) {
		this.errorcode = inError;
	}

	/**************************************************************************
	 * @return
	 **************************************************************************/
	public int getErrorcode() {
		return this.errorcode;
	}

	/**************************************************************************
	 * @param errorcode
	 **************************************************************************/
	public void setErrorcode(int errorcode) {
		this.errorcode = errorcode;
	}

	/***************************************************************************
	 * @deprecated
	 * @return
	 **************************************************************************/
	@Deprecated
	public int getErrorCode() {
		return this.errorcode;
	}

}
