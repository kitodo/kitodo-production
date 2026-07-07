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
        assertTrue(hash.startsWith("$s0"), "Hash should start with $s0 for scrypt, got: " + hash);
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
    public void hashPbkdf2ShouldProduceNonNullResult() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashPbkdf2("testPassword");
        assertNotNull(hash);
    }

    @Test
    public void hashPbkdf2ShouldProduceDifferentHashesForSamePassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash1 = encoder.hashPbkdf2("testPassword");
        String hash2 = encoder.hashPbkdf2("testPassword");
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to random salt");
    }

    @Test
    public void matchesPbkdf2ShouldReturnTrueForCorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String password = "mySecretPassword123";
        String hash = encoder.hashPbkdf2(password);
        assertTrue(encoder.matchesPbkdf2(password, hash));
    }

    @Test
    public void matchesPbkdf2ShouldReturnFalseForIncorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hashPbkdf2("correctPassword");
        assertTrue(!encoder.matchesPbkdf2("wrongPassword", hash));
    }
}
