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
 * This exception is thrown when no ResponseHandler could be found for a given MetadataFormat.
 */
public class ResponseHandlerNotFoundException extends Exception {

    /**
     * Standard constructor with a message String.
     *
     * @param message exception message
     */
    public ResponseHandlerNotFoundException(String message) {
        super(message);
    }

}
