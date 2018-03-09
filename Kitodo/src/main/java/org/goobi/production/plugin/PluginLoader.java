/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.plugin;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.constants.Parameters;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.CataloguePlugin.CataloguePlugin;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.kitodo.production.plugin.importer.massimport.PicaMassImport;

/**
 * The class PluginLoader provides for the loading of plug-ins at runtime.
 * 
 * @author Based on preceding works from authors not named
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class PluginLoader {
    private static final Logger logger = LogManager.getLogger(PluginLoader.class);

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
        for (CataloguePlugin plugin : PluginLoader.getPlugins(CataloguePlugin.class)) {
            if (plugin.supportsCatalogue(catalogue)) {
                return plugin;
            }
        }
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
        return new ArrayList<>(plugins);
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
        for (T plugin : getPlugins(clazz)) {
            if (plugin.getTitle(language).equals(title)) {
                return plugin;
            }
        }
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
        HashMap<String, String> conf = new HashMap<>((int) Math.ceil(ENRIES / 0.75));
        conf.put("configDir", ConfigCore.getKitodoConfigDirectory());
        conf.put("tempDir", ConfigCore.getParameter(Parameters.PLUGIN_TEMP_DIR));
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
        ArrayList<T> result = new ArrayList<>();

        PluginType type = UnspecificPlugin.typeOf(clazz);
        if (Objects.nonNull(type)) {
            PluginManagerUtil pluginLoader = getPluginLoader(type);
            Collection<Plugin> plugins = pluginLoader.getPlugins(Plugin.class);
            // Never API version supports no-arg getPlugins() TODO: update API
            result = new ArrayList<>(plugins.size() - INTERNAL_CLASSES_COUNT);
            for (Plugin implementation : plugins) {
                if (implementation.getClass().getName().startsWith(INTERNAL_CLASSES_PREFIX)) {
                    continue; // Skip plugin API internal classes
                }
                try {
                    T plugin = (T) UnspecificPlugin.create(type, implementation);
                    plugin.configure(getPluginConfiguration());
                    result.add(plugin);
                } catch (NoSuchMethodException | SecurityException e) {
                    logger.warn("Bad implementation of {} plugin {}. Exception: {}",
                            type.getName(), implementation.getClass().getName(), e);
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
        List<String> pluginList = new ArrayList<>();

        for (IPlugin p : PluginLoader.getPluginList(PluginType.IMPORT)) {
            IImportPlugin ip = (IImportPlugin) p;
            if (ip.getImportTypes().contains(type)) {
                pluginList.add(p.getTitle());
            }
        }
        return pluginList;
    }

    /**
     * The function getPluginLoader() returns a PluginManagerUtil suitable for
     * loading plug-ins from the subdirectory defined by the given PluginType.
     *
     * @param type
     *            plug-in type specifying the plug-in subdirectory to scan
     * @return a PluginManagerUtil to load plug-ins from that directory
     */
    private static PluginManagerUtil getPluginLoader(PluginType type) {
        PluginManager pluginManager = PluginManagerFactory.createPluginManager();
        String path = FilenameUtils.concat(ConfigCore.getParameter(Parameters.PLUGIN_FOLDER), type.getName());
        pluginManager.addPluginsFrom(new File(path).toURI());
        return new PluginManagerUtil(pluginManager);
    }
}
