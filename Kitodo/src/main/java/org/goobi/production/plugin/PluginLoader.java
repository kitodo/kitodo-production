/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package org.goobi.production.plugin;

import de.sub.goobi.config.ConfigMain;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goobi.production.constants.Parameters;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.kitodo.production.plugin.importer.massimport.PicaMassImport;

import java.io.File;
import java.util.*;

/**
 * The class PluginLoader provides for the loading of plug-ins at runtime.
 * 
 * @author Based on preceding works from authors not named
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class PluginLoader {
	private static final Logger logger = Logger.getLogger(PluginLoader.class);

	/**
	 * The function getCataloguePluginForCatalogue() returns a redirection class
	 * to handle the first plug-in implementation object that positively
	 * responds to <code>supportsCatalogue(catalogue)</code>.
	 * 
	 * @param catalogue
	 *            catalogue in question
	 * @return the first plug-in that supports the given catalogue
	 */
	public static CataloguePlugin getCataloguePluginForCatalogue(String catalogue) {
		for (CataloguePlugin plugin : PluginLoader.getPlugins(CataloguePlugin.class))
			if (plugin.supportsCatalogue(catalogue))
				return plugin;
		return null;
	}

	/**
	 * @deprecated Using this function is discouraged. Use
	 *             <code>getPlugins(Class)</code> instead.
	 */
	@Deprecated
	public static List<IPlugin> getPluginList(PluginType inType) {
		PluginManagerUtil pmu = getPluginLoader(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(IPlugin.class);
		PicaMassImport pmi = new PicaMassImport();
		plugins.add(pmi);
		return new ArrayList<IPlugin>(plugins);
	}

	/**
	 * @deprecated Using this function is discouraged. Use
	 *             <code>getPluginByTitle(Class, String, Locale)</code> instead.
	 */
	@Deprecated
	public static IPlugin getPluginByTitle(PluginType inType, String inTitle) {
		PluginManagerUtil pmu = getPluginLoader(inType);
		Collection<IPlugin> plugins = pmu.getPlugins(inType.getInterfaz());
		PicaMassImport pmi = new PicaMassImport();
		plugins.add(pmi);
		for (IPlugin p : plugins) {
			if (p.getTitle().equals(inTitle)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * The function getPluginByTitle() loads all plug-ins implementing the given
	 * UnspecificPlugin class type and returns a redirection class to handle the
	 * first plug-in implementation object that responds to
	 * <code>getTitle(language)</code> with the given title.
	 * 
	 * <p>
	 * Currently, this method is not referenced from within the Production code,
	 * but this may change in future. The function is provided to show how the
	 * old plug-in API can be replaced in future.
	 * </p>
	 * 
	 * @param clazz
	 *            UnspecificPlugin class type of the plug-ins to load
	 * @param title
	 *            title the plug-in implementation object shall respond with
	 * @param language
	 *            language the title is in (may be null)
	 * @return the first plug-in that responds with the given title
	 */
	public static <T extends UnspecificPlugin> T getPluginByTitle(Class<T> clazz, String title, Locale language) {
		for (T plugin : getPlugins(clazz))
			if (plugin.getTitle(language).equals(title))
				return plugin;
		return null;
	}

	@Deprecated
	public static IPlugin getPlugin(PluginType inType, String inTitle) {
		return getPluginByTitle(inType, inTitle);
	}

	/**
	 * The function getPluginConfiguration() creates a HashMap that is passed to
	 * the plug-ins upon creation to configure them. The plug-ins may or may not
	 * make use of the configuration provided.
	 * 
	 * <p>
	 * This is intended to be used for <em>general</em> configuration (probably)
	 * suitable for all plug-in types. Use setters on the plug-ins to set
	 * type-specific settings.
	 * </p>
	 * 
	 * @return a HashMap to configure the plug-ins
	 */
	private static HashMap<String, String> getPluginConfiguration() {
		short ENRIES = 2;
		HashMap<String, String> conf = new HashMap<String, String>((int) Math.ceil(ENRIES / 0.75));
		conf.put("configDir", ConfigMain.getParameter(Parameters.CONFIG_DIR));
		conf.put("tempDir", ConfigMain.getParameter(Parameters.PLUGIN_TEMP_DIR));
		return conf;
	}

	/**
	 * The function getPlugins() loads all plug-ins implementing the given
	 * UnspecificPlugin class type and returns a Collection of redirection
	 * classes, each to handle one plug-in implementation object.
	 * 
	 * @param clazz
	 *            UnspecificPlugin class type of the plug-ins to load
	 * @return a Collection of plug-in redirection classes
	 */
	@SuppressWarnings("unchecked")
	public static <T extends UnspecificPlugin> Collection<T> getPlugins(Class<T> clazz) {
		final String INTERNAL_CLASSES_PREFIX = "net.xeoh.plugins.";
		final short INTERNAL_CLASSES_COUNT = 4;
		ArrayList<T> result;

		PluginType type = UnspecificPlugin.typeOf(clazz);
		PluginManagerUtil pluginLoader = getPluginLoader(type);
		Collection<Plugin> plugins = pluginLoader.getPlugins(Plugin.class); // Never API version supports no-arg getPlugins() TODO: update API
		result = new ArrayList<T>(plugins.size() - INTERNAL_CLASSES_COUNT);
		for (Plugin implementation : plugins) {
			if (implementation.getClass().getName().startsWith(INTERNAL_CLASSES_PREFIX))
				continue; // Skip plugin API internal classes
			try {
				T plugin = (T) UnspecificPlugin.create(type, implementation);
				plugin.configure(getPluginConfiguration());
				result.add(plugin);
			} catch (NoSuchMethodException e) {
				if (logger.isEnabledFor(Level.WARN)) {
					logger.warn("Bad implementation of " + type.getName() + " plugin "
							+ implementation.getClass().getName(), e);
				}
			} catch (SecurityException e) {
				if (logger.isEnabledFor(Level.WARN)) {
					logger.warn("Bad implementation of " + type.getName() + " plugin "
							+ implementation.getClass().getName(), e);
				}
			}
		}
		return result;
	}

	/**
	 * The function getImportPluginsForType() returns a list of titles of import
	 * plug-ins matching the given ImportType.
	 * 
	 * @param type
	 *            ImportType of plug-ins to look for
	 * @return a list of titles of import plug-ins matching
	 */
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
	 * loading plug-ins from the subdirectory defined by the given PluginType
	 * 
	 * @param type
	 *            plug-in type specifying the plug-in subdirectory to scan
	 * @return a PluginManagerUtil to load plug-ins from that directory
	 */
	private static PluginManagerUtil getPluginLoader(PluginType type) {
		PluginManager pluginManager = PluginManagerFactory.createPluginManager();
		String path = FilenameUtils.concat(ConfigMain.getParameter(Parameters.PLUGIN_FOLDER), type.getName());
		pluginManager.addPluginsFrom(new File(path).toURI());
		return new PluginManagerUtil(pluginManager);
	}
}
