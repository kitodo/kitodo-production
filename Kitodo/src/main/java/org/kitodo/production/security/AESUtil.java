package org.kitodo.production.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final int IV_LENGTH = 16;

    public static SecretKey getSecretKey( byte[] secret ) {
        if(Objects.isNull(secret)) {
            secret = "".getBytes();
        }
        return new SecretKeySpec(secret,"AES");
    }

    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encrypt(String algorithm, String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(algorithm);
        IvParameterSpec iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());

        // attach iv to the end
        byte[] cipherCombined = new byte[cipherText.length + IV_LENGTH];
        System.arraycopy(cipherText,0, cipherCombined, 0, cipherText.length);
        System.arraycopy(iv.getIV(),0, cipherCombined,  cipherText.length, iv.getIV().length);

        return Base64.getEncoder()
                .encodeToString(cipherCombined);
    }

    public static String decrypt(String algorithm, String base64CipherCombined, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        byte[] cipherCombined = Base64.getDecoder().decode(base64CipherCombined);

        // get cipherText and iv from cipherCombined
        byte[] cipherText = Arrays.copyOfRange(cipherCombined, 0, cipherCombined.length - IV_LENGTH);
        byte[] iv = Arrays.copyOfRange(cipherCombined, cipherCombined.length - IV_LENGTH, cipherCombined.length);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText);
    }
}
