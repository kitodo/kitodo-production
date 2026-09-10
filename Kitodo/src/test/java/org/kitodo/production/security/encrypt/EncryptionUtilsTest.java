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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EncryptionUtilsTest {
    
    private static final String RANDOM_SECRET = ",~!TrI8?3|:wX0@n=~Y#L|u;jiU|M#1Q&-K?^19.?-%byr] dHpv4).lq* ?s+^+";
    private static final String RANDOM_INPUT = "M9NsvwYkGqu03n59g9iq";
     
    // ciphers for "kitodo" using secret key "kitodo"
    private static final String FIXTURE_SECRET = "kitodo";
    private static final String FIXTURE_INPUT = "kitodo";
    private static final String AES_CBC_CIPHER = "S0lUT0RPz8oMzAbM9ouNjp1s8LbNNNtkALHmpAItVEPi0g9i5K/50QmtUffMqyR3";
    private static final String AES_GCM_CIPHER = "{aes-gcm}S0lUT0RPggBsoJEsQ8NCyJZDXmCbXELGVffZlKHEDbW0qgiGA4CD+RgZAj0hIB1LNSkvyte1";

    /**
     * Input text can be encrypted and decrypted correctly with the default encryption strategy.
     */
    @Test 
    public void defaultEncryptAndDecryptTest() {
        assertEquals(RANDOM_INPUT, EncryptionUtils.decrypt(EncryptionUtils.encrypt(RANDOM_INPUT, RANDOM_SECRET), RANDOM_SECRET));
    }

    /**
     * For legacy reasons, passwords were stored unencrypted and should be passed-through even when an admin
     * defines a new secret (before migrating and encrypting all passwords).
     */
    @Test 
    public void rawTextIsDecryptedTest() {
        assertEquals(RANDOM_INPUT, EncryptionUtils.decrypt(RANDOM_INPUT, RANDOM_SECRET));
    }

    /**
     * Fixtures of ciphers can be correctly decrypted.
     */
    @Test 
    public void decryptFromFixtureTest() {
        assertEquals(FIXTURE_INPUT, EncryptionUtils.decrypt(AES_CBC_CIPHER, FIXTURE_SECRET));
        assertEquals(FIXTURE_INPUT, EncryptionUtils.decrypt(AES_GCM_CIPHER, FIXTURE_SECRET));
    }

    /**
     * Check that legacy encrypted values are reported as needing an upgrade.
     */
    @Test 
    public void needsUpgradeTest() {
        assertTrue(EncryptionUtils.needsUpgrade(RANDOM_INPUT));
        assertTrue(EncryptionUtils.needsUpgrade(AES_CBC_CIPHER));
        assertFalse(EncryptionUtils.needsUpgrade(AES_GCM_CIPHER));
        assertFalse(EncryptionUtils.needsUpgrade(EncryptionUtils.upgradeEncryption(RANDOM_INPUT, RANDOM_SECRET)));
        assertFalse(EncryptionUtils.needsUpgrade(EncryptionUtils.upgradeEncryption(AES_CBC_CIPHER, FIXTURE_SECRET)));
        assertFalse(EncryptionUtils.needsUpgrade(EncryptionUtils.upgradeEncryption(AES_GCM_CIPHER, FIXTURE_SECRET)));
    }

    /**
     * Check that upgrading a cipher yields a new cipher that can be correctly decrypted.
     */
    @Test 
    public void doUpgradeTest() {
        assertEquals(
            RANDOM_INPUT, 
            EncryptionUtils.decrypt(EncryptionUtils.upgradeEncryption(RANDOM_INPUT, RANDOM_SECRET), RANDOM_SECRET)
        );
        assertEquals(
            FIXTURE_INPUT, 
            EncryptionUtils.decrypt(EncryptionUtils.upgradeEncryption(AES_CBC_CIPHER, FIXTURE_SECRET), FIXTURE_SECRET)
        );
        assertEquals(
            FIXTURE_INPUT, 
            EncryptionUtils.decrypt(EncryptionUtils.upgradeEncryption(AES_GCM_CIPHER, FIXTURE_SECRET), FIXTURE_SECRET)
        );
    }

}
