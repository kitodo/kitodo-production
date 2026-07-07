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

package org.kitodo.data.database.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PasswordEncryptionTest {

    @Test
    public void shaHasCorrectValues() {
        assertEquals(0, PasswordEncryption.SHA.getValue());
        assertEquals("SHA", PasswordEncryption.SHA.getTitle());
        assertEquals("{SSHA}", PasswordEncryption.SHA.getLdapPrefix());
    }

    @Test
    public void md5HasCorrectValues() {
        assertEquals(1, PasswordEncryption.MD5.getValue());
        assertEquals("MD5", PasswordEncryption.MD5.getTitle());
        assertEquals("{SMD5}", PasswordEncryption.MD5.getLdapPrefix());
    }

    @Test
    public void sha256HasCorrectValues() {
        assertEquals(2, PasswordEncryption.SHA_256.getValue());
        assertEquals("SHA-256", PasswordEncryption.SHA_256.getTitle());
        assertEquals("{SSHA-256}", PasswordEncryption.SHA_256.getLdapPrefix());
    }

    @Test
    public void getEncryptionFromValueReturnsCorrectEnum() {
        assertEquals(PasswordEncryption.SHA, PasswordEncryption.getEncryptionFromValue(0));
        assertEquals(PasswordEncryption.MD5, PasswordEncryption.getEncryptionFromValue(1));
        assertEquals(PasswordEncryption.SHA_256, PasswordEncryption.getEncryptionFromValue(2));
    }

    @Test
    public void getEncryptionFromValueReturnsDefaultForNull() {
        assertEquals(PasswordEncryption.SHA, PasswordEncryption.getEncryptionFromValue(null));
    }

    @Test
    public void getEncryptionFromValueReturnsDefaultForUnknownValue() {
        assertEquals(PasswordEncryption.SHA, PasswordEncryption.getEncryptionFromValue(99));
    }

    @Test
    public void allEnumValuesAreUnique() {
        Set<Integer> seen = new HashSet<>();
        for (PasswordEncryption pe : PasswordEncryption.values()) {
            assertTrue(seen.add(pe.getValue()), "Duplicate getValue(): " + pe.getValue());
        }
    }
}
