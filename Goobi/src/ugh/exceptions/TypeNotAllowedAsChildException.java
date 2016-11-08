package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / TypeNotAllowedAsChildException.java
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
 * <p>
 * Exception is thrown, if a child should be added, but the DocStructType of the
 * child is not member of possible child types of a DocStruct.
 * </p>
 * 
 * @author Markus Enders
 * @version 2010-01-22
 * @since 2005-03-09
 ******************************************************************************/

public class TypeNotAllowedAsChildException extends UGHException {

	private static final long	serialVersionUID	= -8326109048661152397L;

	ugh.dl.DocStructType		dsChildType;
	ugh.dl.DocStructType		dsParentType;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public TypeNotAllowedAsChildException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public TypeNotAllowedAsChildException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public TypeNotAllowedAsChildException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public TypeNotAllowedAsChildException(String inReason, Exception e) {
		super(inReason, e);
	}

	/***************************************************************************
	 * @param child
	 **************************************************************************/
	public TypeNotAllowedAsChildException(DocStructType child) {
		this.dsChildType = child;
	}

	/***************************************************************************
	 * @param parent
	 * @param child
	 **************************************************************************/
	public TypeNotAllowedAsChildException(DocStructType parent,
			DocStructType child) {
		this.dsChildType = child;
		this.dsParentType = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		String result = null;

		if (this.dsChildType == null && this.dsParentType != null) {
			result = "DocStructType for child is not defined; probably a mapping problem in the ruleset. Child should be added for parent '"
					+ this.dsParentType.getName() + "'";
		} else if (this.dsParentType == null && this.dsChildType != null) {
			result = "Child of type '"
					+ this.dsChildType.getName()
					+ "' is not allowed for parent; unfortunately we don't have any information about the parent";
		} else {
			result = "Child of "
					+ (this.dsChildType == null ? "unknown type" : "type '"
							+ this.dsChildType.getName() + "'")
					+ " can't be added to a DocStruct of type '"
					+ this.dsParentType.getName() + "'";
		}

		return result;
	}

}
