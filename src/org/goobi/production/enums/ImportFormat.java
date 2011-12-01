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
