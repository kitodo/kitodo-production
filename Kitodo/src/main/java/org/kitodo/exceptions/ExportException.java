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

/**
 * An exception thrown to indicate that an attempt to export a process failed.
 */
public class ExportException extends RuntimeException {

    /**
     * Constructs an {@code ExportException} with the specified detail message.
     *
     * @param exceptionMessage the detail message
     */
    public ExportException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
