package ugh.exceptions;

/*******************************************************************************
 * ugh.exceptions / MissingModsMappingException.java
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

import java.util.List;

import ugh.dl.DocStructType;
import ugh.dl.Metadata;

/*******************************************************************************
 * @author Stefan E. Funk
 * @version 2010-01-22
 * @since 2009-06-29
 ******************************************************************************/

public class MissingModsMappingException extends UGHException {

	private static final long	serialVersionUID	= -8257927032753509817L;

	DocStructType				docStructType;
	List<Metadata>				metadataTypes;

	/***************************************************************************
	 * Default constructor.
	 **************************************************************************/
	public MissingModsMappingException() {
		super();
	}

	/***************************************************************************
	 * @param inReason
	 **************************************************************************/
	public MissingModsMappingException(String inReason) {
		super(inReason);
	}

	/***************************************************************************
	 * @param e
	 **************************************************************************/
	public MissingModsMappingException(Exception e) {
		super(e);
	}

	/***************************************************************************
	 * @param inReason
	 * @param e
	 **************************************************************************/
	public MissingModsMappingException(String inReason, Exception e) {
		super(inReason, e);
	}

	/***************************************************************************
	 * @param in
	 * @param inDSType
	 **************************************************************************/
	public MissingModsMappingException(DocStructType inDSType,
			List<Metadata> inMDList) {
		this.docStructType = inDSType;
		this.metadataTypes = inMDList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {

		if (this.docStructType != null && !this.metadataTypes.isEmpty()) {
			StringBuffer listEntries = new StringBuffer();
			for (Metadata m : this.metadataTypes) {
				if (m.getValue() != null && !m.getValue().equals("")) {
					listEntries.append("[" + m.getType().getName() + ":'"
							+ m.getValue() + "'] ");
				} else {
					listEntries.append("[" + m.getType().getName() + "] ");
				}
			}

			return "The following metadata types for DocStruct '"
					+ this.docStructType.getName()
					+ "' are NOT YET mapped to the MODS: "
					+ listEntries.toString().trim();
		}

		return "Error occurred for unknown reason";
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

	/**************************************************************************
	 * @return
	 **************************************************************************/
	public List<Metadata> getMetadataTypes() {
		return this.metadataTypes;
	}

	/**************************************************************************
	 * @param metadataType
	 **************************************************************************/
	public void setMetadataTypes(List<Metadata> metadataTypes) {
		this.metadataTypes = metadataTypes;
	}

}
