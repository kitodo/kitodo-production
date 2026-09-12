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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class SecurityPasswordEncoderTest {
    private static Map<String, String> legacyPasswords;

    static {
        legacyPasswords = new HashMap<>();
        legacyPasswords.put("Password", "6lPEb6Gic+/7BNRdMmL1qQ==");
        legacyPasswords.put("12345678", "wkQy7f152Zl422PTPOPAMQ==");
        legacyPasswords.put("GoobiPassword1234*./", "nDI2cSug5Nj/kkEvKQPBOsHjdTofLmaJ");
        legacyPasswords.put("AreallyreallyreallylongPassword", "89DIASbZ9PNGN132djaFlfVXNo7V3DgBeFCEZG2WmSM=");
        legacyPasswords.put("$%!--_-_/*-äöüä", "/wFe+pyc/QQTmhxAZcjSt9mkwrv03udL");
    }

    @Test
    public void encryptTest() {
        for (String clearText : legacyPasswords.keySet()) {
            String encrypted = new KitodoLegacyPasswordEncoder().encrypt(clearText);
            assertEquals(legacyPasswords.get(clearText), encrypted, "Encrypted Password doesn't match the precomputed one!");
        }
    }

    @Test
    public void decryptTest() {
        for (String clearText : legacyPasswords.keySet()) {
            String decrypted = new KitodoLegacyPasswordEncoder().decrypt(legacyPasswords.get(clearText));
            assertEquals(clearText, decrypted, "Decrypted Password doesn't match the given plain text");
        }
    }

    /**
     * Check that legacy passwords are considered for upgrade to newer encoder.
     */
    @Test
    public void needsUpgradeTest() {
        KitodoDelegatingPasswordEncoder encoder = new KitodoDelegatingPasswordEncoder();
        for (String clearText : legacyPasswords.keySet()) {
            assertTrue(encoder.upgradeEncoding(legacyPasswords.get(clearText)));
        }
    }

    /**
     * Check that legacy passwords are auto-upgraded to newer argon2 hashes.
     */
    @Test
    public void isUpgradedTest() {
        KitodoDelegatingPasswordEncoder encoder = new KitodoDelegatingPasswordEncoder();
        for (String clearText : legacyPasswords.keySet()) {
            assertTrue(encoder.encode(legacyPasswords.get(clearText)).startsWith("{argon2}"));
        }
    }

    /**
     * Check that argon2 encoded passwords can be matched.
     */
    @Test
    public void matchTest() {
        KitodoDelegatingPasswordEncoder encoder = new KitodoDelegatingPasswordEncoder();
        String hash = "$argon2id$v=19$m=16384,t=2,p=1$Qu90MIWxOrzVO/JR+rWK7g$uOKxhxY2dkaG63RD+gqR+z7w3NM5Ui4SAemKrN0LRsM";

        // password doesn't match incorrect hash
        assertFalse(encoder.matches("password", "wrong"));

        // password doesn't match hash without argon2 prefix
        assertFalse(encoder.matches("password", hash));

        // password matches correct hash with prefix
        assertTrue(encoder.matches("password", "{argon2}" + hash));
    }

}
