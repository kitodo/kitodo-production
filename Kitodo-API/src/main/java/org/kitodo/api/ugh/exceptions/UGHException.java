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
 * Base class for all UGH exceptions.
 */
public class UGHException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code PreferencesException}.
     */
    public UGHException() {
    }

    /**
     * Creates a new {@code PreferencesException} with a message.
     *
     * @param message
     *            error message
     */
    public UGHException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code PreferencesException} with a message and a cause.
     *
     * @param message
     *            error message
     * @param cause
     *            error cause
     */
    public UGHException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@code PreferencesException} with a cause.
     *
     * @param cause
     *            error cause
     */
    public UGHException(Throwable cause) {
        super(cause.getClass().getSimpleName() + (cause.getMessage() != null ? ": " + cause.getMessage() : ""), cause);
    }
}
