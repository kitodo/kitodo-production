package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / MetadataTypeNotAllowedException.java
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

import ugh.dl.DocStructType;
import ugh.dl.MetadataType;

/*******************************************************************************
 * @author Markus Enders
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-26
 * @since 2005-03-09
 ******************************************************************************/

public class MetadataTypeNotAllowedException extends UGHException {

	private static final long	serialVersionUID	= -6826313212661918527L;

	private MetadataType		metadataType;
	private DocStructType		docStructType;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public MetadataTypeNotAllowedException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public MetadataTypeNotAllowedException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public MetadataTypeNotAllowedException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public MetadataTypeNotAllowedException(String inReason, Exception e) {
		super(inReason, e);
	}

	/***************************************************************************
	 * @param in
	 * @param inDSType
	 **************************************************************************/
	public MetadataTypeNotAllowedException(MetadataType in,
			DocStructType inDSType) {
		this.metadataType = in;
		this.docStructType = inDSType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {

		String result;

		if (this.metadataType == null && this.docStructType == null && super.getMessage() != null) {
			return super.getMessage();
		}

		if (this.metadataType == null && this.docStructType == null) {
			return "Metadata not allowed! MetadataType and DocStructType unknown";
		}

		if (this.metadataType == null && this.docStructType != null) {
			result = "Metadata not allowed for DocStruct '"
					+ this.docStructType.getName() + "'";
		} else if (this.metadataType != null && this.docStructType == null) {
			result = "Metadata '" + this.metadataType.getName()
					+ "' not allowed for current DocStruct";
		} else {
			result = "Metadata of "
					+ (this.metadataType == null ? "unknown type" : "type '"
							+ this.metadataType.getName() + "'")
					+ " not allowed for DocStruct '"
					+ this.docStructType.getName() + "'";
		}

		return result;
	}

	/***************************************************************************
	 * @return the dst
	 **************************************************************************/
	public DocStructType getDocStructType() {
		return this.docStructType;
	}

	/**************************************************************************
	 * @param docStructType
	 **************************************************************************/
	public void setDocStructType(DocStructType docStructType) {
		this.docStructType = docStructType;
	}

	/***************************************************************************
	 * @return the mdt
	 **************************************************************************/
	public MetadataType getMetadataType() {
		return this.metadataType;
	}

	/**************************************************************************
	 * @param metadataType
	 **************************************************************************/
	public void setMetadataType(MetadataType metadataType) {
		this.metadataType = metadataType;
	}

}
