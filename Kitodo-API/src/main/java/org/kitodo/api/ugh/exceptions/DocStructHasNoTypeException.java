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
 * Exception is thrown, if meta-data should be added, but the document structure has no
 * document structure type.
 */
public class DocStructHasNoTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code DocStructHasNoTypeException}.
     */
    public DocStructHasNoTypeException() {
    }

    /**
     * Creates a new {@code DocStructHasNoTypeException} with a message.
     * 
     * @param message
     *            error message
     */
    public DocStructHasNoTypeException(String message) {
        super(message);
    }

}
