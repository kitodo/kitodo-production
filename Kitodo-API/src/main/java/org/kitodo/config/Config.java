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

import java.net.URI;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Config {
    private static final Logger logger = LogManager.getLogger(Config.class);
    private static volatile PropertiesConfiguration config;
    private static final String CONFIG_FILE = "kitodo_config.properties";
    public static final int INT_PARAMETER_NOT_DEFINED_OR_ERRONEOUS = 0;

    private static final String DIR_MODULES = "moduleFolder";

    /**
     * Absolute path to the directory that process directories will be created
     * in, terminated by a directory separator ("/"). The servlet container must
     * have write permission to that directory.
     */
    private static final String DIR_PROCESSES = "MetadatenVerzeichnis";

    /**
     * Absolute path to the directory that the configuration files are stored
     * in, terminated by a directory separator ("/").
     *
     * <p>
     * Note: Several, but not all configuration files are read from that
     * directory. You may want to decide to point this path to the directory
     * where the servlet container will extract the configuration files to (like
     * webapps/kitodo/WEB-INF/classes) in order to make sure they are found.
     */
    private static final String DIR_XML_CONFIG = "KonfigurationVerzeichnis";

    /**
     * Returns the directory that contains the modules.
     *
     * @return the directory for the process directories
     */
    public static String getKitodoModulesDirectory() {
        return getParameter(DIR_MODULES);
    }

    /**
     * Returns the directory that contains the process directories.
     *
     * @return the directory for the process directories
     */
    public static String getKitodoDataDirectory() {
        return getParameter(DIR_PROCESSES);
    }

    /**
     * Returns the directory that contains XML configuration files.
     *
     * @return the directory for XML configuration files
     */
    public static String getKitodoConfigDirectory() {
        return getParameter(DIR_XML_CONFIG);
    }

    /**
     * Returns the selected parameter from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            key whose value is to be returned
     * @return value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception
     *             occurred
     */
    public static String getParameter(String key) {
        try {
            return getConfig().getString(key);
        } catch (NoSuchElementException e) {
            logger.catching(e);
            throw new NoSuchElementException("No configuration found in kitodo_config.properties for key " + key + "!");
        }
    }

    /**
     * Returns the selected parameter from the configuration file. If no such
     * parameter exists, returns the given default value.
     *
     * @param key
     *            key whose value is to be returned
     * @param defaultValue
     *            default value in case parameter taken from config file does
     *            not exist or exception occurred
     * @return value for the requested key, or {@code defaultIfNull} if not
     *         found
     */
    public static String getParameter(String key, String defaultValue) {
        try {
            return getConfig().getString(key, defaultValue);
        } catch (ConversionException e) {
            logger.catching(e);
            logger.warn("Configuration found in kitodo_config.properties for key {} is defined, but not a String!",
                key);
            return defaultValue;
        }
    }

    /**
     * Returns the selected boolean parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code boolean},
     * returns {@code false}.
     *
     * @param key
     *            key whose value is to be returned
     * @return boolean value for the requested key, or {@code false} if not
     *         found or not parsing
     */
    public static boolean getBooleanParameter(String key) {
        return getBooleanParameter(key, false);
    }

    /**
     * Returns the selected boolean parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code boolean},
     * returns the provided default value.
     *
     * @param key
     *            key whose value is to be returned
     * @param defaultValue
     *            default value in case parameter taken from config file does
     *            not exist or exception occurred
     * @return boolean value for the requested key, or {@code defaultIfNull} if
     *         not found or not parsing
     */
    public static boolean getBooleanParameter(String key, boolean defaultValue) {
        try {
            return getConfig().getBoolean(key, defaultValue);
        } catch (ConversionException e) {
            logger.catching(e);
            logger.warn(
                "Configuration found in kitodo_config.properties for key {} is defined, but cannot be converted to boolean!",
                key);
            return defaultValue;
        }
    }

    /**
     * Returns the selected int parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code int},
     * returns {@code 0}.
     *
     * @param key
     *            key whose value is to be returned
     * @return int value for the requested key, or {@code 0} if not found or not
     *         parsing
     */
    public static int getIntParameter(String key) {
        return getIntParameter(key, INT_PARAMETER_NOT_DEFINED_OR_ERRONEOUS);
    }

    /**
     * Returns the selected int parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code int},
     * returns the provided default value.
     *
     * @param key
     *            key whose value is to be returned
     * @param defaultValue
     *            default value in case parameter taken from config file does
     *            not exist or exception occurred
     * @return int value for the requested key, or {@code defaultIfNull} if not
     *         found or not parsing
     */
    public static int getIntParameter(String key, int defaultValue) {
        try {
            return getConfig().getInt(key, defaultValue);
        } catch (ConversionException e) {
            logger.catching(e);
            logger.warn(
                "Configuration found in kitodo_config.properties for key {} is defined, but cannot be converted to int!",
                key);
            return defaultValue;
        }
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public static PropertiesConfiguration getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                PropertiesConfiguration initialized = config;
                if (initialized == null) {
                    AbstractConfiguration.setDefaultListDelimiter('&');
                    try {
                        initialized = new PropertiesConfiguration(CONFIG_FILE);
                    } catch (ConfigurationException e) {
                        logger.warn("Loading of " + CONFIG_FILE + " failed. Trying to start with empty configuration.",
                            e);
                        initialized = new PropertiesConfiguration();
                    }
                    initialized.setListDelimiter('&');
                    initialized.setReloadingStrategy(new FileChangedReloadingStrategy());
                    initialized.setThrowExceptionOnMissing(true);
                    config = initialized;
                }
            }
        }
        return config;
    }

    /**
     * Returns the selected parameter from the configuration file, if any.
     *
     * @param key
     *            key whose value is to be returned
     * @return Optional holding the value for the requested key, else empty.
     */
    public static Optional<String> getOptionalString(String key) {
        try {
            return Optional.of(getConfig().getString(key));
        } catch (NoSuchElementException e) {
            logger.catching(e);
            return Optional.empty();
        }
    }

    /**
     * Returns the selected URI from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            key whose value is to be returned
     * @return URI value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception
     *             occurred
     */
    public static URI getUri(String key) {
        return Paths.get(getParameter(key)).toUri();
    }

    /**
     * Returns the selected URI from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            key whose value is to be returned
     * @param fullFilenameToAdd
     *            the filename (or path) to attach to the base
     * @return URI value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception
     *             occurred
     */
    public static URI getUri(String key, String fullFilenameToAdd) {
        return Paths.get(FilenameUtils.concat(getParameter(key), fullFilenameToAdd)).toUri();
    }
}
