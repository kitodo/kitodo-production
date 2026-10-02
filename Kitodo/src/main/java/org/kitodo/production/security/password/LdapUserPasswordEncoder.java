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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Sha2Crypt;
import org.kitodo.data.database.enums.PasswordEncryption;

public class LdapUserPasswordEncoder {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    

    /**
     * Encode a password as "userPassword" attribute using a supported encoding method.
     * 
     * <p>SHA-1 and MD5 are considered outdated and insecure.</p>
     * <p>SSHA and CRYPT are the most secure options currently available.</p>
     * 
     * @param method the password encoding method
     * @param password the raw user password
     * @return the encoded password to be set as "userPassword" attribute
     * @throws NoSuchAlgorithmException in case a required algorithm is missing
     */
    public static String encode(PasswordEncryption method, String password) throws NoSuchAlgorithmException {
        return switch (method) {
            case PasswordEncryption.SHA -> encodeShaOne(password);
            case PasswordEncryption.MD5 -> encodeMdFive(password);
            case PasswordEncryption.SSHA -> encodeSaltedShaOne(password);
            case PasswordEncryption.CRYPT_5 -> encodeCryptFive(password);
            case PasswordEncryption.CRYPT_6 -> encodeCryptSix(password);
        };
    }

    /**
     * Encode password as SHA-1 hash without a salt (legacy, not secure).
     * 
     * @param password the password to be encoded
     * @return the encoded password
     * @throws NoSuchAlgorithmException in case hash algorithm SHA-1 is not available
     */
    public static String encodeShaOne(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes(StandardCharsets.UTF_8));
        return "{SHA}" + new String(Base64.encodeBase64(md.digest()), StandardCharsets.UTF_8);
    }

    /**
     * Encode password as MD5 hash without a salt (legacy, not secure).
     * 
     * @param password the password to be encoded
     * @return the encoded password
     * @throws NoSuchAlgorithmException in case hash algorithm MD5 is not available
     */
    public static String encodeMdFive(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes(StandardCharsets.UTF_8));
        return "{MD5}" + new String(Base64.encodeBase64(md.digest()), StandardCharsets.UTF_8);
    }

    /**
     * Encode password as SHA-1 hash including a random salt.
     *
     * <p>See: https://www.openldap.org/faq/data/cache/347.html</p>
     *
     * @param password the password to be encoded
     * @return the encoded password
     * @throws NoSuchAlgorithmException in case hash algorithm SHA-1 is not available
     */
    public static String encodeSaltedShaOne(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        // generate random salt
        final int saltLength = 8;
        byte[] salt = new byte[saltLength];
        SECURE_RANDOM.nextBytes(salt);

        // hash password with salt
        md.update(password.getBytes(StandardCharsets.UTF_8));
        md.update(salt);
        byte[] hash = md.digest();

        // copy hash and salt into one byte array
        byte[] hashAndSalt = new byte[hash.length + salt.length];
        System.arraycopy(hash, 0, hashAndSalt, 0, hash.length);
        System.arraycopy(salt, 0, hashAndSalt, hash.length, salt.length);

        // encode byte array with base64
        return "{SSHA}" + Base64.encodeBase64String(hashAndSalt);
    }

    /**
     * Encode password according to the glibc libcrypt hashing library as SHA-256 hash including a random salt.
     * 
     * <p>See: https://man7.org/linux/man-pages/man3/crypt.3.html</p>
     * <p>See: https://commons.apache.org/proper/commons-codec/archives/1.13/apidocs/org/apache/commons/codec/digest/Sha2Crypt.html</p>
     * 
     * @param password the password to be encoded
     * @return the encoded password
     */
    public static String encodeCryptFive(String password) {
        return "{CRYPT}" + Sha2Crypt.sha256Crypt(password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encode password according to the glibc libcrypt hashing library as SHA-512 hash including a random salt.
     * 
     * <p>See: https://man7.org/linux/man-pages/man3/crypt.3.html</p>
     * <p>See: https://commons.apache.org/proper/commons-codec/archives/1.13/apidocs/org/apache/commons/codec/digest/Sha2Crypt.html</p>
     * 
     * @param password the password to be encoded
     * @return the encoded password
     */
    public static String encodeCryptSix(String password) {
        return "{CRYPT}" + Sha2Crypt.sha512Crypt(password.getBytes(StandardCharsets.UTF_8));
    }

}
