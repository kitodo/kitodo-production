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
    public void hashShouldProduceNonNullResult() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hash("testPassword");
        assertNotNull(hash);
    }

    @Test
    public void hashShouldStartWithBcryptPrefix() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hash("testPassword");
        assertTrue(hash.startsWith("$2"), "Hash should start with $2 for bcrypt, got: " + hash);
    }

    @Test
    public void hashShouldProduceDifferentHashesForSamePassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash1 = encoder.hash("testPassword");
        String hash2 = encoder.hash("testPassword");
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to random salt");
    }

    @Test
    public void matchesShouldReturnTrueForCorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String password = "mySecretPassword123";
        String hash = encoder.hash(password);
        assertTrue(encoder.matches(password, hash));
    }

    @Test
    public void matchesShouldReturnFalseForIncorrectPassword() {
        AdaptivePasswordEncoder encoder = new AdaptivePasswordEncoder();
        String hash = encoder.hash("correctPassword");
        assertTrue(!encoder.matches("wrongPassword", hash));
    }
}
