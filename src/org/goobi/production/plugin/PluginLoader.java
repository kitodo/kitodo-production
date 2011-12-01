package org.goobi.production.plugin;

import java.io.File;
import java.util.Collection;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigMain;



public class PluginLoader {

	private PluginType type;
	private String id;
	
	public PluginLoader(PluginType t, String id) {
		this.type = t;
		this.id = id;
	}
	
	public IPlugin getPlugin() {
		PluginManager pm = PluginManagerFactory.createPluginManager();
		pm.addPluginsFrom(new File(ConfigMain.getParameter("pluginFolder")).toURI());
		PluginManagerUtil pmu = new PluginManagerUtil(pm);
		Collection<IPlugin> plugins = pmu.getPlugins(IPlugin.class);
		for (IPlugin p : plugins) {
			if (p.getType().equals(type) && p.getId().equals(id)) {
				return p;
			}	
		}
		return null;
	}
	
}
