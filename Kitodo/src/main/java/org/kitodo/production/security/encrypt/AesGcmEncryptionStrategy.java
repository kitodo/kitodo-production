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

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Encryption strategy based on AES-GCM with Argon2id encoded 256-bit secret key.
 * 
 * <p>DO NOT CHANGE! If this file is changed, it may not be possible to decrypt previously encrypted values. 
 * Instead, add a new encryption strategy implemnentation to EncryptionUtils and migrate to that.</p>
 */
public class AesGcmEncryptionStrategy implements KitodoEncryptionStrategy {

    private static final SecureRandom secureRandom = new SecureRandom();

    // encryption parameters
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String SALT_PREFIX = "KITODO";
    private static final int SALT_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 16;
    private static final int GCM_TAG_LENGTH = 128;

    // argon2id parameter based on spring-security defaults
    // see org.springframework.security.crypto.argon2.Argon2PasswordEncoder
    private static final int ARGON2ID_MEMORY_KB = 1 << 14; // 16 MiB
    private static final int ARGON2ID_ITERATIONS = 2; 
    private static final int ARGON2ID_PARALLELISM = 1;
    private static final int ARGON2ID_KEY_LENGTH = 32; // 256 bits (required for AES)

    @Override
    public String encrypt(String value, String secret) throws KitodoEncryptionException {
        try {
            // generate salt
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);
            System.arraycopy(SALT_PREFIX.getBytes(), 0, salt, 0, SALT_PREFIX.getBytes().length);

            // generate iv (or nonce)
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // encrypt
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secret, salt), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // combine to [salt + cipher + iv]
            byte[] combined = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, combined, 0, SALT_LENGTH);
            System.arraycopy(cipherText, 0, combined, SALT_LENGTH, cipherText.length);
            System.arraycopy(iv, 0, combined, SALT_LENGTH + cipherText.length, iv.length);

            // encode with base64
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException 
                    | BadPaddingException | IllegalBlockSizeException e) {
            throw new KitodoEncryptionException(e);
        }
    }

    @Override
    public String decrypt(String encryptedValue, String secret) throws KitodoEncryptionException {
        try {
            // decode from base64
            byte[] combined = Base64.getDecoder().decode(encryptedValue);

            // extract [salt + cipher + iv] from combined string
            byte[] salt = Arrays.copyOfRange(combined, 0, SALT_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(combined, SALT_LENGTH, combined.length - GCM_IV_LENGTH);
            byte[] iv = Arrays.copyOfRange(combined, combined.length - GCM_IV_LENGTH, combined.length);

            // decrypt
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secret, salt), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] value = cipher.doFinal(cipherText);

            return new String(value, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException 
                    | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            throw new KitodoEncryptionException(e);
        }
    }
    
    /**
     * Generate a 256bit key from the user-defined secret+salt (with variable length) such that these 256bit 
     * can be used as secret key for the AES encryption.
     * 
     * @param secret the user-defined secret
     * @param salt the salt
     * @return the 256bit secret key to be uesd with AES
     */
    private static SecretKey getSecretKey(String secret, byte[] salt) {
        // specify argon2id parameters
        Argon2Parameters params =
            new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(ARGON2ID_MEMORY_KB)
                .withIterations(ARGON2ID_ITERATIONS)
                .withParallelism(ARGON2ID_PARALLELISM)
                .build();

        // generate 256-bit key
        byte[] password = secret.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[ARGON2ID_KEY_LENGTH];
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        generator.generateBytes(password, key);

        // return key as spec to be used for AES encryption
        return new SecretKeySpec(key, "AES");
    }

}
