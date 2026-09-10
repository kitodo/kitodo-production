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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.xmp.impl.Base64;

import java.util.List;

import org.junit.jupiter.api.Test;

public class EncryptionStrategiesTest {

    private static final String SECRET = ",~!TrI8?3|:wX0@n=~Y#L|u;jiU|M#1Q&-K?^19.?-%byr] dHpv4).lq* ?s+^+";
    private static final String INPUT = "M9NsvwYkGqu03n59g9iq";

    private static List<KitodoEncryptionStrategy> STRATEGIES = List.of(
        new LegacyEncryptionStrategy(),
        new AesGcmEncryptionStrategy()
    );

    /**
     * Check that encrypted text can be decrypted and matches.
     */
    @Test
    public void encryptAndDecryptTest() throws Exception {
        for (KitodoEncryptionStrategy strategy : STRATEGIES) {
            String cipher = strategy.encrypt(INPUT, SECRET);
            String potentialInput = strategy.decrypt(cipher, SECRET);
            assertEquals(INPUT, potentialInput, "Decrypted text does not match the original text");
        }
    }

    /**
     * Check that two encryptions of the same text lead to two different ciphers, both of which can be decrypted correctly.
     */
    @Test
    public void differsEncrypt() throws Exception {
        for (KitodoEncryptionStrategy strategy : STRATEGIES) {
            String firstEncrypt = strategy.encrypt(INPUT, SECRET);
            String secondEncrypt = strategy.encrypt(INPUT, SECRET);
            assertNotEquals(firstEncrypt, secondEncrypt, "The encrypted value results are the same. IV does not work.");

            String firstDecrypt = strategy.decrypt(firstEncrypt, SECRET);
            assertEquals(INPUT, firstDecrypt, "First decrypted text does not match the original text");

            String secondDecrypt = strategy.decrypt(secondEncrypt, SECRET);
            assertEquals(INPUT, secondDecrypt, "Second decrypted text does not match the original text");
        }
    }

    /**
     * Check that not-encrypted text can be distinguished when using legacy encryption strategy.
     */
    @Test
    public void checkIsEncrypted() throws Exception {
        String cipher = LegacyAesUtil.encrypt(INPUT, SECRET);
        assertTrue(LegacyAesUtil.isEncrypted(cipher));

        String potentialCipher = "Lorem Ipsum";
        assertFalse(LegacyAesUtil.isEncrypted(potentialCipher));
        assertFalse(LegacyAesUtil.isEncrypted(Base64.encode(potentialCipher)));
    }

}
