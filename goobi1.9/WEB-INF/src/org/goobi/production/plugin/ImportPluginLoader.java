package org.goobi.production.plugin;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

public class ImportPluginLoader extends PluginLoader {

	public List<String> getPluginsForType(ImportType type) {
		List<String> pluginList = new ArrayList<String>();
		
		for (IPlugin p : PluginLoader.getPluginList(PluginType.Import)) {
			IImportPlugin ip = (IImportPlugin) p;
			if (ip.getImportTypes().contains(type)) {
				pluginList.add(p.getTitle());
			}
		}
		return pluginList;
	}
	
}
