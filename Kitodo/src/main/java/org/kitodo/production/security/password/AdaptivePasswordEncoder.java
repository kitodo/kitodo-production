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

package org.kitodo.production.security.password;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

public class AdaptivePasswordEncoder {

    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder(16);
    private static final SCryptPasswordEncoder SCRYPT_ENCODER = new SCryptPasswordEncoder(16, 8, 1, 32, 16);
    private static final int PBKDF2_ITERATIONS = 185000;
    private static final int SALT_LENGTH = 16;

    public String hashBcrypt(String rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    public boolean matchesBcrypt(String rawPassword, String encodedPassword) {
        return BCRYPT_ENCODER.matches(rawPassword, encodedPassword);
    }

    public String hashScrypt(String rawPassword) {
        return SCRYPT_ENCODER.encode(rawPassword);
    }

    public boolean matchesScrypt(String rawPassword, String encodedPassword) {
        return SCRYPT_ENCODER.matches(rawPassword, encodedPassword);
    }

    public String hashPbkdf2(String rawPassword) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, PBKDF2_ITERATIONS, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            byte[] hashAndSalt = new byte[hash.length + salt.length];
            System.arraycopy(hash, 0, hashAndSalt, 0, hash.length);
            System.arraycopy(salt, 0, hashAndSalt, hash.length, salt.length);
            return Base64.encodeBase64String(hashAndSalt);
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 hashing failed", e);
        }
    }

    public boolean matchesPbkdf2(String rawPassword, String encodedPassword) {
        try {
            byte[] decoded = Base64.decodeBase64(encodedPassword);
            byte[] storedHash = new byte[32];
            byte[] storedSalt = new byte[SALT_LENGTH];
            System.arraycopy(decoded, 0, storedHash, 0, 32);
            System.arraycopy(decoded, 32, storedSalt, 0, SALT_LENGTH);
            KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), storedSalt, PBKDF2_ITERATIONS, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] computedHash = factory.generateSecret(spec).getEncoded();
            byte[] computedHashAndSalt = new byte[computedHash.length + storedSalt.length];
            System.arraycopy(computedHash, 0, computedHashAndSalt, 0, computedHash.length);
            System.arraycopy(storedSalt, 0, computedHashAndSalt, computedHash.length, storedSalt.length);
            return Base64.encodeBase64String(computedHashAndSalt).equals(encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
