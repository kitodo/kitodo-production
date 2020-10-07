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

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.enums.ParameterAPI;
import org.kitodo.config.enums.ParameterInterface;

public class KitodoConfig extends Config {

    private static final Logger logger = LogManager.getLogger(KitodoConfig.class);

    private static final String CONFIG_FILE = "kitodo_config.properties";
    public static final int INT_PARAMETER_NOT_DEFINED_OR_ERRONEOUS = 0;

    /**
     * Returns the directory that contains the modules.
     *
     * @return the directory for the process directories
     */
    public static String getKitodoModulesDirectory() {
        return getParameter(ParameterAPI.DIR_MODULES);
    }

    /**
     * Returns the directory that contains the process directories.
     *
     * @return the directory for the process directories
     */
    public static String getKitodoDataDirectory() {
        return getParameter(ParameterAPI.DIR_PROCESSES);
    }

    /**
     * Returns the directory that contains XML configuration files.
     *
     * @return the directory for XML configuration files
     */
    public static String getKitodoConfigDirectory() {
        return getParameter(ParameterAPI.DIR_XML_CONFIG);
    }

    /**
     * Returns the selected parameter from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @return value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception occurred
     */
    public static String getParameter(ParameterInterface key) {
        return getParameter(key.getName());
    }

    /**
     * Returns the selected parameter from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            whose value is to be returned
     * @return value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception occurred
     */
    public static String getParameter(String key) {
        try {
            return resolveOsSpecific(getConfig().getString(key));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("No configuration found in " + CONFIG_FILE + " for key " + key + "!");
        }
    }

    /**
     * Returns the selected parameter from the configuration file. If no such
     * parameter exists, returns the given default value.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @param defaultValue
     *            default value in case parameter taken from config file does not
     *            exist or exception occurred
     * @return value for the requested key, or {@code defaultIfNull} if not found
     */
    public static String getParameter(ParameterInterface key, String defaultValue) {
        try {
            return getConfig().getString(key.getName(), defaultValue);
        } catch (ConversionException e) {
            logConversionException(key.getName(), CONFIG_FILE, String.class, e, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the selected parameter from the configuration file. If no such
     * parameter exists, returns the given default value.
     *
     * @param key
     *            whose value is to be returned
     * @param defaultValue
     *            default value in case parameter taken from config file does not
     *            exist or exception occurred
     * @return value for the requested key, or {@code defaultIfNull} if not found
     */
    // TODO: there is still one place when it is needed
    public static String getParameter(String key, String defaultValue) {
        try {
            return getConfig().getString(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, CONFIG_FILE, String.class, e, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the selected boolean parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code boolean},
     * returns {@code false}.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @return boolean value for the requested key, or {@code false} if not found or
     *         not parsing
     */
    public static boolean getBooleanParameter(ParameterInterface key) {
        return getBooleanParameter(key, false);
    }

    /**
     * Returns the selected boolean parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code boolean},
     * returns the provided default value.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @param defaultValue
     *            default value in case parameter taken from config file does not
     *            exist or exception occurred
     * @return boolean value for the requested key, or {@code defaultIfNull} if not
     *         found or not parsing
     */
    public static boolean getBooleanParameter(ParameterInterface key, boolean defaultValue) {
        try {
            return getConfig().getBoolean(key.getName(), defaultValue);
        } catch (ConversionException e) {
            logConversionException(key.getName(), CONFIG_FILE, boolean.class, e, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the selected int parameter from the configuration file. If no such
     * parameter exists, or the value cannot be parsed to {@code int}, returns
     * {@code 0}.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @return int value for the requested key, or {@code 0} if not found or not
     *         parsing
     */
    public static int getIntParameter(ParameterInterface key) {
        return getIntParameter(key, INT_PARAMETER_NOT_DEFINED_OR_ERRONEOUS);
    }

    /**
     * Returns the selected int parameter from the configuration file. If no such
     * parameter exists, or the value cannot be parsed to {@code int}, returns the
     * provided default value.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @param defaultValue
     *            default value in case parameter taken from config file does not
     *            exist or exception occurred
     * @return int value for the requested key, or {@code defaultIfNull} if not
     *         found or not parsing
     */
    public static int getIntParameter(ParameterInterface key, int defaultValue) {
        try {
            return getConfig().getInt(key.getName(), defaultValue);
        } catch (ConversionException e) {
            logConversionException(key.getName(), CONFIG_FILE, int.class, e, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public static PropertiesConfiguration getConfig() {
        return Config.getConfig(CONFIG_FILE);
    }

    /**
     * Returns the selected parameter from the configuration file, if any.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @return Optional holding the value for the requested key, else empty.
     */
    public static Optional<String> getOptionalString(ParameterInterface key) {
        try {
            return Optional.of(resolveOsSpecific(getConfig().getString(key.getName())));
        } catch (NoSuchElementException e) {
            logger.catching(Level.TRACE, e);
            return Optional.empty();
        }
    }

    /**
     * Returns the selected URI from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @return URI value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception occurred
     */
    public static URI getUriParameter(ParameterInterface key) {
        return Paths.get(getParameter(key)).toUri();
    }

    /**
     * Returns the selected URI from the configuration file. Throws a
     * {@code NoSuchElementException} if no such parameter exists.
     *
     * @param key
     *            as ParameterInterface enum implementation whose value is to be
     *            returned
     * @param fullFilenameToAdd
     *            the filename (or path) to attach to the base
     * @return URI value for the requested key
     * @throws NoSuchElementException
     *             if parameter taken from config file is null or exception occurred
     */
    public static URI getUriParameter(ParameterInterface key, String fullFilenameToAdd) {
        return Paths.get(FilenameUtils.concat(getParameter(key), fullFilenameToAdd)).toUri();
    }

    /**
     * Parameter value like "foo(.bar|.baz)" will resolve to "foo.bar" on Unix
     * and to "foo.baz" on Windows.
     *
     * @param string
     *            string to process
     * @return OS-specific string
     */
    private static String resolveOsSpecific(String string) {
        return string.replaceFirst("\\((\\p{Graph}*)\\|(\\p{Graph}*)\\)$", File.separatorChar == '/' ? "$1" : "$2");
    }
}
