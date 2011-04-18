package org.goobi.production.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigMain;

public class PluginLoader {
	
	private final static boolean useDevelopmentPath = false;
//	private final static String developmentPath = "/Users/steffen/Documents/workspace/SampleStepPlugins/bin/";
	private final static String developmentPath = "/Users/steffen/Documents/workspace/MetsEditor/bin/";

	public static List<IPlugin> getPluginList(PluginType inType) {
		PluginManagerUtil pmu = initialize(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(IPlugin.class);
		return new ArrayList<IPlugin>(plugins);
	}

	public static IPlugin getPlugin(PluginType inType, String inId) {
		PluginManagerUtil pmu = initialize(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(inType.getInterfaz());
		for (IPlugin p : plugins) {
			if (p.getId().equals(inId)) {
				return p;
			}
		}
		return null;
	}

	private static PluginManagerUtil initialize(PluginType inType) {
		PluginManager pm = PluginManagerFactory.createPluginManager();
		String path = ConfigMain.getParameter("pluginFolder") + inType.getName() + "/";
		// switch here to development path for development of special plugin
		if (useDevelopmentPath){
			path = developmentPath;
		}
		pm.addPluginsFrom(new File(path).toURI());
		return new PluginManagerUtil(pm);
	}
}
