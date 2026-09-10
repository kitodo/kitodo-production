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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Encryption strategy based on "LegacyAesUtils".
 */
@Deprecated 
public class LegacyEncryptionStrategy implements KitodoEncryptionStrategy {

    @Override
    @Deprecated 
    public String encrypt(String value, String secret) {
        try {
            return LegacyAesUtil.encrypt(value, secret);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                        | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException
                        | IllegalBlockSizeException | InvalidKeySpecException e) {
            throw new KitodoEncryptionException(e);
        }
    }

    @Override
    @Deprecated 
    public String decrypt(String encryptedValue, String secret) {
        if (!LegacyAesUtil.isEncrypted(encryptedValue)) {
            // value was previously not encrypted even though a secret is configured
            return encryptedValue;
        }

        // decrypt
        try {
            return LegacyAesUtil.decrypt(encryptedValue, secret);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                    | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException
                    | IllegalBlockSizeException | InvalidKeySpecException e) {
            throw new KitodoEncryptionException(e);
        }        
    }
    
}
