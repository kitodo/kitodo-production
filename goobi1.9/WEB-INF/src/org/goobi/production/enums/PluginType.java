package org.goobi.production.enums;

import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

public enum PluginType {

	Import(1, "import", IImportPlugin.class), Step(2, "step", IStepPlugin.class), Validator(2, "validate", IValidatorPlugin.class);
	
	private int id;
	private String name;
	private Class<IPlugin> interfaz;
	
	@SuppressWarnings("unchecked")
	private PluginType(int id, String name, Class<? extends IPlugin> inInterfaz) {
		this.id =id;
		this.name = name;
		interfaz = (Class<IPlugin>) inInterfaz;
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

	public int getId() {
		return id;
	}
	
	public Class<IPlugin> getInterfaz() {
		return interfaz;
	}

	public String getName() {
		return name;
	}
	
}
