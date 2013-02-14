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

package org.goobi.api.display;
public class Item {
	private String myLabel;
	private String myValue;
	private Boolean isSelected;
	
	/**
	 * Creates a new item with given params
	 * 
	 * @param label label of the item
	 * @param value value of the item
	 * @param selected indicates wether an item is preselected or not
	 */
	
	public Item(String label, String value, Boolean selected ){
		setLabel(label);
		setValue(value);
		setIsSelected(selected);
		
	}
	
	/**
	 * 
	 * @param myLabel sets label for the item
	 */

	public void setLabel(String myLabel) {
		this.myLabel = myLabel;
	}

	/**
	 * 
	 * @return label of the item
	 */
	public String getLabel() {
		return myLabel;
	}

	/**
	 * 
	 * @param myValue sets value for the item
	 */
	
	public void setValue(String myValue) {
		this.myValue = myValue;
	}

	/**
	 * 
	 * @return value of the item
	 */
	public String getValue() {
		return myValue;
	}

	/**
	 * 
	 * @param isSelected sets Boolean that indicates whether item is preselected or not
	 */
	
	public void setIsSelected(Boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 * 
	 * @return Boolean: is preselected or not
	 */
	public Boolean getIsSelected() {
		return isSelected;
	}
	
}
