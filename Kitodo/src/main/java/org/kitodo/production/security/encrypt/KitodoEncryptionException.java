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

package org.kitodo.production.security.encrypt;

public class KitodoEncryptionException extends RuntimeException {

    /**
     * Simple message based exception.
     * 
     * @param message the message
     */
    public KitodoEncryptionException(String message) {
        super(message);
    }

    /**
     * Passthrough constructor.
     *
     * @param cause the cause
     */
    public KitodoEncryptionException(Throwable cause) {
        super(cause);
    }
    
}
