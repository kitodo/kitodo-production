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
 * This exception is thrown, if a {@code ContentFile} is not linked to a
 * {@code ContentFile} object, but it is assumed, that it is.
 */
public class ContentFileNotLinkedException extends UGHException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code ContentFileNotLinkedException}.
     */
    public ContentFileNotLinkedException() {
    }

    /**
     * Creates a new {@code ContentFileNotLinkedException} with a message.
     * 
     * @param message
     *            error message
     */
    public ContentFileNotLinkedException(String message) {
        super(message);
    }

}
