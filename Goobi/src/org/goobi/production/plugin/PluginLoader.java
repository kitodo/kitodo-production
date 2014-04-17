package org.goobi.production.plugin;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.goobi.production.constants.Parameters;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigMain;

/**
 * The class PluginLoader provides for the loading of plug-ins at runtime.
 * 
 * @author unknown
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class PluginLoader {
	private static final Logger logger = Logger.getLogger(PluginLoader.class);

	public static CataloguePlugin getCataloguePluginForCatalogue(String catalogue) {
		CataloguePlugin result = null;
		for (CataloguePlugin plugin : PluginLoader.getPlugins(CataloguePlugin.class))
			if (plugin.supportsCatalogue(catalogue)) {
				result = plugin;
				break;
			}
		return result;
	}

	/**
	 * The function getPluginList() loads the plugins of the given PluginType
	 * and returns them as an array list.
	 * 
	 * @param inType
	 * @return
	 */
	@Deprecated
	public static List<IPlugin> getPluginList(PluginType inType) {
		PluginManagerUtil pmu = getPluginLoader(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(IPlugin.class);
		return new ArrayList<IPlugin>(plugins);
	}

	@Deprecated
	public static IPlugin getPluginByTitle(PluginType inType, String inTitle) {
		PluginManagerUtil pmu = getPluginLoader(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(inType.getInterfaz());
		for (IPlugin p : plugins) {
			if (p.getTitle().equals(inTitle)) {
				return p;
			}
		}
		return null;
	}

	public static <T extends UnspecificPlugin> T getPluginByTitle(Class<T> clazz, String title) {
		for (T plugin : getPlugins(clazz))
			if (plugin.getTitle().equals(title))
				return plugin;
		return null;
	}

	@Deprecated
	public static IPlugin getPlugin(PluginType inType, String inTitle) {
		PluginManagerUtil pmu = getPluginLoader(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(inType.getInterfaz());
		for (IPlugin p : plugins) {
			if (p.getTitle().equals(inTitle)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * The function getPluginConfiguration() creates a HashMap that is passed to
	 * the plugins upon creation to configure them. This is to use for
	 * <em>general</em> configuration (probably) suitable for all plugins. Use
	 * setters on the plugins to set specific settings.
	 * 
	 * @return a HashMap to configure the plugins
	 */
	private static HashMap<String, String> getPluginConfiguration() {
		HashMap<String, String> conf = new HashMap<String, String>(3);
		conf.put("configDir", ConfigMain.getParameter(Parameters.CONFIG_DIR));
		conf.put("tempDir", ConfigMain.getParameter(Parameters.PLUGIN_TEMP_DIR));
		return conf;
	}

	@SuppressWarnings("unchecked")
	public static <T extends UnspecificPlugin> Collection<T> getPlugins(Class<T> clazz) {
		PluginType type = UnspecificPlugin.typeOf(clazz);
		PluginManagerUtil pluginLoader = getPluginLoader(type);
		Collection<Plugin> plugins = pluginLoader.getPlugins(Plugin.class); // Never API version supports no-arg getPlugins() TODO: update API
		ArrayList<T> result = new ArrayList<T>(plugins.size());
		for (Plugin implementation : plugins) {
			try {
				T plugin = (T) UnspecificPlugin.create(type, implementation);
				plugin.configure(getPluginConfiguration());
				result.add(plugin);
			} catch (NoSuchMethodException e) {
				logger.warn("Bad implementation of " + type.getName() + " plugin "
						+ implementation.getClass().getName(), e);
			} catch (SecurityException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return result;
	}

	public static List<String> getImportPluginsForType(ImportType type) {
		List<String> pluginList = new ArrayList<String>();

		for (IPlugin p : PluginLoader.getPluginList(PluginType.Import)) {
			IImportPlugin ip = (IImportPlugin) p;
			if (ip.getImportTypes().contains(type)) {
				pluginList.add(p.getTitle());
			}
		}
		return pluginList;
	}

	/**
	 * The function getPluginLoader() returns a PluginManagerUtil suitable for
	 * loading plugins from the subdirectory defined by the given PluginType
	 * 
	 * @param type
	 *            plugin type specifying the plugin subdirectory to scan
	 * @return a PluginManagerUtil to load plugins from that directory
	 */
	private static PluginManagerUtil getPluginLoader(PluginType type) {
		PluginManager pluginManager = PluginManagerFactory.createPluginManager();
		File path = new File(FilenameUtils.concat(ConfigMain.getParameter(Parameters.PLUGIN_FOLDER), type.getName()));
		pluginManager.addPluginsFrom(path.toURI());
		return new PluginManagerUtil(pluginManager);
	}
}
