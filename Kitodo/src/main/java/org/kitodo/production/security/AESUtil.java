package org.kitodo.production.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final int SALT_LENGTH = 16;

    private static final int IV_LENGTH = 16;

    public static SecretKey getSecretKey( String secret ) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if(Objects.isNull(secret)) {
            secret = "";
        }

        // generate salt
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 256); // AES-256
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = f.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key,"AES");
    }

    public static String encrypt(String algorithm, String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(algorithm);

        // generate iv
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(input.getBytes());

        // attach iv to the end
        byte[] cipherCombined = new byte[cipherText.length + IV_LENGTH];
        System.arraycopy(cipherText,0, cipherCombined, 0, cipherText.length);
        System.arraycopy(iv,0, cipherCombined,  cipherText.length, iv.length);

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
