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

public interface KitodoEncryptionStrategy {
    
    /**
     * Encrypt text given the user-defined secret key.
     * 
     * @param value the text to be encrypted
     * @param secret the user-defined secret key
     * @return the encrypted text
     * @throws KitodoEncryptionException in case encryption fails
     */
    public String encrypt(String value, String secret) throws KitodoEncryptionException;

    /**
     * Decrypt a previously encrypted text given the user-defined secret key.
     * 
     * @param encryptedValue the previously encrypted text
     * @param secret the user-defined secret key
     * @return the decrypted text
     * @throws KitodoEncryptionException in case decryption fails
     */
    public String decrypt(String encryptedValue, String secret) throws KitodoEncryptionException;

}
