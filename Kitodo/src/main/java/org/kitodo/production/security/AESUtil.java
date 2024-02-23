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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AESUtil {

    private static final Logger logger = LogManager.getLogger(AESUtil.class);

    private static final SecureRandom secureRandom = new SecureRandom();

    /*
     * DO NOT CHANGE! Identifier for is encryption check and secret key generation.
     * If changed are made, encrypted data cannot be restored.
     */
    private static final String SALT_PREFIX = "KITODO";

    private static final int SALT_LENGTH = 16;

    private static final int IV_LENGTH = 16;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Encrypts a value by the secret using AES/CBC/PKCS5Padding algorithm.
     *
     * <p>
     * Function generates salt to convert secret to AES-256 secret key and makes
     * simple secrets more complex. In addition, function generates initialization
     * vector (iv) to made encrypted value different when original value is the
     * same. Salt and iv will be become one with the cipher text and result is
     * converted to base64. As pseudocode the structure is as follows: BASE64( SALT(
     * SALT_PREFIX + RANDOM ) + CIPHER TEXT + IV ( RANDOM ) )
     * </p>
     *
     * @param value
     *            The value to encrypt
     * @param secret
     *            The secret from config properties
     * @return The encrypted value as base64 string.
     */
    public static String encrypt(String value, String secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        if (Objects.isNull(value)) {
            return StringUtils.EMPTY;
        }

        // generate salt
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        System.arraycopy(SALT_PREFIX.getBytes(), 0, salt, 0, SALT_PREFIX.getBytes().length);

        // generate iv
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secret, salt), new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(value.getBytes());

        // prefix cipher text with salt and attach iv to the end
        byte[] cipherCombined = new byte[SALT_LENGTH + cipherText.length + IV_LENGTH];

        System.arraycopy(salt, 0, cipherCombined, 0, SALT_LENGTH);
        System.arraycopy(cipherText, 0, cipherCombined, SALT_LENGTH, cipherText.length);
        System.arraycopy(iv, 0, cipherCombined, SALT_LENGTH + cipherText.length, iv.length);

        return Base64.getEncoder().encodeToString(cipherCombined);
    }

    /**
     * Checks if value is encrypted.
     *
     * <p>
     * Function checks if value starts with defined salt prefix after base64
     * decoding.
     * </p>
     *
     * @param potentialEncryptedValue
     *            The value to be checked.
     * @return boolean true if value is encrypted using encrypt function
     */
    public static boolean isEncrypted(String potentialEncryptedValue) {
        try {
            if (Objects.nonNull(potentialEncryptedValue)) {
                byte[] cipherCombined = Base64.getDecoder().decode(potentialEncryptedValue);
                byte[] saltPrefix = Arrays.copyOfRange(cipherCombined, 0, SALT_PREFIX.getBytes().length);
                // check if cipher combined has salt prefix
                return Arrays.equals(saltPrefix, SALT_PREFIX.getBytes());
            }
        } catch (IllegalArgumentException e) {
            logger.debug("Value to encrypt is not a valid base64 string.");
        }
        return false;
    }

    /**
     * Decrypts encrypted value by the secret using AES/CBC/PKCS5Padding algorithm.
     * 
     * <p>
     * Function splits encrypted value salt, cipher text and iv after base64
     * decoding. Salt and secret will be used to build AES-256 secret key. With the
     * help of the iv and the secret key, the cipher text is decrypted and returned.
     * </p>
     *
     * @param encryptValue
     *            The encrypted value
     * @param secret
     *            The secret from config properties
     * @return The decrypted value.
     */
    public static String decrypt(String encryptValue, String secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        byte[] cipherCombined = Base64.getDecoder().decode(encryptValue);

        // get cipherText and iv from cipherCombined
        byte[] salt = Arrays.copyOfRange(cipherCombined, 0, SALT_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(cipherCombined, SALT_LENGTH, cipherCombined.length - IV_LENGTH);
        byte[] iv = Arrays.copyOfRange(cipherCombined, cipherCombined.length - IV_LENGTH, cipherCombined.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secret, salt), new IvParameterSpec(iv));
        byte[] value = cipher.doFinal(cipherText);
        return new String(value);
    }

    private static SecretKey getSecretKey(String secret, byte[] salt)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 256); // AES-256
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = f.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
}
