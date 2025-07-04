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
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.beans.Parameter;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.exceptions.ConfigParameterException;

public class ConfigCore extends KitodoConfig {
    private static final Logger logger = LogManager.getLogger(ConfigCore.class);

    /**
     * Private constructor to hide the implicit public one.
     */
    private ConfigCore() {

    }

    /**
     * Request string parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return parameter as String or default value for this parameter
     */
    public static String getParameterOrDefaultValue(ParameterCore key) {
        Parameter<?> parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof String) {
            return getParameter(key.getName(), (String) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "String");
    }

    /**
     * Request boolean parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return parameter as boolean or default value for this parameter
     */
    public static boolean getBooleanParameterOrDefaultValue(ParameterCore key) {
        Parameter<?> parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof Boolean) {
            return getBooleanParameter(key, (Boolean) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "boolean");
    }

    /**
     * Request int parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return parameter as int or default value for this parameter
     */
    public static int getIntParameterOrDefaultValue(ParameterCore key) {
        Parameter<?> parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof Integer) {
            return getIntParameter(key, (Integer) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "int");
    }

    /**
     * Request long parameter from configuration, if parameter is not there - use
     * default value.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return Parameter as long or default value
     */
    public static long getLongParameterOrDefaultValue(ParameterCore key) {
        Parameter<?> parameter = key.getParameter();

        if (parameter.getDefaultValue() instanceof Long) {
            return getLongParameter(key, (Long) parameter.getDefaultValue());
        }
        throw new ConfigParameterException(parameter.getKey(), "long");
    }

    /**
     * Request long parameter or default value from configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @param defaultValue
     *            as long
     * @return Parameter as long or default value
     */
    public static long getLongParameter(ParameterCore key, long defaultValue) {
        return getConfig().getLong(key.getName(), defaultValue);
    }

    /**
     * Request Duration parameter from configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @param temporalUnit
     *            as TemporalUnit
     * @return Parameter as Duration
     */
    public static Duration getDurationParameter(ParameterCore key, TemporalUnit temporalUnit) {
        long duration = getLongParameterOrDefaultValue(key);
        return Duration.of(duration, temporalUnit);
    }

    /**
     * Request String[]-parameter from Configuration.
     *
     * @param key
     *            as Parameter whose value is to be returned
     * @return Parameter as String[]
     */
    public static String[] getStringArrayParameter(ParameterCore key) {
        return getConfig().getStringArray(key.getName());
    }

    /**
     * Get Kitodo debug directory.
     *
     * @return String
     */
    public static File getKitodoDebugDirectory() {
        try {
            File debugDirectory = new File(ConfigCore.getParameter(ParameterCore.DIR_DEBUG));
            if (!debugDirectory.exists()) {
                logger.debug("Cannot save debug output to {}: Directory does not exist", debugDirectory);
                return null;
            }
            if (!debugDirectory.isDirectory()) {
                logger.debug("Cannot save debug output to {}: Not a directory", debugDirectory);
                return null;
            }
            if (!debugDirectory.canWrite()) {
                logger.debug("Cannot save debug output to {}: Directory is not writable", debugDirectory);
                return null;
            }
            return debugDirectory;
        } catch (NoSuchElementException debugDirectoryNotConfigured) {
            logger.catching(Level.TRACE, debugDirectoryNotConfigured);
            return null;
        }
    }

    /**
     * Get Kitodo diagram directory.
     *
     * @return String
     */
    public static String getKitodoDiagramDirectory() {
        return getParameter(ParameterCore.DIR_DIAGRAMS);
    }
}
