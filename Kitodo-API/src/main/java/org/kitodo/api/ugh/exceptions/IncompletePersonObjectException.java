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
 * This exception is thrown, when you are dealing with an incomplete person
 * object. For example, each object of this kind must have a metadata type. If
 * there is none, this exception may be thrown in some methods, which need the
 * type information.
 */
public class IncompletePersonObjectException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code IncompletePersonObjectException}.
     */
    public IncompletePersonObjectException() {
    }

    /**
     * Creates a new {@code IncompletePersonObjectException} with a message.
     * 
     * @param message
     *            error message
     */
    public IncompletePersonObjectException(String message) {
        super(message);
    }

}
