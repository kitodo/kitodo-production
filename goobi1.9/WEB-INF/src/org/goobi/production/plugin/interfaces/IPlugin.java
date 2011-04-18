package org.goobi.production.plugin.interfaces;

import net.xeoh.plugins.base.Plugin;

import org.goobi.production.enums.PluginType;

public interface IPlugin extends Plugin {
	
	public String getId();
	public PluginType getType();
	public String getTitle();
	public String getDescription();
	
}
