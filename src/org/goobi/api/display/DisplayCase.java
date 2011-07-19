package org.goobi.api.display;
//TODO: Document this a bit more.
//TODO: Add licence header

import java.util.ArrayList;
import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDispayRules;

import de.sub.goobi.Beans.Prozess;


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
