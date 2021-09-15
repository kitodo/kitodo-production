package org.kitodo.production.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.crypto.SecretKey;

import org.junit.Test;

public class AESUtilTest {

    @Test
    public void encryptTest() throws Exception {
        String secretKeyInConfig = "X8eU~sL8x seiB4?To=!HFslc_HG%C;F";
        String algorithm = "AES/CBC/PKCS5Padding";
        String input = "secretPassword";
        SecretKey secretKey = AESUtil.getSecretKey(secretKeyInConfig.getBytes());
        assertNotNull(secretKey);

        String base64CipherCombined = AESUtil.encrypt(algorithm, input, secretKey);

        String potentialInput = AESUtil.decrypt(algorithm, base64CipherCombined, secretKey);

        assertEquals("Decrypted text does not match the original text", input, potentialInput);
    }

}
