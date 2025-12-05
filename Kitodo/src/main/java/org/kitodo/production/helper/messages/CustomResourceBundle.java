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

package org.kitodo.production.helper.messages;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.helper.LocaleHelper;

abstract class CustomResourceBundle extends ResourceBundle {

    private static final Logger logger = LogManager.getLogger(CustomResourceBundle.class);
    private static URLClassLoader urlClassLoader;
    private static Map<String, Boolean> propertiesFileExistsMap = new HashMap<String, Boolean>();

    @Override
    public Enumeration<String> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object handleGetObject(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a URLClassLoader that is allowed to load resource bundles from the external directory
     * containing translated messages and errors (e.g. /usr/local/kitodo/messages).

     * @return instance of URLClassLoader
     */
    private static URLClassLoader getURLClassLoader() {
        if (Objects.isNull(urlClassLoader)) {
            File file = new File(ConfigCore.getParameterOrDefaultValue(ParameterCore.DIR_LOCAL_MESSAGES));
            if (file.exists()) {
                try {
                    final URL resourceURL = file.toURI().toURL();
                    urlClassLoader = new URLClassLoader(new URL[] { resourceURL });
                } catch (MalformedURLException e) {
                    logger.info(e.getMessage(), e);
                }
            } else {
                urlClassLoader = null;
            }
        }
        return urlClassLoader;
    }

    /**
     * Checks if properties file for a specfiic external resource bundle does exist.
     * Remembers existence in static map such that filesystem is not checked repeatedly.
     * 
     * <p>This check is required because resource bundles seem to always load if an URLClassLoader
     * could be initialized even if the corresponding properties file in that directory does not 
     * exist. The resulting resource bundle is simply empty then.</p>
     * 
     * <p>This check allows to only load from URLClassLoader, if the corresponding properties file
     * actually exists.</p>
     * 
     * @param bundleName the bundle name
     * @param locale the locale
     * @return true if properties file for resource bundle exists, else false
     */
    private static Boolean externalResourceBundleFileExists(String bundleName, Locale locale) {
        String key = bundleName + "_" + locale.getLanguage();
        if (!propertiesFileExistsMap.containsKey(key)) {
            String directory = ConfigCore.getParameterOrDefaultValue(ParameterCore.DIR_LOCAL_MESSAGES);
            Path path = Paths.get(directory, bundleName + "_" + locale.getLanguage() + ".properties");
            File file = path.toFile();
            
            propertiesFileExistsMap.put(key, file.exists());

            if (!file.exists()) {
                logger.info("Could not find external resource bundle '" + bundleName + "' at " + file);
            }
        }
        return propertiesFileExistsMap.get(key);
    }

    /**
     * Loads an external resource bundle (outside jar files) if a corresponding properties file 
     * exists and an URLClassLoader could be build that has the permissions to load files from 
     * that directory.
     * 
     * @param bundleName the bundle name
     * @param locale the locale
     * @return the external resource bundle or null if it does not exist
     */
    private static ResourceBundle getExternalResourceBundle(String bundleName, Locale locale) {
        if (!externalResourceBundleFileExists(bundleName, locale)) {
            return null;
        }
        URLClassLoader urlLoader = getURLClassLoader();
        if (Objects.nonNull(urlLoader)) {
            try {
                return ResourceBundle.getBundle(bundleName, locale, urlLoader);
            } catch (MissingResourceException e) {
                logger.error("Could not load external resource bundle '" + bundleName + "': " + e.getMessage());
            }
        }
        return null;
    }

    private static ResourceBundle getExternalResourceBundle(String bundleName) {
        Locale locale = LocaleHelper.getCurrentLocale();
        return getExternalResourceBundle(bundleName, locale);
    }

    /**
     * Get resource bundle. In case there is a custom version of translation
     * files load them, if not load the default ones.
     * 
     * @param defaultBundleName
     *            name of the default translation bundle
     * @param customBundleName
     *            name of the custom translation bundle
     * @param locale
     *            for which translation bundle should be created
     * @return available translation bundle
     */
    public static ResourceBundle getResourceBundle(String defaultBundleName, String customBundleName, Locale locale) {
        ResourceBundle bundle = getExternalResourceBundle(customBundleName, locale);
        if (Objects.nonNull(bundle)) {
            return bundle;
        }
        return ResourceBundle.getBundle(defaultBundleName, locale);
    }

    /**
     * Loads default resource bundle (from inside jar files).
     * @param bundleName the bundle name
     * @return the resource bundle
     */
    protected ResourceBundle getBaseResources(String bundleName) {
        return ResourceBundle.getBundle(bundleName, LocaleHelper.getCurrentLocale());
    }

    /**
     * Loads value from external resource bundles (outside jar files).
     * @param key the key of the resource
     * @param bundleName the bundle name
     * @return the value or null if not exists
     */
    protected Object getValueFromExternalResourceBundle(String key, String bundleName) {
        ResourceBundle bundle = getExternalResourceBundle(bundleName);
        if (Objects.nonNull(bundle)) {
            return bundle.getObject(key);
        }
        return null;
    }

}
