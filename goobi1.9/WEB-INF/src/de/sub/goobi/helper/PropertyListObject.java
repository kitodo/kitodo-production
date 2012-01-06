package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.properties.ProcessProperty;

public class PropertyListObject {

	private List<ProcessProperty> propertyList = new ArrayList<ProcessProperty>();
	private int containerNumber = 0;

	public PropertyListObject(int container) {
		this.containerNumber = container;
	}

	public void addToList(ProcessProperty pp) {
		this.propertyList.add(pp);
	}

	public int getContainerNumber() {
		return this.containerNumber;
	}

	public List<ProcessProperty> getPropertyList() {
		return this.propertyList;
	}

	public int getPropertyListSize() {
		return this.propertyList.size();
	}
	
	public String getPropertyListSizeString() {
		return ""+this.propertyList.size();
	}
}
