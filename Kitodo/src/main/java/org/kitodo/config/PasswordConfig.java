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

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PasswordConfig extends Config {

    private static final String CONFIG_FILE = "password-rules.properties";

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public static PropertiesConfiguration getConfig() {
        return Config.getConfig(CONFIG_FILE);
    }

    /**
     * Get min length of password.
     *
     * @return min length of password
     */
    public static int getLengthMin() {
        return getIntParameter("length.min", 8);
    }

    /**
     * Get max length of password.
     *
     * @return max length of password
     */
    public static int getLengthMax() {
        return getIntParameter("length.max", 30);
    }

    /**
     * Get min number of digit characters in password.
     *
     * @return min number of digit characters
     */
    public static int getNumberOfDigitCharacters() {
        return getIntParameter("character.digit", 1);
    }

    /**
     * Get min number of special characters in password.
     *
     * @return min number of special characters
     */
    public static int getNumberOfSpecialCharacters() {
        return getIntParameter("character.special", 1);
    }

    /**
     * Get min number of uppercase characters in password.
     *
     * @return min number of uppercase characters
     */
    public static int getNumberOfUppercaseCharacters() {
        return getIntParameter("character.upperCase", 1);
    }

    /**
     * Check if whitespace is allowed in password.
     * 
     * @return true if whitespace is allowed, false otherwise
     */
    public static boolean isWhitespaceAllowed() {
        return getBooleanParameter("allowWhitespace", false);
    }

    /**
     * Get list of not allowed words.
     *
     * @return list of not allowed words
     */
    public static String[] getNotAllowedWords() {
        return getStringArrayParameter("notAllowedWords");
    }

    /**
     * Returns the selected boolean parameter from the configuration file. If no
     * such parameter exists, or the value cannot be parsed to {@code boolean},
     * returns the provided default value.
     *
     * @param key
     *            whose value is to be returned
     * @param defaultValue
     *            default value in case parameter taken from config file does not
     *            exist or exception occurred
     * @return boolean value for the requested key, or {@code defaultIfNull} if not
     *         found or not parsing
     */
    private static boolean getBooleanParameter(String key, boolean defaultValue) {
        try {
            return getConfig().getBoolean(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, CONFIG_FILE, boolean.class, e, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the selected int parameter from the configuration file. If no such
     * parameter exists, or the value cannot be parsed to {@code int}, returns the
     * provided default value.
     *
     * @param key
     *            whose value is to be returned
     * @param defaultValue
     *            default value in case parameter taken from config file does not
     *            exist or exception occurred
     * @return int value for the requested key, or {@code defaultIfNull} if not
     *         found or not parsing
     */
    private static int getIntParameter(String key, int defaultValue) {
        try {
            return getConfig().getInt(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, CONFIG_FILE, int.class, e, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Request String[]-parameter from Configuration.
     *
     * @param key
     *            whose value is to be returned
     * @return Parameter as String[]
     */
    public static String[] getStringArrayParameter(String key) {
        return getConfig().getStringArray(key);
    }
}
