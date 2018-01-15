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
 * Exception indicating that a type is not allowed as child.
 */
public class TypeNotAllowedAsChildException extends UGHException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code TypeNotAllowedAsChildException} with a message.
     *
     * @param message
     *            error message
     */
    public TypeNotAllowedAsChildException(String message) {
        super(message);
    }

}
