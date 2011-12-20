/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.enums;


public enum ImportFormat {

	
	
	PICA("1", "pica"), 
	MARC21("2", "marc21"), 
	MARCXML ("3", "marcxml");
	
	
	private String value;
	private String title;
	
	private ImportFormat(String inValue, String inTitle) {
		setValue(inValue);
		setTitle(inTitle);
	}
	
	
	
	
	public static ImportFormat getTypeFromValue(String editType) {
		if (editType != null) {
			for (ImportFormat ss : values()) {
				if (ss.getValue().equals(editType))
					return ss;
			}
		}
		return PICA;
	}


	public static ImportFormat getTypeFromTitle(String editType) {
		if (editType != null) {
			for (ImportFormat ss : values()) {
				if (ss.getTitle().equals(editType))
					return ss;
			}
		}
		return PICA;
	}
	


	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}




	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}




	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}




	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
}
