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

package org.kitodo.config;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    private static final Logger logger = LogManager.getLogger(Config.class);
    private static volatile PropertiesConfiguration config;
    private static final String CONFIG_FILE = "kitodo_config.properties";
    private static final String METADATA_DIRECTORY = "MetadatenVerzeichnis";
    private static final String CONFIG_DIR = "KonfigurationVerzeichnis";

    /**
     * Private constructor to hide the implicit public one.
     */
    private Config() {

    }

    /**
     * Get Kitodo data directory.
     *
     * @return String
     */
    public static String getKitodoDataDirectory() {
        return getParameter(METADATA_DIRECTORY);
    }

    /**
     * Get Kitodo config directory.
     *
     * @return String
     */
    public static String getKitodoConfigDirectory() {
        return getParameter(CONFIG_DIR);
    }

    /**
     * Request selected parameter from configuration.
     *
     * @return Parameter as String
     */
    public static String getParameter(String parameter) {
        try {
            return getConfig().getString(parameter);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return "No configuration found!";
        }
    }

    /**
     * Request boolean parameter from configuration.
     *
     * @return Parameter as String
     */
    public static boolean getBooleanParameter(String parameter, boolean defaultIfNull) {
        return getConfig().getBoolean(parameter, defaultIfNull);
    }

    /**
     * Request selected parameter with given default value from configuration.
     *
     * @return Parameter as String
     */
    public static String getParameter(String parameter, String defaultIfNull) {
        try {
            return getConfig().getString(parameter, defaultIfNull);
        } catch (RuntimeException e) {
            return defaultIfNull;
        }
    }

    /**
     * Gets the configuration.
     *
     * @return the PropertyConfiguration
     */
    private static PropertiesConfiguration getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                PropertiesConfiguration initialized = config;
                if (initialized == null) {
                    PropertiesConfiguration.setDefaultListDelimiter('&');
                    try {
                        initialized = new PropertiesConfiguration(CONFIG_FILE);
                    } catch (ConfigurationException e) {
                        logger.warn(
                                "Loading of " + CONFIG_FILE + " failed. Trying to start with empty configuration.", e);
                        initialized = new PropertiesConfiguration();
                    }
                    initialized.setListDelimiter('&');
                    initialized.setReloadingStrategy(new FileChangedReloadingStrategy());
                    config = initialized;
                }
            }
        }
        return config;
    }
}
