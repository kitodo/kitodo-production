package org.kitodo.production.security;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import com.itextpdf.xmp.impl.Base64;


public class AESUtilTest {

    private String secretInConfig = ",~!TrI8?3|:wX0@n=~Y#L|u;jiU|M#1Q&-K?^19.?-%byr] dHpv4).lq* ?s+^+";

    private String input = "M9NsvwYkGqu03n59g9iq";

    @Test
    public void encryptAndDecryptTest() throws Exception {
        String base64CipherCombined = AESUtil.encrypt(input, secretInConfig);

        String potentialInput = AESUtil.decrypt(base64CipherCombined, secretInConfig);

        assertEquals("Decrypted text does not match the original text", input, potentialInput);
    }

    @Test
    public void differsEncrypt() throws Exception {
        String firstEncrypt = AESUtil.encrypt(input, secretInConfig);

        String secondEncrypt = AESUtil.encrypt(input, secretInConfig);

        Assert.assertNotEquals("The encrypted value results are the same. IV does not work.", firstEncrypt, secondEncrypt);

        String firstDecrypt = AESUtil.decrypt(firstEncrypt, secretInConfig);

        assertEquals("First decrypted text does not match the original text", input, firstDecrypt);

        String secondDecrypt = AESUtil.decrypt(secondEncrypt, secretInConfig);

        assertEquals("Secound decrypted text does not match the original text", input, secondDecrypt);
    }


    @Test
    public void checkIsEncrypted() throws Exception {
        String base64CipherCombined = AESUtil.encrypt(input, secretInConfig);

        assertTrue(AESUtil.isEnrypted(base64CipherCombined));

        String potentialBase64CipherCombined = "Lorem Ipsum";

        assertFalse(AESUtil.isEnrypted(potentialBase64CipherCombined));

        assertFalse(AESUtil.isEnrypted(Base64.encode(potentialBase64CipherCombined)));
    }

}
