package org.goobi.api.display.enums;
//TODO: Document this.
//TODO: Add licence header

public enum BindState {
	
	create("0","create"), edit("1","edit"); 
	
	private String id;
	private String title;

	private BindState(String myId, String myTitle) {
		id = myId;
		title = myTitle;
	}

	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static BindState getByTitle(String inTitle){
		for (BindState type : BindState.values()) {
			if (type.getTitle().equals(inTitle)) {
				return type;
			}
		}
		return edit; // edit is default
	}
	
	
	
	
}