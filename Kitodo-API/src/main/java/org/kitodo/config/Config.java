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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.FileSystem;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Config {
    private static final Logger logger = LogManager.getLogger(Config.class);
    private static final Map<String, PropertiesConfiguration> configMap = new ConcurrentHashMap<>();

    /**
     * Returns the configuration.
     *
     * @param configFile
     *            file with configuration
     * @return the configuration
     */
    static PropertiesConfiguration getConfig(String configFile) {
        if (!configMap.containsKey(configFile)) {
            synchronized (Config.class) {
                if (!configMap.containsKey(configFile)) {
                    PropertiesConfiguration initialized;
                    AbstractConfiguration.setDefaultListDelimiter('&');
                    try {
                        initialized = new PropertiesConfiguration(configFile);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Using configuration from "
                                    + ConfigurationUtils.locate(FileSystem.getDefaultFileSystem(), null, configFile));
                        }
                    } catch (ConfigurationException e) {
                        logger.warn("Loading of {} failed. Trying to start with empty configuration.", configFile, e);
                        initialized = new PropertiesConfiguration();
                    }
                    initialized.setListDelimiter('&');
                    initialized.setReloadingStrategy(new FileChangedReloadingStrategy());
                    initialized.setThrowExceptionOnMissing(true);
                    configMap.put(configFile, initialized);
                }
            }
        }
        return configMap.get(configFile);
    }

    /**
     * Logs a conversion exception with a helpful error message.
     *
     * @param key
     *            whose value could not be converted
     * @param failedClass
     *            class to convert the value to
     * @param occurred
     *            conversion exception occurred
     * @param usedValue
     *            default value being used
     */
    static <T> void logConversionException(String configFile, String key, Class<T> failedClass, ConversionException occurred,
                                                   T usedValue) {
        logger.catching(Level.DEBUG, occurred);
        final String message = "Configuration found in {} for key {} is defined as \"{}\", but "
                .concat("cannot be converted to {}! Using the default value of \"{}\".");
        logger.warn(message, configFile, key, failedClass.getSimpleName(), usedValue);
    }
}
