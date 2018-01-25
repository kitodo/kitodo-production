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

package org.kitodo.api.ugh.exceptions;

/**
 * Exception indicating that a meta-data type is not allowed.
 */
public class MetadataTypeNotAllowedException extends UGHException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code MetadataTypeNotAllowedException}.
     */
    public MetadataTypeNotAllowedException() {
    }

    /**
     * Creates a new {@code MetadataTypeNotAllowedException} with a message.
     *
     * @param message
     *            error message
     */
    public MetadataTypeNotAllowedException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code MetadataTypeNotAllowedException} with a message and
     * a cause.
     *
     * @param message
     *            error message
     * @param cause
     *            error cause
     */
    public MetadataTypeNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

}
