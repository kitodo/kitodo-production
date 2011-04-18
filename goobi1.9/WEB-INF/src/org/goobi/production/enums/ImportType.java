package org.goobi.production.enums;

public enum ImportType {
	
	
	
	Record("1","record"), ID("2", "id"), FILE("3","file");
	
	private String id;
	private String title;
	
	private ImportType(String id, String title) {
		this.id = id;
		this.title = title;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
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
	
	public static ImportType getByTitle(String title) {
		for (ImportType t : ImportType.values()) {
			if (t.getTitle().equals(title)) {
				return t;
			}
		}		
		return null;
	}
	

}
