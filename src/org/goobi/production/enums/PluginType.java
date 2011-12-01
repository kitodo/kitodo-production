package org.goobi.production.enums;

public enum PluginType {

	Import(1, "import"), Step(2, "step");
	
	private int id;
	private String name;
	
	private PluginType(int id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	
	public static PluginType getTypeFromValue(String pluginType) {
		if (pluginType != null) {
			for (PluginType type : PluginType.values()) {
				if (type.getName().equals(pluginType))
					return type;
			}
		}
		return null;
	}
	
	public static PluginType getTypesFromId(int pluginType) {
		for (PluginType type : PluginType.values()) {
			if (type.getId()== pluginType) {
				return type;
			}
		}
		return null;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
}
