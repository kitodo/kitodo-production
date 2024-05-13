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

import java.util.NoSuchElementException;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigMain {
    private static final Logger logger = LogManager.getLogger(ConfigMain.class);
    private static volatile PropertiesConfiguration config;
    private static final String CONFIG_FILE = "kitodo_config.properties";

    /**
     * Private constructor to hide the implicit public one.
     */
    private ConfigMain() {

    }

    /**
     * Get properties from configuration file.
     *
     * @return PropertiesConfiguration object
     */
    private static PropertiesConfiguration getConfig() {
        if (config == null) {
            synchronized (ConfigMain.class) {
                PropertiesConfiguration initialized = config;
                if (initialized == null) {
                    try {
                        // Create and initialize the builder
                        ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                            new ReloadingFileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                                .configure(new Parameters().properties()
                                    .setFileName(CONFIG_FILE)
                                    .setListDelimiterHandler(new DefaultListDelimiterHandler('&'))
                                    .setIncludesAllowed(false));
                        // Register an event listener for triggering reloading checks
                        builder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST,
                            event -> builder.getReloadingController().checkForReloading(null));
                        initialized = builder.getConfiguration();
                    } catch (ConfigurationException e) {
                        logger.warn("Loading of {} failed. Trying to start with empty configuration. Exception: {}",
                            CONFIG_FILE, e);
                        initialized = new PropertiesConfiguration();
                    }
                    config = initialized;
                }
            }
        }
        return config;
    }

    /**
     * Request selected parameter with given default value from configuration.
     *
     * @param inParameter
     *            name of parameter in config file
     * @param inDefaultIfNull
     *            default value in case parameter taken from config file is null or
     *            exception occurred
     * @return Parameter as String
     */
    public static String getParameter(String inParameter, String inDefaultIfNull) {
        try {
            return getConfig().getString(inParameter, inDefaultIfNull);
        } catch (ConversionException e) {
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
     * @param inParameter
     *            name of parameter in config file
     * @param inDefault
     *            default value in case parameter taken from config file is null or
     *            exception occurred
     * @return Parameter as boolean
     */
    public static boolean getBooleanParameter(String inParameter, boolean inDefault) {
        try {
            return getConfig().getBoolean(inParameter, inDefault);
        } catch (ConversionException e) {
            return inDefault;
        }
    }

    /**
     * Request int-parameter from Configuration with default-value.
     *
     * @param inParameter
     *            name of parameter in config file
     * @param inDefault
     *            default value in case parameter taken from config file is null or
     *            exception occurred
     * @return Parameter as int
     */
    public static int getIntParameter(String inParameter, int inDefault) {
        try {
            return getConfig().getInt(inParameter, inDefault);
        } catch (NoSuchElementException e) {
            return inDefault;
        }
    }

    /**
     * Request String[]-parameter from Configuration.
     *
     * @param inParameter
     *            as Parameter whose value is to be returned
     * @return Parameter as String[]
     */
    public static String[] getStringArrayParameter(String inParameter) {
        return getConfig().getStringArray(inParameter);
    }
}
