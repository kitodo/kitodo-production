package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFileArea.java
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

import java.io.Serializable;

import org.apache.log4j.Logger;

import ugh.exceptions.ContentFileAreaTypeUnknownException;

/*******************************************************************************
 * <p>
 * A ContentFileArea object defines an area inside a ContentFile. Depending on
 * the type of ContentFile, there may be several options to define such an area.
 * <ul>
 * <li>coordinates, e.g. when using images the from field contains the
 * coordinates of the upper left corner of an area, the to field contains the
 * lower right corner of the area.
 * <li>xmlid, if pointing into an XML file, id values (stored in xml-identifier
 * in the Contentfile) can be used to point to a part of a content file.
 * <li>smtpe-codes; time codes pointing to the beginning and end of a streaming
 * media part (as video or audio)
 * <li>
 * </ul>
 * byte-offset: If an area just contains the lowest entity, which can be
 * addressed in the content file (e.g. a single pixel, a single second, a single
 * xml-element), the from and to fields must contain the same values.
 * </p>
 * 
 * @author Markus Enders
 * @version 2010-02-13
 * 
 *          CHANGELOG
 * 
 *          13.02.2010 --- Funk --- Minor changes.
 * 
 *          17.11.2009 --- Funk --- Refactored some things for Sonar
 *          improvement. --- Removed modifier "transient" from LOGGER.
 * 
 ******************************************************************************/

public class ContentFileArea implements Serializable {

	private static final long	serialVersionUID	= 3957147069912977429L;

	private static final Logger	LOGGER				= Logger
															.getLogger(ugh.dl.DigitalDocument.class);

	// Type of area (coordinates, xml id, byteoffset, ...).
	private String				type;
	// From attribute; e.g. can be xml id, SMTPE code etc.
	private String				from;
	// To attribute; same as from.
	private String				to;

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getFrom() {
		return this.from;
	}

	/***************************************************************************
	 * @param from
	 **************************************************************************/
	public void setFrom(String from) {
		this.from = from;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getTo() {
		return this.to;
	}

	/***************************************************************************
	 * @param to
	 **************************************************************************/
	public void setTo(String to) {
		this.to = to;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getType() {
		return this.type;
	}

	/***************************************************************************
	 * <p>
	 * Sets the type of reference. The following types are known:
	 * <ul>
	 * <li>byteoffset</li>
	 * <li>coordinates</li>
	 * <li>smtpe</li>
	 * <li>xmlid Types are case sensitive. If an unknown type is set, an
	 * exception is thrown.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param type
	 *            As a String.
	 * @throws ContentFileAreaTypeUnknownException
	 **************************************************************************/
	public void setType(String type) throws ContentFileAreaTypeUnknownException {

		if (type.equals("coordinates") || type.equals("byteoffset")
				|| type.equals("xmlid") || type.equals("smtpe")) {
			this.type = type;
		} else {
			String message = "'" + type
					+ "' is unknown for ContentFileArea type";
			LOGGER.error(message);
			throw new ContentFileAreaTypeUnknownException(message);
		}
	}

	/***************************************************************************
	 * <p>
	 * Overloaded method compares this ContentFileArea with parameter
	 * contentFileArea.
	 * </p>
	 * 
	 * @author Wulf Riebensahm
	 * @return TRUE if type and value are the same.
	 * @param ContentFileArea
	 *            contentFileArea
	 **************************************************************************/
	public boolean equals(ContentFileArea contentFileArea) {

		// Try block for comparing strings.
		try {
			if (!((this.getType() == null && contentFileArea.getType() == null) || this
					.getType().equals(contentFileArea.getType()))) {
				return false;
			}

			if (!((this.getFrom() == null && contentFileArea.getFrom() == null) || this
					.getFrom().equals(contentFileArea.getFrom()))) {
				return false;
			}

			if (!((this.getTo() == null && contentFileArea.getTo() == null) || this
					.getTo().equals(contentFileArea.getTo()))) {
				return false;
			}

		}
		// TODO Teldemokles says: "Do never catch a NullPointerException"!
		catch (NullPointerException npe) {
			return false;
		}

		return true;
	}

}
