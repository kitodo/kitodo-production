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

public class ConfigMain {
    private static final Logger logger = LogManager.getLogger(ConfigMain.class);
    private static volatile PropertiesConfiguration config;
    private static final String CONFIG_FILE = "kitodo_config.properties";

    /**
     * Get properties from configuration file.
     * 
     * @return PropertiesConfiguration object
     */
    public static PropertiesConfiguration getConfig() {
        if (config == null) {
            synchronized (ConfigMain.class) {
                PropertiesConfiguration initialized = config;
                if (initialized == null) {
                    PropertiesConfiguration.setDefaultListDelimiter('&');
                    try {
                        initialized = new PropertiesConfiguration(CONFIG_FILE);
                    } catch (ConfigurationException e) {
                        logger.warn("Loading of {} failed. Trying to start with empty configuration. Exception: {}",
                                CONFIG_FILE, e);
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

    /**
     * Request selected parameter from configuration.
     *
     * @return Parameter as String
     */
    public static String getParameter(String inParameter) {
        try {
            return getConfig().getString(inParameter);
        } catch (RuntimeException e) {
            logger.error(e);
            return "- keine Konfiguration gefunden -";
        }
    }

    /**
     * Request selected parameter with given default value from configuration.
     *
     * @return Parameter as String
     */
    public static String getParameter(String inParameter, String inDefaultIfNull) {
        try {
            return getConfig().getString(inParameter, inDefaultIfNull);
        } catch (RuntimeException e) {
            return inDefaultIfNull;
        }
    }

    /**
     * Request boolean parameter from configuration, default if missing: false.
     *
     * @return Parameter as String
     */
    public static boolean getBooleanParameter(String inParameter) {
        return getBooleanParameter(inParameter, false);
    }

    /**
     * Request boolean parameter from configuration.
     *
     * @return Parameter as String
     */
    public static boolean getBooleanParameter(String inParameter, boolean inDefault) {
        return getConfig().getBoolean(inParameter, inDefault);
    }

    /**
     * Request int-parameter from Configuration.
     *
     * @return Parameter as Int
     */
    public static int getIntParameter(String inParameter) {
        return getIntParameter(inParameter, 0);
    }

    /**
     * Request int-parameter from Configuration with default-value.
     *
     * @return Parameter as Int
     */
    public static int getIntParameter(String inParameter, int inDefault) {
        try {
            return getConfig().getInt(inParameter, inDefault);
        } catch (Exception e) {
            return 0;
        }
    }
}
