package org.kitodo.production.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;



public class AESUtilTest {

    private String secretInConfig = ",~!TrI8?3|:wX0@n=~Y#L|u;jiU|M#1Q&-K?^19.?-%byr] dHpv4).lq* ?s+^+";
    private String algorithm = "AES/CBC/PKCS5Padding";
    private String input = "secretPassword";

    @Test
    public void encryptAndDecryptTest() throws Exception {
        SecretKey secretKey = AESUtil.getSecretKey(secretInConfig);
        assertNotNull(secretKey);

        String base64CipherCombined = AESUtil.encrypt(algorithm, input, secretKey);

        String potentialInput = AESUtil.decrypt(algorithm, base64CipherCombined, secretKey);

        assertEquals("Decrypted text does not match the original text", input, potentialInput);
    }

    @Test
    public void differsEncrypt() throws Exception {
        SecretKey secretKey = AESUtil.getSecretKey(secretInConfig);
        assertNotNull(secretKey);

        String firstEncrypt = AESUtil.encrypt(algorithm, input, secretKey);

        String secondEncrypt = AESUtil.encrypt(algorithm, input, secretKey);

        Assert.assertNotEquals("The encrypted value results are the same. IV does not work.", firstEncrypt, secondEncrypt);

        String firstDecrypt = AESUtil.decrypt(algorithm, firstEncrypt, secretKey);

        assertEquals("First decrypted text does not match the original text", input, firstDecrypt);

        String secondDecrypt = AESUtil.decrypt(algorithm, secondEncrypt, secretKey);

        assertEquals("Secound decrypted text does not match the original text", input, secondDecrypt);
    }

}
