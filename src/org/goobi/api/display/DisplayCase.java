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

import java.util.ArrayList;

import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDispayRules;

import de.sub.goobi.beans.Prozess;


public class DisplayCase {
	private DisplayType displayType = null;
	private ArrayList<Item> itemList = new ArrayList<Item>();
	private ConfigDispayRules configDisplay;
	private Prozess myProcess;
	private String metaName;
	private BindState myBindState;
	
	/**
	 * gets items with current bind state
	 * 
	 * @param inProcess
	 * @param metaType
	 */
	
	public DisplayCase(Prozess inProcess, String metaType ){
		metaName = metaType;
		myProcess = inProcess;
		myBindState = Modes.getBindState();
		try {
			configDisplay = ConfigDispayRules.getInstance();
			if (configDisplay != null) {
			displayType = configDisplay.getElementTypeByName(myProcess.getProjekt().getTitel(), myBindState.getTitle(), metaName);
			itemList = configDisplay.getItemsByNameAndType(myProcess.getProjekt().getTitel(), myBindState.getTitle(), metaName, displayType);
			} else {
				// no ruleset file
				displayType = DisplayType.getByTitle("textarea");
				itemList.add(new Item(metaName, "", false));
			}
		} catch (Exception e) {
			// incorrect ruleset file
			displayType = DisplayType.getByTitle("textarea");
			itemList.add(new Item(metaName, "", false));
		}
			
	}

	/**
	 * gets items with given bind state
	 * 
	 * @param inProcess
	 * @param bind
	 * @param metaType
	 */
	
	public DisplayCase(Prozess inProcess, String bind, String metaType ){
		metaName = metaType;
		myProcess = inProcess;
		myBindState = Modes.getBindState();
		try {
			configDisplay = ConfigDispayRules.getInstance();
			if (configDisplay != null) {
				displayType = configDisplay.getElementTypeByName(myProcess.getProjekt().getTitel(), bind, metaName);
				itemList = configDisplay.getItemsByNameAndType(myProcess.getProjekt().getTitel(), bind, metaName, displayType);
			} else {
				// no ruleset file
				displayType = DisplayType.getByTitle("textarea");
				itemList.add(new Item(metaName, "", false));
			}
		} catch (Exception e) {
			// incorrect ruleset file
			displayType = DisplayType.getByTitle("textarea");
			itemList.add(new Item(metaName, "", false));
		}
		
	}
	
	/**
	 * 
	 * @return current DisplayType
	 */
	
	public DisplayType getDisplayType() {
		return displayType;
	}

	/**
	 * 
	 * @param itemList ArrayList with items for metadatum
	 */
	
	public void setItemList(ArrayList<Item> itemList) {
		this.itemList = itemList;
	}

	/**
	 * 
	 * @return ArrayList with items for metadatum
	 */

	public ArrayList<Item> getItemList() {
		return itemList;
	}	
}
