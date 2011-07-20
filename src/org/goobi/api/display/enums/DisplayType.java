package org.goobi.api.display.enums;
//TODO: Document this.
//TODO: Add licence header

public enum DisplayType {

	
	input("0","input"),select("1","select"),select1("2","select1"),textarea("3","textarea"); 
	
	private String id;
	private String title;

	private DisplayType(String myId, String myTitle) {
		id = myId;
		title = myTitle;
	}

	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static DisplayType getByTitle(String inTitle){
		if (inTitle != null) {
			for (DisplayType type : DisplayType.values()) {
				if (type.getTitle().equals(inTitle)) {
					return type;
				}
			}
		} 
		return textarea; // textarea is default
	}
}
