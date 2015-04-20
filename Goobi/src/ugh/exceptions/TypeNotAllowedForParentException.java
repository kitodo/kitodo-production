package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / TypeNotAllowedForParentException.java
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

import ugh.dl.DocStructType;

/*******************************************************************************
 * @author Markus Enders
 * @version 2009-09-24
 * @since 2004-05-21
 ******************************************************************************/

public class TypeNotAllowedForParentException extends UGHException {

	private static final long	serialVersionUID	= -4288773248662868878L;

	ugh.dl.DocStructType		docStructType;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public TypeNotAllowedForParentException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public TypeNotAllowedForParentException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public TypeNotAllowedForParentException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public TypeNotAllowedForParentException(String inReason, Exception e) {
		super(inReason, e);
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	public TypeNotAllowedForParentException(DocStructType in) {
		this.docStructType = in;
	}

	/**************************************************************************
	 * @return
	 **************************************************************************/
	public ugh.dl.DocStructType getDocStructType() {
		return this.docStructType;
	}

	/**************************************************************************
	 * @param docStructType
	 **************************************************************************/
	public void setDocStructType(ugh.dl.DocStructType docStructType) {
		this.docStructType = docStructType;
	}

}
