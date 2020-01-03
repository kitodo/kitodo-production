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

package org.kitodo.data.elasticsearch.exceptions;

/**
 * Exception for checking code statuses from server responses.
 */
public class CustomResponseException extends Exception {

    public CustomResponseException(Exception exception) {
        super(exception);
    }

    public CustomResponseException(String message) {
        super(message);
    }
}
