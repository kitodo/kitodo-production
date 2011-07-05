package org.goobi.api.display;
//TODO: Document this.
//TODO: Add licence header

import org.goobi.api.display.enums.BindState;

public class Modes {

	public static BindState myBindState = BindState.create;
	
	
	
	public static BindState getBindState() {
		return myBindState;
	}


	public static void setBindState(BindState inBindState) {
		myBindState = inBindState;
	}
	
}
