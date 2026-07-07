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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AdaptivePasswordEncoderTest {

    @Test
    public void hashBcryptShouldProduceNonNullResult() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashBcrypt("testPassword");
        assertNotNull(hash);
    }

    @Test
    public void hashBcryptShouldStartWithBcryptPrefix() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashBcrypt("testPassword");
        assertTrue(hash.startsWith("$2"), "Hash should start with $2 for bcrypt, got: " + hash);
    }

    @Test
    public void hashBcryptShouldProduceDifferentHashesForSamePassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash1 = encoder.hashBcrypt("testPassword");
        String hash2 = encoder.hashBcrypt("testPassword");
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to random salt");
    }

    @Test
    public void matchesBcryptShouldReturnTrueForCorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String password = "mySecretPassword123";
        String hash = encoder.hashBcrypt(password);
        assertTrue(encoder.matchesBcrypt(password, hash));
    }

    @Test
    public void matchesBcryptShouldReturnFalseForIncorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashBcrypt("correctPassword");
        assertTrue(!encoder.matchesBcrypt("wrongPassword", hash));
    }

    @Test
    public void hashScryptShouldProduceNonNullResult() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashScrypt("testPassword");
        assertNotNull(hash);
    }

    @Test
    public void hashScryptShouldStartWithScryptPrefix() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashScrypt("testPassword");
        assertTrue(hash.startsWith("$4"), "Hash should start with $4 for scrypt, got: " + hash);
    }

    @Test
    public void hashScryptShouldProduceDifferentHashesForSamePassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash1 = encoder.hashScrypt("testPassword");
        String hash2 = encoder.hashScrypt("testPassword");
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to random salt");
    }

    @Test
    public void matchesScryptShouldReturnTrueForCorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String password = "mySecretPassword123";
        String hash = encoder.hashScrypt(password);
        assertTrue(encoder.matchesScrypt(password, hash));
    }

    @Test
    public void matchesScryptShouldReturnFalseForIncorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashScrypt("correctPassword");
        assertTrue(!encoder.matchesScrypt("wrongPassword", hash));
    }

    @Test
    public void hashPbkdf2WithHmacShouldProduceNonNullResult() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashPbkdf2WithHmac("testPassword");
        assertNotNull(hash);
    }

    @Test
    public void hashPbkdf2WithHmacShouldProduceDifferentHashesForSamePassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash1 = encoder.hashPbkdf2WithHmac("testPassword");
        String hash2 = encoder.hashPbkdf2WithHmac("testPassword");
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to random salt");
    }

    @Test
    public void matchesPbkdf2WithHmacShouldReturnTrueForCorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String password = "mySecretPassword123";
        String hash = encoder.hashPbkdf2WithHmac(password);
        assertTrue(encoder.matchesPbkdf2WithHmac(password, hash));
    }

    @Test
    public void matchesPbkdf2WithHmacShouldReturnFalseForIncorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashPbkdf2WithHmac("correctPassword");
        assertTrue(!encoder.matchesPbkdf2WithHmac("wrongPassword", hash));
    }
}
