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

package org.kitodo.production.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.xmp.impl.Base64;

import org.junit.jupiter.api.Test;

public class AESUtilTest {

    private String secretInConfig = ",~!TrI8?3|:wX0@n=~Y#L|u;jiU|M#1Q&-K?^19.?-%byr] dHpv4).lq* ?s+^+";

    private String input = "M9NsvwYkGqu03n59g9iq";

    @Test
    public void encryptAndDecryptTest() throws Exception {
        String base64CipherCombined = AESUtil.encrypt(input, secretInConfig);

        String potentialInput = AESUtil.decrypt(base64CipherCombined, secretInConfig);

        assertEquals(input, potentialInput, "Decrypted text does not match the original text");
    }

    @Test
    public void differsEncrypt() throws Exception {
        String firstEncrypt = AESUtil.encrypt(input, secretInConfig);

        String secondEncrypt = AESUtil.encrypt(input, secretInConfig);

        assertNotEquals(firstEncrypt, secondEncrypt, "The encrypted value results are the same. IV does not work.");

        String firstDecrypt = AESUtil.decrypt(firstEncrypt, secretInConfig);

        assertEquals(input, firstDecrypt, "First decrypted text does not match the original text");

        String secondDecrypt = AESUtil.decrypt(secondEncrypt, secretInConfig);

        assertEquals(input, secondDecrypt, "Secound decrypted text does not match the original text");
    }


    @Test
    public void checkIsEncrypted() throws Exception {
        String base64CipherCombined = AESUtil.encrypt(input, secretInConfig);

        assertTrue(AESUtil.isEncrypted(base64CipherCombined));

        String potentialBase64CipherCombined = "Lorem Ipsum";

        assertFalse(AESUtil.isEncrypted(potentialBase64CipherCombined));

        assertFalse(AESUtil.isEncrypted(Base64.encode(potentialBase64CipherCombined)));
    }

}
