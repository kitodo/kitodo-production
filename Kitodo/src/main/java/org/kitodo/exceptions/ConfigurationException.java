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

public class ConfigurationException extends Exception {
    /**
     * Constructor with given exception message.
     *
     * @param exceptionMessage
     *            as String
     */
    public ConfigurationException(String exceptionMessage) {
        super(exceptionMessage);
    }

    /**
     * Constructor with given exception message and throwable cause.
     *
     * @param exceptionMessage as String
     * @param cause as Throwable
     */
    public ConfigurationException(String exceptionMessage, Throwable cause) {
        super(exceptionMessage, cause);
    }
}
