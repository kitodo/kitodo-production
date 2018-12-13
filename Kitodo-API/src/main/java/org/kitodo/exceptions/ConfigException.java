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

package org.kitodo.exceptions;

public class ConfigException extends RuntimeException {

    /**
     * Constructor with given parameter name for exception message.
     *
     * @param parameterName
     *            as String
     */
    public ConfigException(String parameterName) {
        super("Default value for key: " + parameterName + " not defined!");
    }

    /**
     * Constructor with given parameter name and type for exception message.
     *
     * @param parameterName
     *            as String
     * @param type
     *            as String
     */
    public ConfigException(String parameterName, String type) {
        super("Default value for key: " + parameterName + " not defined or have different type than " + type + "!");
    }

    /**
     * Constructor with given parameter the specified detail message and the cause.
     *
     * @param message
     * @param cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

